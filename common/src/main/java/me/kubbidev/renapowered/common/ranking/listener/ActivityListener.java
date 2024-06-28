package me.kubbidev.renapowered.common.ranking.listener;

import com.github.benmanes.caffeine.cache.Cache;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.ranking.model.MemberVoiceState;
import me.kubbidev.renapowered.common.ranking.tracker.GuildVoiceActivityTracker;
import me.kubbidev.renapowered.common.util.CaffeineFactory;
import me.kubbidev.renapowered.common.worker.event.EventHandler;
import me.kubbidev.renapowered.common.worker.listener.DiscordEventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSuppressEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@EventHandler
public class ActivityListener extends DiscordEventListener {
    private final Cache<Long, GuildVoiceActivityTracker> activityTrackers = CaffeineFactory.newBuilder()
            .expireAfterAccess(10, TimeUnit.DAYS)
            .build();

    public ActivityListener(RenaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent e) {
        AudioChannel joinedChannel = e.getChannelJoined();
        AudioChannel leftChannel = e.getChannelLeft();

        if (leftChannel != null) {
            if (!e.getMember().getUser().isBot() && isChannelAllowed(leftChannel)) {
                stopRecord(leftChannel, e.getMember());
            }
        }

        if (joinedChannel != null) {
            if (!e.getMember().getUser().isBot() && isChannelAllowed(joinedChannel)) {
                startRecord(joinedChannel, e.getMember());
            }
        }
    }

    @Override
    public void onGuildVoiceMute(@NotNull GuildVoiceMuteEvent e) {
        handleGuildFreezing(e);
    }

    @Override
    public void onGuildVoiceSuppress(@NotNull GuildVoiceSuppressEvent e) {
        handleGuildFreezing(e);
    }

    private void handleGuildFreezing(GenericGuildVoiceEvent e) {
        if (e.getMember().getUser().isBot()) {
            return;
        }
        updateFreeze(e.getMember(), e.getVoiceState());
    }

    private void startRecord(AudioChannel channel, Member member) {
        Guild guild = channel.getGuild();
        GuildVoiceActivityTracker tracker = this.activityTrackers.get(guild.getIdLong(), a -> new GuildVoiceActivityTracker());

        if (tracker != null) {
            tracker.add(channel.getIdLong(), member.getIdLong(), isFrozen(member));
        }
    }

    private void stopRecord(AudioChannel channel, Member member) {
        long guildId = channel.getGuild().getIdLong();
        long memberId = member.getIdLong();

        GuildVoiceActivityTracker tracker = this.activityTrackers.getIfPresent(guildId);
        if (tracker == null) {
            return;
        }

        MemberVoiceState voiceState = tracker.remove(channel.getIdLong(), memberId);
        if (voiceState != null) {
            this.plugin.getDiscordService().getRankingService()
                    .addVoiceActivity(member, voiceState);
        }
        if (tracker.isEmpty()) {
            this.activityTrackers.invalidate(guildId);
        }
    }

    private void updateFreeze(Member member, GuildVoiceState voiceState) {
        GuildVoiceActivityTracker tracker = this.activityTrackers.getIfPresent(member.getGuild().getIdLong());
        if (tracker == null) {
            return;
        }
        tracker.freeze(member.getIdLong(), isFrozen(voiceState));
    }

    private boolean isChannelAllowed(AudioChannel channel) {
        Guild guild = channel.getGuild();
        if (channel.equals(guild.getAfkChannel())) {
            return false;
        }

        GuildEntity guildEntity = StandardGuildManager.fetch(this.plugin, guild);
        return guildEntity.isRanking();
    }

    private static boolean isFrozen(Member member) {
        return member.getVoiceState() != null && isFrozen(member.getVoiceState());
    }

    private static boolean isFrozen(GuildVoiceState voiceState) {
        return !voiceState.inAudioChannel() || voiceState.isMuted() || voiceState.isSuppressed();
    }
}
