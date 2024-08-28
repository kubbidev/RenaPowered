package me.kubbidev.renapowered.common.storage.implementation.mongodb;

import com.google.common.base.Strings;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.kubbidev.renapowered.common.cache.LoadingMap;
import me.kubbidev.renapowered.common.model.manager.abstraction.Manager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.storage.StorageMetadata;
import me.kubbidev.renapowered.common.storage.implementation.StorageImplementation;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import me.kubbidev.renapowered.common.storage.misc.StorageCredentials;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class MongoStorage implements StorageImplementation {
    private final RenaPlugin plugin;
    private final StorageCredentials configuration;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private final String prefix;
    private final String connectionUri;

    /**
     * Map storing all {@link MongoEntity} for iteration facility
     */
    private final Map<Class<?>, MongoEntity> entitiesMap = LoadingMap.of(MongoEntity::new);

    public MongoStorage(RenaPlugin plugin, StorageCredentials configuration, String prefix, String connectionUri) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.prefix = prefix;
        this.connectionUri = connectionUri;
    }

    @Override
    public RenaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return "MongoDB";
    }

    @Override
    public void init() {
        MongoClientOptions.Builder options = MongoClientOptions.builder()
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY);

        if (!Strings.isNullOrEmpty(this.connectionUri)) {
            this.mongoClient = new MongoClient(new MongoClientURI(this.connectionUri, options));
        } else {
            MongoCredential credential = null;
            if (!Strings.isNullOrEmpty(this.configuration.username())) {
                credential = MongoCredential.createCredential(
                        this.configuration.username(),
                        this.configuration.database(),
                        Strings.isNullOrEmpty(this.configuration.password()) ? new char[0] : this.configuration.password().toCharArray()
                );
            }

            String[] addressSplit = this.configuration.address().split(":");
            String host = addressSplit[0];
            int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 27017;
            ServerAddress address = new ServerAddress(host, port);

            if (credential == null) {
                this.mongoClient = new MongoClient(address, options.build());
            } else {
                this.mongoClient = new MongoClient(address, credential, options.build());
            }
        }

        this.database = this.mongoClient.getDatabase(this.configuration.database());
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public StorageMetadata getMeta() {
        StorageMetadata metadata = new StorageMetadata();

        boolean success = true;
        long start = System.currentTimeMillis();

        try {
            this.database.runCommand(new Document("ping", 1));
        } catch (Exception e) {
            success = false;
        }

        if (success) {
            int duration = (int) (System.currentTimeMillis() - start);
            metadata.ping(duration);
        }

        metadata.connected(success);
        return metadata;
    }

    /**
     * This record represent an data object used for caching
     * result from database.
     *
     * <p>It exist for the reason that even if the {@link Document}
     * could not be found, the entity need to be created without exception.</p>
     *
     * @param resultSet a map containing the required values of the
     *                  entity currently loaded
     */
    public record MongoData(Map<String, Object> resultSet) {

    }

    @Override
    public <I, T extends BaseEntity> T loadEntity(Class<T> type, I id, Manager<I, T> manager) throws Exception {
        MongoEntity entity = this.entitiesMap.get(type);
        MongoData mongoData = null;

        MongoCollection<Document> c = this.database.getCollection(this.prefix + entity.getCollectionName());
        try (MongoCursor<Document> cursor = c.find(Filters.eq(entity.getId(), id)).iterator()) {
            if (cursor.hasNext()) {
                mongoData = fillMongoData(entity, cursor.next());
            }
        }
        return buildInstance(entity, manager, id, mongoData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, T extends BaseEntity> void loadAllEntities(Class<T> type, Manager<I, T> manager) throws Exception {
        MongoEntity entity = this.entitiesMap.get(type);
        Map<I, MongoData> result = new HashMap<>();

        MongoCollection<Document> c = this.database.getCollection(this.prefix + entity.getCollectionName());
        try (MongoCursor<Document> cursor = c.find().iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                result.put((I) d.get(entity.getId()), fillMongoData(entity, d));
            }
        }

        for (Map.Entry<I, MongoData> e : result.entrySet()) {
            buildInstance(entity, manager,
                    e.getKey(),
                    e.getValue());
        }
        manager.retainAll(result.keySet());
    }

    @Override
    public <T extends BaseEntity> void saveEntity(T o) throws Exception {
        MongoEntity entity = this.entitiesMap.get(o.getClass());

        MongoCollection<Document> c = this.database.getCollection(this.prefix + entity.getCollectionName());
        Document d = new Document();
        for (MongoEntry entry : entity.getEntries()) {
            Field field = entry.field();

            if (field.trySetAccessible()) {
                d.append(entry.name(), field.get(o));
            }
        }

        c.replaceOne(Filters.eq(entity.getId(), o.getId()), d,
                new ReplaceOptions().upsert(true));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, T extends BaseEntity> Set<I> getUniqueEntities(Class<T> type) {
        MongoEntity entity = this.entitiesMap.get(type);
        Set<I> ids = new HashSet<>();

        MongoCollection<Document> c = this.database.getCollection(this.prefix + entity.getCollectionName());
        try (MongoCursor<Document> cursor = c.find().iterator()) {
            while (cursor.hasNext()) {
                try {
                    ids.add((I) cursor.next().get(entity.getId()));
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        return ids;
    }

    private static <I, T extends BaseEntity> T buildInstance(MongoEntity entity, Manager<I, T> manager, I id, @Nullable MongoData data)
            throws IllegalAccessException {

        T o = manager.getOrMake(id);
        if (data != null) {
            for (MongoEntry entry : entity.getEntries()) {
                Field field = entry.field();

                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                if (field.trySetAccessible()) {
                    field.set(o, data.resultSet.get(entry.name()));
                }
            }
        }
        return o;
    }

    private static MongoData fillMongoData(MongoEntity entity, Document d) {
        Map<String, Object> objectMap = new HashMap<>();

        for (MongoEntry entry : entity.getEntries()) {
            objectMap.put(entry.name(), d.get(entry.name()));
        }
        return new MongoData(objectMap);
    }
}