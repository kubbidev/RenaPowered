package me.kubbidev.renapowered.common.ranking.tracker;

import me.kubbidev.renapowered.common.ranking.RankingService;
import me.kubbidev.renapowered.common.ranking.model.MemberVoiceState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceActivityTracker {
    private final Map<Long, MemberVoiceState> voiceStates = new ConcurrentHashMap<>();

    public boolean isEmpty() {
        synchronized (this.voiceStates) {
            return this.voiceStates.isEmpty();
        }
    }

    public synchronized void add(long userId, boolean frozen) {
        accumulate();
        this.voiceStates.put(userId, new MemberVoiceState(frozen));
    }

    public synchronized @Nullable MemberVoiceState remove(long userId) {
        accumulate();
        return this.voiceStates.remove(userId);
    }

    public synchronized void freeze(long userId, boolean frozen) {
        accumulate();
        MemberVoiceState voiceState = this.voiceStates.get(userId);
        if (voiceState != null) {
            voiceState.setFrozen(frozen);
        }
    }

    @SuppressWarnings("ConstantValue")
    private void accumulate() {
        long currentMillis = System.currentTimeMillis();
        long speakingMembers = this.voiceStates.values().stream().filter(e -> !e.isFrozen()).count();

        int maxVoiceMembers = RankingService.MAX_VOICES;
        if (maxVoiceMembers > 0) {
            speakingMembers = Math.min(speakingMembers, maxVoiceMembers);
        }

        for (Map.Entry<Long, MemberVoiceState> entry : this.voiceStates.entrySet()) {
            MemberVoiceState voiceState = entry.getValue();

            if (!voiceState.isFrozen() && speakingMembers > 1) {
                long duration = currentMillis - voiceState.getLastAccumulated();

                voiceState.getActivityTime().addAndGet(duration);
                voiceState.getActivityPoints().addAndGet((duration / 60000.0f * speakingMembers * 0.4f));
            }
            voiceState.setLastAccumulated(currentMillis);
        }
    }
}
