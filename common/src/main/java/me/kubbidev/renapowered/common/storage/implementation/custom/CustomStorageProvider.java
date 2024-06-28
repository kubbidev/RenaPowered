package me.kubbidev.renapowered.common.storage.implementation.custom;

import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.storage.implementation.StorageImplementation;

/**
 * A storage provider
 */
@FunctionalInterface
public interface CustomStorageProvider {

    StorageImplementation provide(RenaPlugin plugin);

}