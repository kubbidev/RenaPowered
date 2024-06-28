package me.kubbidev.renapowered.common.config.generic.adapter;

import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EnvironmentVariableConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "RENAPOWERED_";

    private final RenaPlugin plugin;

    public EnvironmentVariableConfigAdapter(RenaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> RENAPOWERED_SERVER
        // 'data.table_prefix' -> RENAPOWERED_DATA_TABLE_PREFIX
        String key = PREFIX + path.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');

        String value = System.getenv(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger().info(String.format("Resolved configuration value from environment variable: %s = %s", key, printableValue));
        }
        return value;
    }

    @Override
    public RenaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void reload() {
        // no-op
    }
}