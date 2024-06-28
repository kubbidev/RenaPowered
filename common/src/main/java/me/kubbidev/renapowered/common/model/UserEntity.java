package me.kubbidev.renapowered.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import org.jetbrains.annotations.Nullable;

@Entity(name = "user")
public class UserEntity implements BaseEntity {

    @JsonProperty("_id")
    private final long id;

    @Setter
    @Getter
    @JsonProperty("username")
    private @Nullable String username;

    @Setter
    @Getter
    @JsonProperty("avatar_url")
    private @Nullable String avatarUrl;

    @Setter
    @Getter
    @JsonProperty("last_seen")
    private long lastSeen = System.currentTimeMillis();

    public UserEntity(long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }
}
