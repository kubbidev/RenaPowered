package me.kubbidev.renapowered.common.model;

import lombok.Getter;
import lombok.Setter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entry;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Entity(name = "member")
public class MemberEntity implements BaseEntity {

    @Entry(name = "uuid")
    private final UUID id;

    @Getter
    @Setter
    @Entry(name = "effective_name")
    private @Nullable String effectiveName;

    @Setter
    @Getter
    @Entry(name = "experience")
    private long experience;

    @Setter
    @Getter
    @Entry(name = "voice_activity")
    private long voiceActivity;

    @Setter
    @Getter
    @Entry(name = "previous_placement")
    private int previousPlacement = -1;

    public MemberEntity(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return this.id;
    }
}
