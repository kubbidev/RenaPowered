package me.kubbidev.renapowered.standalone.loader;

import me.kubbidev.renapowered.common.loader.JarInJarClassLoader;
import me.kubbidev.renapowered.common.loader.LoaderBootstrap;
import me.kubbidev.renapowered.standalone.app.RenaApplication;
import me.kubbidev.renapowered.standalone.app.integration.ShutdownCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Loader bootstrap for RenaPowered running as a "standalone" app.
 * <p>
 * There are three main modules:
 * 1. the loader (this)
 *      - performs jar-in-jar loading for the plugin
 *      - starts the application
 * <p>
 * 2. the plugin (RStandaloneBootstrap, RStandalonePlugin, etc)
 *      - implements the standard classes required to create an abstract RenaPowered "plugin")
 * <p>
 * 3. the application
 *      - allows the user to interact with the plugin through a basic terminal layer
 */
public class StandaloneLoader implements ShutdownCallback {
    public static final Logger LOGGER = LogManager.getLogger(StandaloneLoader.class);

    private static final String JAR_NAME = "renapowered-standalone.jarinjar";
    private static final String BOOTSTRAP_PLUGIN_CLASS = "me.kubbidev.renapowered.standalone.PStandaloneBootstrap";
    private static final String BOOTSTRAP_DEPENDENCY_PRELOADER_CLASS = "me.kubbidev.renapowered.standalone.StandaloneDependencyPreloader";

    private RenaApplication app;
    private JarInJarClassLoader loader;
    private LoaderBootstrap plugin;

    // Entrypoint
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOGGER.error("Exception in thread " + t.getName(), e));

        StandaloneLoader loader = new StandaloneLoader();
        loader.start(args);
    }

    public void start(String[] args) {
        // construct an application, but don't "start" it yet
        this.app = new RenaApplication(this);

        // create a jar-in-jar classloader for the standalone plugin, then enable it
        // the application is passes to the plugin constructor, to allow it to pass hooks back
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        // special case for dependency preload command
        if (args.length == 1 && args[0].equals("preloadDependencies")) {
            try {
                Class<?> clazz = this.loader.loadClass(BOOTSTRAP_DEPENDENCY_PRELOADER_CLASS);
                clazz.getMethod("main").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        this.plugin = this.loader.instantiatePlugin(BOOTSTRAP_PLUGIN_CLASS, RenaApplication.class, this.app);
        this.plugin.onLoad();
        this.plugin.onEnable();

        // start the application
        this.app.start();
    }

    @Override
    public void shutdown() {
        // shutdown in reverse order
        this.app.close();
        this.plugin.onDisable();
        try {
            this.loader.close();
        } catch (IOException e) {
            LOGGER.error(e);
        }

        LogManager.shutdown(true);
    }
}