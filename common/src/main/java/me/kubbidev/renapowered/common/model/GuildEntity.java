package me.kubbidev.renapowered.common.model;

import lombok.Getter;
import lombok.Setter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entry;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import org.jetbrains.annotations.Nullable;

@Entity(name = "guild")
public class GuildEntity implements BaseEntity {

    @Entry(name = "_id")
    private final long id;

    @Setter
    @Getter
    @Entry(name = "name")
    private @Nullable String name;

    @Setter
    @Getter
    @Entry(name = "icon_url")
    private @Nullable String iconUrl;

    @Setter
    @Getter
    @Entry(name = "ranking")
    private boolean ranking;

    @Setter
    @Getter
    @Entry(name = "ranking_channel")
    private long rankingChannel;

    @Setter
    @Getter
    @Entry(name = "suggest_channel")
    private long suggestChannel;

    public GuildEntity(long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }
}
