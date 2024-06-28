package me.kubbidev.renapowered.standalone;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.renapowered.common.dependencies.Dependency;
import me.kubbidev.renapowered.common.dependencies.DependencyManager;
import me.kubbidev.renapowered.common.dependencies.DependencyManagerImpl;
import me.kubbidev.renapowered.common.dependencies.relocation.RelocationHandler;
import me.kubbidev.renapowered.common.util.MoreFiles;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pre-loads and pre-relocates all possible dependencies.
 */
@SuppressWarnings("ConfusingMainMethod")
public class StandaloneDependencyPreloader {

    public static void main(String[] args) throws Exception {
        main();
    }

    public static void main() throws Exception {
        Path cacheDirectory = Paths.get("data").resolve("libs");
        MoreFiles.createDirectoriesIfNotExists(cacheDirectory);

        ExecutorService executorService = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setDaemon(true).build());
        DependencyManager dependencyManager = new DependencyManagerImpl(cacheDirectory, executorService);

        Set<Dependency> dependencies = new HashSet<>(Arrays.asList(Dependency.values()));
        System.out.println("Preloading " + dependencies.size() + " dependencies, please wait...");

        dependencies.removeAll(RelocationHandler.DEPENDENCIES);
        dependencyManager.loadDependencies(RelocationHandler.DEPENDENCIES);
        dependencyManager.loadDependencies(dependencies);

        System.out.println("Done!");
    }
}
