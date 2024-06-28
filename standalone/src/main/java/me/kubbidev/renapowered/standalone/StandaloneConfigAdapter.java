package me.kubbidev.renapowered.standalone;

import me.kubbidev.renapowered.common.config.generic.adapter.ConfigurateConfigAdapter;
import me.kubbidev.renapowered.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.nio.file.Path;

public class StandaloneConfigAdapter extends ConfigurateConfigAdapter implements ConfigurationAdapter {
    public StandaloneConfigAdapter(RenaPlugin plugin, Path path) {
        super(plugin, path);
    }

    @Override
    protected ConfigurationLoader<? extends ConfigurationNode> createLoader(Path path) {
        return YAMLConfigurationLoader.builder().setPath(path).build();
    }
}