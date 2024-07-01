package me.kubbidev.renapowered.common.module.ranking.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import me.kubbidev.renapowered.common.module.ranking.model.MemberVoiceState;
import me.kubbidev.renapowered.common.util.CaffeineFactory;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class GuildVoiceActivityTracker {
    private final Cache<Long, VoiceActivityTracker> voiceTrackers = CaffeineFactory.newBuilder()
            .expireAfterAccess(10, TimeUnit.DAYS)
            .build();

    public boolean isEmpty() {
        synchronized (this.voiceTrackers) {
            return this.voiceTrackers.estimatedSize() == 0L;
        }
    }

    public synchronized void add(long channelId, long userId, boolean frozen) {
        VoiceActivityTracker tracker = this.voiceTrackers.get(channelId, a -> new VoiceActivityTracker());
        if (tracker != null) {
            tracker.add(userId, frozen);
        }
    }

    public synchronized @Nullable MemberVoiceState remove(long channelId, long userId) {
        VoiceActivityTracker tracker = this.voiceTrackers.getIfPresent(channelId);
        if (tracker == null) {
            return null;
        }

        MemberVoiceState voiceState = tracker.remove(userId);
        if (tracker.isEmpty()) {
            this.voiceTrackers.invalidate(channelId);
        }
        return voiceState;
    }

    public synchronized void freeze(long userId, boolean frozen) {
        this.voiceTrackers.asMap().forEach((channelId, tracker) -> tracker.freeze(userId, frozen));
    }
}