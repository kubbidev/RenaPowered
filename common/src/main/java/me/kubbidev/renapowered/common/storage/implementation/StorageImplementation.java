package me.kubbidev.renapowered.common.storage.implementation;

import me.kubbidev.renapowered.common.model.manager.abstraction.Manager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.storage.StorageMetadata;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Experimental
public interface StorageImplementation {

    RenaPlugin getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    StorageMetadata getMeta();

    <I, T extends BaseEntity> T loadEntity(Class<T> type, I id, Manager<I, T> manager)
            throws Exception;

    <I, T extends BaseEntity> void loadAllEntities(Class<T> type, Manager<I, T> manager)
            throws Exception;

    <T extends BaseEntity> void saveEntity(T o)
            throws Exception;

    <I, T extends BaseEntity> Set<I> getUniqueEntities(Class<T> type)
            throws Exception;
}