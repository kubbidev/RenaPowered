package me.kubbidev.renapowered.common.model.manager;

import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.manager.abstraction.AbstractManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;

public class StandardGuildManager extends AbstractManager<Long, GuildEntity> {

    public static GuildEntity fetch(RenaPlugin plugin, Guild guild) {
        GuildEntity entity = fetch(plugin, guild.getIdLong());

        if (updateIfRequired(guild, entity)) {
            save(plugin, entity);
        }
        return entity;
    }

    public static GuildEntity fetch(RenaPlugin plugin, long guildId) {
        StandardGuildManager guildManager = plugin.getGuildManager();

        // load the entity from the local data storage first to avoid
        // useless remote database queries
        GuildEntity entity = guildManager.getIfLoaded(guildId);
        if (entity == null) {
            entity = plugin.getStorage()
                    .loadEntity(GuildEntity.class, guildId, guildManager).join();
        }

        return entity;
    }

    public static void save(RenaPlugin plugin, GuildEntity guild) {
        try {
            plugin.getStorage().saveEntity(guild).get();
        } catch (Exception e) {
            plugin.getLogger().warn("Error whilst saving guild", e);
        }
    }

    private static boolean updateIfRequired(Guild guild, GuildEntity guildEntity) {
        boolean shouldSave = false;

        if (!Objects.equals(guild.getName(), guildEntity.getName())) {
            guildEntity.setName(guild.getName());
            shouldSave = true;
        }

        if (!Objects.equals(guild.getIconUrl(), guildEntity.getIconUrl())) {
            guildEntity.setIconUrl(guild.getIconUrl());
            shouldSave = true;
        }

        return shouldSave;
    }

    @Override
    public GuildEntity apply(Long id) {
        return new GuildEntity(id);
    }
}