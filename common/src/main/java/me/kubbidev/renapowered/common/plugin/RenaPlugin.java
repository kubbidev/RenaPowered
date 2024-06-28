package me.kubbidev.renapowered.common.plugin;

import me.kubbidev.renapowered.common.command.abstraction.Command;
import me.kubbidev.renapowered.common.config.RenaConfiguration;
import me.kubbidev.renapowered.common.dependencies.DependencyManager;
import me.kubbidev.renapowered.common.command.CommandManager;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.locale.TranslationManager;
import me.kubbidev.renapowered.common.model.manager.StandardUserManager;
import me.kubbidev.renapowered.common.model.manager.StandardMemberManager;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.plugin.bootstrap.RenaBootstrap;
import me.kubbidev.renapowered.common.plugin.logging.PluginLogger;
import me.kubbidev.renapowered.common.storage.Storage;
import me.kubbidev.renapowered.common.tasks.SyncTask;
import me.kubbidev.renapowered.common.worker.DiscordService;

import java.util.Collections;
import java.util.List;

/**
 * Main internal interface for RenaPowered plugins, providing the base for
 * abstraction throughout the project.
 * <p>
 * All plugin platforms implement this interface.
 */
public interface RenaPlugin {

    /**
     * Gets the bootstrap plugin instance
     *
     * @return the bootstrap plugin
     */
    RenaBootstrap getBootstrap();

    /**
     * Gets the guild manager instance for the platform
     *
     * @return the guild manager
     */
    StandardGuildManager getGuildManager();

    /**
     * Gets the user manager instance for the platform
     *
     * @return the user manager
     */
    StandardUserManager getUserManager();

    /**
     * Gets the member manager instance for the platform
     *
     * @return the member manager
     */
    StandardMemberManager getMemberManager();

    /**
     * Gets the plugin's configuration
     *
     * @return the plugin config
     */
    RenaConfiguration getConfiguration();

    /**
     * Gets the discord service handling connection from the plugin to discord.
     *
     * @return the plugin discord service
     */
    DiscordService getDiscordService();

    /**
     * Gets the primary data storage instance. This is likely to be wrapped with extra layers for caching, etc.
     *
     * @return the storage handler instance
     */
    Storage getStorage();

    /**
     * Gets a wrapped logger instance for the platform.
     *
     * @return the plugin's logger
     */
    PluginLogger getLogger();

    /**
     * Gets the command manager
     *
     * @return the command manager
     */
    CommandManager getCommandManager();

    /**
     * Gets the instance providing locale translations for the plugin
     *
     * @return the translation manager
     */
    TranslationManager getTranslationManager();

    /**
     * Gets the dependency manager for the plugin
     *
     * @return the dependency manager
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the console.
     *
     * @return the console sender of the instance
     */
    Sender getConsoleSender();

    default List<Command<?>> getExtraCommands() {
        return Collections.emptyList();
    }

    /**
     * Gets the sync task buffer of the platform, used for scheduling and running sync tasks.
     *
     * @return the sync task buffer instance
     */
    SyncTask.Buffer getSyncTaskBuffer();

    /**
     * Called at the end of the sync task.
     */
    default void performPlatformDataSync() {

    }
}