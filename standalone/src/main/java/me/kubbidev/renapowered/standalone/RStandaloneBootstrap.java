package me.kubbidev.renapowered.standalone;

import me.kubbidev.renapowered.common.loader.LoaderBootstrap;
import me.kubbidev.renapowered.common.plugin.bootstrap.BootstrappedWithLoader;
import me.kubbidev.renapowered.common.plugin.bootstrap.RenaBootstrap;
import me.kubbidev.renapowered.common.plugin.classpath.ClassPathAppender;
import me.kubbidev.renapowered.common.plugin.classpath.JarInJarClassPathAppender;
import me.kubbidev.renapowered.common.plugin.logging.Log4jPluginLogger;
import me.kubbidev.renapowered.common.plugin.logging.PluginLogger;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.renapowered.standalone.app.RenaApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

/**
 * Bootstrap plugin for RenaPowered running as a standalone app.
 */
public class RStandaloneBootstrap implements RenaBootstrap, LoaderBootstrap, BootstrappedWithLoader {
    private final RenaApplication loader;

    private final PluginLogger logger;
    private final StandaloneSchedulerAdapter schedulerAdapter;
    private final ClassPathAppender classPathAppender;
    private final RStandalonePlugin plugin;

    private Instant startTime;
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);

    public RStandaloneBootstrap(RenaApplication loader) {
        this.loader = loader;

        this.logger = new Log4jPluginLogger(RenaApplication.LOGGER);
        this.schedulerAdapter = new StandaloneSchedulerAdapter(this);
        this.classPathAppender = new JarInJarClassPathAppender(getClass().getClassLoader());
        this.plugin = new RStandalonePlugin(this);
    }

    // provide adapters

    @Override
    public RenaApplication getLoader() {
        return this.loader;
    }

    @Override
    public PluginLogger getPluginLogger() {
        return this.logger;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    @Override
    public ClassPathAppender getClassPathAppender() {
        return this.classPathAppender;
    }

    // lifecycle

    @Override
    public void onLoad() {
        try {
            this.plugin.load();
        } finally {
            this.loadLatch.countDown();
        }
    }

    @Override
    public void onEnable() {
        this.startTime = Instant.now();
        try {
            this.plugin.enable();
        } finally {
            this.enableLatch.countDown();
        }
    }

    @Override
    public void onDisable() {
        this.plugin.disable();
    }

    @Override
    public CountDownLatch getLoadLatch() {
        return this.loadLatch;
    }

    @Override
    public CountDownLatch getEnableLatch() {
        return this.enableLatch;
    }

    // provide information about the plugin

    @Override
    public String getVersion() {
        return this.loader.getVersion().replace('\n', ' ');
    }

    @Override
    public Instant getStartTime() {
        return this.startTime;
    }

    // provide information about the platform

    @Override
    public Type getType() {
        return Type.STANDALONE;
    }

    @Override
    public String getServerBrand() {
        return "standalone";
    }

    @Override
    public String getServerVersion() {
        return "n/a";
    }

    @Override
    public Path getDataDirectory() {
        return Paths.get("data").toAbsolutePath();
    }
}
