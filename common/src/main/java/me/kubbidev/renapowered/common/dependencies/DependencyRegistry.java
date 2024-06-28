package me.kubbidev.renapowered.common.dependencies;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import me.kubbidev.renapowered.common.dependencies.relocation.Relocation;
import me.kubbidev.renapowered.common.plugin.platform.Platform;
import me.kubbidev.renapowered.common.storage.StorageType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies RenaPowered specific behaviour for {@link Dependency}s.
 */
public class DependencyRegistry {

    private static final SetMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES = ImmutableSetMultimap.<StorageType, Dependency>builder()
            .putAll(StorageType.MONGODB, Dependency.MONGODB_DRIVER_CORE, Dependency.MONGODB_DRIVER_LEGACY, Dependency.MONGODB_DRIVER_SYNC, Dependency.MONGODB_DRIVER_BSON)
            .build();

    private static final Set<Platform.Type> SNAKEYAML_PROVIDED_BY_PLATFORM = ImmutableSet.of(

    );

    private final Platform.Type platformType;

    public DependencyRegistry(Platform.Type platformType) {
        this.platformType = platformType;
    }

    public Set<Dependency> resolveStorageDependencies(Set<StorageType> storageTypes) {
        Set<Dependency> dependencies = new LinkedHashSet<>();
        for (StorageType storageType : storageTypes) {
            dependencies.addAll(STORAGE_DEPENDENCIES.get(storageType));
        }

        // don't load slf4j if it's already present
        if ((dependencies.contains(Dependency.SLF4J_API) || dependencies.contains(Dependency.SLF4J_SIMPLE)) && slf4jPresent()) {
            dependencies.remove(Dependency.SLF4J_API);
            dependencies.remove(Dependency.SLF4J_SIMPLE);
        }

        // don't load snakeyaml if it's provided by the platform
        if (dependencies.contains(Dependency.SNAKEYAML) && SNAKEYAML_PROVIDED_BY_PLATFORM.contains(this.platformType)) {
            dependencies.remove(Dependency.SNAKEYAML);
        }

        return dependencies;
    }

    public void applyRelocationSettings(Dependency dependency, List<Relocation> relocations) {
        // relocate yaml within configurate if its being provided by RP
        if (dependency == Dependency.CONFIGURATE_YAML && !SNAKEYAML_PROVIDED_BY_PLATFORM.contains(this.platformType)) {
            relocations.add(Relocation.of("yaml", "org{}yaml{}snakeyaml"));
        }
    }

    public boolean shouldAutoLoad(Dependency dependency) {
        return switch (dependency) {
            // all used within 'isolated' classloaders, and are therefore not
            // relocated.
            case ASM, ASM_COMMONS, JAR_RELOCATOR -> false;
            default -> true;
        };
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean slf4jPresent() {
        return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
    }
}