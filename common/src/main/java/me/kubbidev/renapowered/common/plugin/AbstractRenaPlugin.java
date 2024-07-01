package me.kubbidev.renapowered.common.plugin;

import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.config.RenaConfiguration;
import me.kubbidev.renapowered.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.renapowered.common.config.generic.adapter.EnvironmentVariableConfigAdapter;
import me.kubbidev.renapowered.common.config.generic.adapter.MultiConfigurationAdapter;
import me.kubbidev.renapowered.common.config.generic.adapter.SystemPropertyConfigAdapter;
import me.kubbidev.renapowered.common.dependencies.Dependency;
import me.kubbidev.renapowered.common.dependencies.DependencyManager;
import me.kubbidev.renapowered.common.dependencies.DependencyManagerImpl;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.locale.TranslationManager;
import me.kubbidev.renapowered.common.plugin.logging.PluginLogger;
import me.kubbidev.renapowered.common.storage.Storage;
import me.kubbidev.renapowered.common.storage.StorageFactory;
import me.kubbidev.renapowered.common.tasks.SyncTask;
import me.kubbidev.renapowered.common.worker.DiscordService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRenaPlugin implements RenaPlugin {

    // init during load
    private DependencyManager dependencyManager;
    private TranslationManager translationManager;

    // init during enable
    private RenaConfiguration configuration;
    private Storage storage;
    private SyncTask.Buffer syncTaskBuffer;
    private DiscordService discordService;

    /**
     * Performs the initial actions to load the plugin
     */
    public final void load() {
        // load dependencies
        getLogger().info("Loading dependencies, please wait...");

        this.dependencyManager = createDependencyManager();
        this.dependencyManager.loadDependencies(getGlobalDependencies());

        // load translations
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();
    }

    public final void enable() {
        // load the sender factory instance
        setupSenderFactory();

        // send the startup banner
        Message.STARTUP_BANNER.send(getConsoleSender(), getBootstrap());

        // load configuration
        getLogger().info("Loading configuration...");
        ConfigurationAdapter configFileAdapter = provideConfigurationAdapter();
        this.configuration = new RenaConfiguration(this, new MultiConfigurationAdapter(this,
                new SystemPropertyConfigAdapter(this),
                new EnvironmentVariableConfigAdapter(this),
                configFileAdapter
        ));

        // now the configuration is loaded, we can create a storage factory and load initial dependencies
        StorageFactory storageFactory = new StorageFactory(this);
        this.dependencyManager.loadStorageDependencies(storageFactory.getRequiredTypes());

        // initialise storage
        this.storage = storageFactory.getInstance();

        // setup the update task buffer
        this.syncTaskBuffer = new SyncTask.Buffer(this);

        // register commands
        registerCommands();

        // setup guild/user/member manager
        setupManagers();

        // setup platform hooks
        setupPlatformHooks();

        // schedule update tasks
        int syncMins = getConfiguration().get(ConfigKeys.SYNC_TIME);
        if (syncMins > 0) {
            getBootstrap().getScheduler().asyncRepeating(() -> this.syncTaskBuffer.request(), syncMins, TimeUnit.MINUTES);
        }

        // setup the discord connection service
        this.discordService = new DiscordService(this);
        this.discordService.enable();

        // run an update instantly.
        getLogger().info("Performing initial data load...");
        try {
            new SyncTask(this).run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // perform any platform-specific final setup tasks
        performFinalSetup();

        Duration timeTaken = Duration.between(getBootstrap().getStartTime(), Instant.now());
        getLogger().info("Successfully enabled. (took " + timeTaken.toMillis() + "ms)");
    }

    public final void disable() {
        getLogger().info("Starting shutdown process...");

        // cancel delayed/repeating tasks
        getBootstrap().getScheduler().shutdownScheduler();

        // close the discord connection
        getLogger().info("Closing connection...");
        getDiscordService().close();

        // remove any hooks into the platform
        removePlatformHooks();

        // close storage
        getLogger().info("Closing storage...");
        this.storage.shutdown();

        // shutdown async executor pool
        getBootstrap().getScheduler().shutdownExecutor();

        /* wait for the discord connection to shutdown. because the event is fired async, it can be called after
           the plugin has shutdown and so after the classpath appender close, leading to NoClassDefFoundError.  */
        try {
            getDiscordService().getShutdownLatch().await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // close isolated loaders for non-relocated dependencies
        getDependencyManager().close();

        // close classpath appender
        getBootstrap().getClassPathAppender().close();

        getLogger().info("Goodbye!");
    }

    // hooks called during load

    protected DependencyManager createDependencyManager() {
        return new DependencyManagerImpl(this);
    }

    protected Set<Dependency> getGlobalDependencies() {
        return EnumSet.of(
                Dependency.ADVENTURE,
                Dependency.CAFFEINE,
                Dependency.OKIO,
                Dependency.OKHTTP,
                Dependency.JACKSON_CORE,
                Dependency.JACKSON_DATABIND,
                Dependency.JACKSON_ANNOTATIONS,
                Dependency.COLLECTIONS4,
                Dependency.NEOVISIONARIES,
                Dependency.TROVE4J,
                Dependency.JDA
        );
    }

    protected abstract void setupSenderFactory();

    protected abstract ConfigurationAdapter provideConfigurationAdapter();

    protected abstract void registerCommands();

    protected abstract void setupManagers();

    protected abstract void setupPlatformHooks();

    protected abstract void performFinalSetup();

    // hooks called during disable

    protected void removePlatformHooks() {}

    protected Path resolveConfig(String fileName) {
        Path configFile = getBootstrap().getConfigDirectory().resolve(fileName);

        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                // ignore
            }

            try (InputStream is = getBootstrap().getResourceStream(fileName)) {
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    @Override
    public PluginLogger getLogger() {
        return getBootstrap().getPluginLogger();
    }

    @Override
    public DependencyManager getDependencyManager() {
        return this.dependencyManager;
    }

    @Override
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

    @Override
    public RenaConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public DiscordService getDiscordService() {
        return this.discordService;
    }

    @Override
    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public SyncTask.Buffer getSyncTaskBuffer() {
        return this.syncTaskBuffer;
    }

    public static String getPluginName() {
        LocalDate date = LocalDate.now();
        if (date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1) {
            return "RoninePowered";
        }
        return "RenaPowered";
    }
}
