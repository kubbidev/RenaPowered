package me.kubbidev.renapowered.common.module.ranking.model;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public class MemberVoiceState {
    private final AtomicLong activityTime = new AtomicLong();
    private final AtomicDouble activityPoints = new AtomicDouble();

    @Setter
    private long lastAccumulated = System.currentTimeMillis();

    @Setter
    private boolean frozen;

    public MemberVoiceState(boolean frozen) {
        this.frozen = frozen;
    }
}