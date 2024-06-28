package me.kubbidev.renapowered.common.plugin.bootstrap;

import me.kubbidev.renapowered.common.plugin.classpath.ClassPathAppender;
import me.kubbidev.renapowered.common.plugin.logging.PluginLogger;
import me.kubbidev.renapowered.common.plugin.platform.Platform;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Bootstrap plugin interface
 *
 * <p>Instances of this interface are responsible for loading the
 * "RenaPowered plugin" on their respective platforms.</p>
 */
public interface RenaBootstrap extends Platform {

    /**
     * Gets the plugin logger
     *
     * @return the logger
     */
    PluginLogger getPluginLogger();

    /**
     * Gets an adapter for the platforms scheduler
     *
     * @return the scheduler
     */
    SchedulerAdapter getScheduler();

    /**
     * Gets a {@link ClassPathAppender} for this bootstrap plugin
     *
     * @return a class path appender
     */
    ClassPathAppender getClassPathAppender();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has loaded.
     *
     * @return a loading latch
     */
    CountDownLatch getLoadLatch();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has enabled.
     *
     * @return an enable latch
     */
    CountDownLatch getEnableLatch();

    /**
     * Gets a string of the plugin's version
     *
     * @return the version of the plugin
     */
    String getVersion();

    /**
     * Gets the name or "brand" of the running platform
     *
     * @return the server brand
     */
    String getServerBrand();

    /**
     * Gets the version of the running platform
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Gets the plugins main data storage directory
     *
     * @return the platforms data folder
     */
    Path getDataDirectory();

    /**
     * Gets the plugins configuration directory
     *
     * @return the config directory
     */
    default Path getConfigDirectory() {
        return getDataDirectory();
    }

    /**
     * Gets a bundled resource file from the jar
     *
     * @param path the path of the file
     * @return the file as an input stream
     */
    default InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }
}