package me.kubbidev.renapowered.common.model.manager;

import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.model.manager.abstraction.AbstractManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.dv8tion.jda.api.entities.User;

import java.util.Objects;

public class StandardUserManager extends AbstractManager<Long, UserEntity> {

    public static UserEntity fetch(RenaPlugin plugin, User user) {
        UserEntity entity = fetch(plugin, user.getIdLong());

        if (updateIfRequired(user, entity)) {
            save(plugin, entity);
        }
        return entity;
    }

    public static UserEntity fetch(RenaPlugin plugin, long userId) {
        StandardUserManager userManager = plugin.getUserManager();

        // load the entity from the local data storage first to avoid
        // useless remote database queries
        UserEntity entity = userManager.getIfLoaded(userId);
        if (entity == null) {
            entity = plugin.getStorage()
                    .loadEntity(UserEntity.class, userId, userManager).join();
        }

        return entity;
    }

    public static void save(RenaPlugin plugin, UserEntity user) {
        try {
            plugin.getStorage().saveEntity(user).get();
        } catch (Exception e) {
            plugin.getLogger().warn("Error whilst saving user", e);
        }
    }

    private static boolean updateIfRequired(User user, UserEntity userEntity) {
        boolean shouldSave = false;

        if (!Objects.equals(user.getName(), userEntity.getUsername())) {
            userEntity.setUsername(user.getName());
            shouldSave = true;
        }

        if (!Objects.equals(user.getAvatarUrl(), userEntity.getAvatarUrl())) {
            userEntity.setAvatarUrl(user.getAvatarUrl());
            shouldSave = true;
        }
        return shouldSave;
    }

    @Override
    public UserEntity apply(Long id) {
        return new UserEntity(id);
    }
}