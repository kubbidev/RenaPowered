package me.kubbidev.renapowered.common.config.generic.adapter;

import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import org.jetbrains.annotations.Nullable;

public class SystemPropertyConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "renapowered.";

    private final RenaPlugin plugin;

    public SystemPropertyConfigAdapter(RenaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> renapowered.server
        // 'data.table_prefix' -> renapowered.data.table-prefix
        String key = PREFIX + path;

        String value = System.getProperty(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger().info(String.format("Resolved configuration value from system property: %s = %s", key, printableValue));
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