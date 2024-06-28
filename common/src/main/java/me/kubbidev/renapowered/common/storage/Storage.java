package me.kubbidev.renapowered.common.storage;

import lombok.Getter;
import me.kubbidev.renapowered.common.model.manager.abstraction.Manager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.storage.implementation.StorageImplementation;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import me.kubbidev.renapowered.common.util.AsyncInterface;

import java.util.concurrent.CompletableFuture;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link StorageImplementation}.
 */
public class Storage extends AsyncInterface {
    private final RenaPlugin plugin;

    @Getter
    private final StorageImplementation implementation;

    public Storage(RenaPlugin plugin, StorageImplementation implementation) {
        super(plugin);
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public String getName() {
        return this.implementation.getImplementationName();
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation", e);
        }
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown storage implementation", e);
        }
    }

    public StorageMetadata getMeta() {
        return this.implementation.getMeta();
    }

    public <T extends BaseEntity, I> CompletableFuture<T> loadEntity(Class<T> type, I id, Manager<I, T> manager) {
        return future(() -> this.implementation.loadEntity(type, id, manager));
    }

    public <T extends BaseEntity, I> CompletableFuture<Void> loadAllEntities(Class<T> type, Manager<I, T> manager) {
        return future(() -> this.implementation.loadAllEntities(type, manager));
    }

    public <T extends BaseEntity> CompletableFuture<Void> saveEntity(T o) {
        return future(() -> this.implementation.saveEntity(o));
    }
}