package me.kubbidev.renapowered.common.tasks;

import me.kubbidev.renapowered.common.cache.BufferedRequest;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;

import java.util.concurrent.TimeUnit;

/**
 * System wide sync task for RenaPowered.
 *
 * <p>Ensures that all local data is consistent with the storage.</p>
 */
public class SyncTask implements Runnable {
    private final RenaPlugin plugin;

    public SyncTask(RenaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the update task
     *
     * <p>Called <b>async</b>.</p>
     */
    @Override
    public void run() {
        // Reload all online guilds.
        this.plugin.getStorage().loadAllEntities(GuildEntity.class,
                plugin.getGuildManager()).join();

        // Reload all online users.
        this.plugin.getStorage().loadAllEntities(UserEntity.class,
                plugin.getUserManager()).join();

        // Reload all online members.
        this.plugin.getStorage().loadAllEntities(MemberEntity.class,
                plugin.getMemberManager()).join();
        
        this.plugin.performPlatformDataSync();
    }

    public static class Buffer extends BufferedRequest<Void> {
        private final RenaPlugin plugin;

        public Buffer(RenaPlugin plugin) {
            super(500L, TimeUnit.MILLISECONDS, plugin.getBootstrap().getScheduler());
            this.plugin = plugin;
        }

        @Override
        protected Void perform() {
            new SyncTask(this.plugin).run();
            return null;
        }
    }
}