package me.kubbidev.renapowered.common.config;

import me.kubbidev.renapowered.common.config.generic.KeyedConfiguration;
import me.kubbidev.renapowered.common.config.generic.key.ConfigKey;
import me.kubbidev.renapowered.common.config.generic.key.SimpleConfigKey;
import me.kubbidev.renapowered.common.storage.StorageType;
import me.kubbidev.renapowered.common.storage.misc.StorageCredentials;
import me.kubbidev.renapowered.common.config.generic.key.ConfigKeyFactory;

import java.util.List;
import java.util.Locale;

import static me.kubbidev.renapowered.common.config.generic.key.ConfigKeyFactory.*;

/**
 * All of the {@link ConfigKey}s used by RenaPowered.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
@SuppressWarnings("CodeBlock2Expr")
public final class ConfigKeys {

    /**
     * The Discord application authentication token used to connect
     */
    public static final ConfigKey<String> AUTHENTICATION_TOKEN = notReloadable(ConfigKeyFactory.stringKey("application.authentication-token", null));

    /**
     * The amount of shard that will be use to shard the Discord application
     */
    public static final ConfigKey<Integer> TOTAL_SHARDS = notReloadable(ConfigKeyFactory.integerKey("application.total-shards", 1));

    /**
     * The name of the storage method being used
     */
    public static final ConfigKey<StorageType> STORAGE_METHOD = notReloadable(key(c -> {
        return StorageType.parse(c.getString("storage-method", "MongoDB"), StorageType.MONGODB);
    }));

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<StorageCredentials> DATABASE_VALUES = notReloadable(key(c -> {
        return new StorageCredentials(
                c.getString("data.address", null),
                c.getString("data.database", null),
                c.getString("data.username", null),
                c.getString("data.password", null)
        );
    }));

    /**
     * The prefix for any MongoDB collections
     */
    public static final ConfigKey<String> MONGODB_COLLECTION_PREFIX = notReloadable(stringKey("data.mongodb-collection-prefix", ""));

    /**
     * MongoDB ClientConnectionURI to override default connection options
     */
    public static final ConfigKey<String> MONGODB_CONNECTION_URI = notReloadable(stringKey("data.mongodb-connection-uri", ""));

    /**
     * How many minutes to wait between syncs. A value <= 0 will disable syncing.
     */
    public static final ConfigKey<Integer> SYNC_TIME = notReloadable(ConfigKeyFactory.integerKey("sync-minutes", 10));

    /**
     * If RenaPowered should rate-limit command executions.
     */
    public static final ConfigKey<Boolean> COMMANDS_RATE_LIMIT = booleanKey("commands-rate-limit", true);

    /**
     * A list of the keys defined in this class.
     */
    private static final List<SimpleConfigKey<?>> KEYS = KeyedConfiguration.initialise(ConfigKeys.class);

    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }

    /**
     * Check if the value at the given path should be censored in console/log output
     *
     * @param path the path
     * @return true if the value should be censored
     */
    public static boolean shouldCensorValue(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("uri");
    }
}