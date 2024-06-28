package me.kubbidev.renapowered.common.storage;

import com.google.common.collect.ImmutableSet;
import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.MongoStorage;
import me.kubbidev.renapowered.common.storage.implementation.StorageImplementation;
import me.kubbidev.renapowered.common.storage.implementation.custom.CustomStorageProviders;

import java.util.Set;

public class StorageFactory {
    private final RenaPlugin plugin;

    public StorageFactory(RenaPlugin plugin) {
        this.plugin = plugin;
    }

    public Set<StorageType> getRequiredTypes() {
        return ImmutableSet.of(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD));
    }

    public Storage getInstance() {
        StorageType type = this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
        this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");

        Storage storage = new Storage(this.plugin, createNewImplementation(type));
        storage.init();
        return storage;
    }

    private StorageImplementation createNewImplementation(StorageType method) {
        return switch (method) {
            case CUSTOM -> CustomStorageProviders.getProvider().provide(this.plugin);
            case MONGODB -> new MongoStorage(
                    this.plugin,
                    this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES),
                    this.plugin.getConfiguration().get(ConfigKeys.MONGODB_COLLECTION_PREFIX),
                    this.plugin.getConfiguration().get(ConfigKeys.MONGODB_CONNECTION_URI)
            );
        };
    }
}