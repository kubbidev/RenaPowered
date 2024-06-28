package me.kubbidev.renapowered.common.config;

import lombok.Getter;
import me.kubbidev.renapowered.common.config.generic.KeyedConfiguration;
import me.kubbidev.renapowered.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;

@Getter
public class RenaConfiguration extends KeyedConfiguration {
    private final RenaPlugin plugin;

    public RenaConfiguration(RenaPlugin plugin, ConfigurationAdapter adapter) {
        super(adapter, ConfigKeys.getKeys());
        this.plugin = plugin;

        init();
    }
}
