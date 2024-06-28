package me.kubbidev.renapowered.common.model;

import lombok.Getter;
import lombok.Setter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entry;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import org.jetbrains.annotations.Nullable;

@Entity(name = "user")
public class UserEntity implements BaseEntity {

    @Entry(name = "_id")
    private final long id;

    @Setter
    @Getter
    @Entry(name = "username")
    private @Nullable String username;

    @Setter
    @Getter
    @Entry(name = "avatar_url")
    private @Nullable String avatarUrl;

    @Setter
    @Getter
    @Entry(name = "last_seen")
    private long lastSeen = System.currentTimeMillis();

    public UserEntity(long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }
}
