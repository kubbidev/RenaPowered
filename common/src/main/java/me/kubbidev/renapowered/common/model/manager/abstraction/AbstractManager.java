package me.kubbidev.renapowered.common.model.manager.abstraction;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.renapowered.common.cache.LoadingMap;

import java.util.Collection;
import java.util.Map;

/**
 * An abstract manager class
 *
 * @param <I> the class used to identify each object held in this manager
 * @param <T> the implementation class this manager is "managing"
 */
public abstract class AbstractManager<I, T> implements Manager<I, T> {

    private final LoadingMap<I, T> objects = LoadingMap.of(this);

    @Override
    public Map<I, T> getAll() {
        return ImmutableMap.copyOf(this.objects);
    }

    @Override
    public T getOrMake(I id) {
        return this.objects.get(sanitizeIdentifier(id));
    }

    @Override
    public T getIfLoaded(I id) {
        return this.objects.getIfPresent(sanitizeIdentifier(id));
    }

    @Override
    public boolean isLoaded(I id) {
        return this.objects.containsKey(sanitizeIdentifier(id));
    }

    @Override
    public void unload(I id) {
        if (id != null) {
            this.objects.remove(sanitizeIdentifier(id));
        }
    }

    @Override
    public void retainAll(Collection<I> ids) {
        this.objects.keySet().stream()
                .filter(g -> !ids.contains(g))
                .forEach(this::unload);
    }

    protected I sanitizeIdentifier(I i) {
        return i;
    }

}