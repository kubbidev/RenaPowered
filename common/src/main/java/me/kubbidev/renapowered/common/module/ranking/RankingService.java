package me.kubbidev.renapowered.common.module.ranking;

import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.model.manager.StandardMemberManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.module.ranking.model.MemberVoiceState;
import me.kubbidev.renapowered.common.module.ranking.util.RankingBuilder;
import me.kubbidev.renapowered.common.util.ExpiringSet;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import me.kubbidev.renapowered.common.util.ScheduledTask;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RankingService extends ScheduledTask implements AutoCloseable {
    // constant used to rule the ranking system
    public static final int MAX_VOICES = 0;

    // multiplier coefficient used to scale the experience given to members
    private static final int TEXT_MULTIPLIER = 1;
    private static final int VOICE_MULTIPLIER = 1;

    // messages cleared before posting the new ranking leaderboard
    private static final int MESSAGES_TO_DELETE = 10;

    private final RenaPlugin plugin;
    private final ExpiringSet<UUID> cooldowns = new ExpiringSet<>(1, TimeUnit.MINUTES);

    private final Random random = new Random();

    public RankingService(RenaPlugin plugin) {
        super(plugin.getBootstrap().getScheduler(), scheduleSettings(7, TimeUnit.DAYS));
        this.plugin = plugin;
    }

    @Override
    public void whenScheduled() {

    }

    @InitialLocalDay(dayOfWeek = DayOfWeek.MONDAY)
    @InitialLocalTime(hour = 18, minute = 0, second = 0)
    @Override
    public void run() {
        Collection<? extends GuildEntity> entities =
                this.plugin.getGuildManager().getAll().values();

        for (GuildEntity guildEntity : entities) {
            // avoid provide a leaderboard printer for guild that don't have the bot anymore
            Guild guild = this.plugin.getDiscordService().getShardManager().getGuildById(guildEntity.getId());
            if (guild != null) {
                render(guild, guildEntity);
            }
        }
    }

    @Override
    public void close() {
        cancel();
    }

    public void onMessage(MessageReceivedEvent e) {
        GuildEntity guildEntity = StandardGuildManager.fetch(this.plugin, e.getGuild());
        if (!guildEntity.isRanking()) {
            return;
        }

        Member member = Objects.requireNonNull(e.getMember(), "member");
        // compute member uuid from user and guild ids
        UUID uuid = new UUID(
                member.getUser().getIdLong(),
                member.getGuild().getIdLong()
        );

        if (this.cooldowns.contains(uuid)) {
            return;
        }
        this.cooldowns.add(uuid);
        MemberEntity memberEntity = StandardMemberManager.fetch(this.plugin, member);

        long gainedExp = random.nextLong(15, 25) * TEXT_MULTIPLIER;
        long totalExp = memberEntity.getExperience() + gainedExp;

        memberEntity.setExperience(totalExp);
        StandardMemberManager.save(this.plugin, memberEntity);
    }

    public void addVoiceActivity(Member member, MemberVoiceState state) {
        GuildEntity guildEntity = StandardGuildManager.fetch(this.plugin, member.getGuild());
        if (!guildEntity.isRanking()) {
            return;
        }
        MemberEntity memberEntity = StandardMemberManager.fetch(this.plugin, member);

        long gainedExp = (long) (15 * state.getActivityPoints().get() * VOICE_MULTIPLIER);
        long totalExp = memberEntity.getExperience() + gainedExp;

        long gainedActivity = state.getActivityTime().get();
        long voiceActivity = memberEntity.getVoiceActivity() + gainedActivity;

        memberEntity.setVoiceActivity(voiceActivity);
        memberEntity.setExperience(totalExp);

        StandardMemberManager.save(this.plugin, memberEntity);
    }

    public void render(Guild guild, GuildEntity guildEntity) {
        if (!guildEntity.isRanking()) {
            return;
        }
        GuildMessageChannel channel = fetchRankingChannel(guild, guildEntity);
        if (channel == null) {
            return;
        }

        MemberEntity[] members = getGuildMembersArray(guild);
        if (members.length == 0) {
            return;
        }

        RankingBuilder printer = new RankingBuilder(this);

        populatePrinter(members, printer);
        sendLeaderboard(channel, printer);
    }

    private void populatePrinter(MemberEntity[] entries, RankingBuilder printer) {
        for (int i = 0; i < entries.length; i++) {
            MemberEntity memberEntity = entries[i];

            int currentPlacement = i + 1;
            int previousPlacement = determinePreviousPlacement(memberEntity);

            printer.addEntry(new RankingBuilder.Entry(
                    memberEntity.getEffectiveName(),
                    memberEntity.getExperience(),
                    currentPlacement,
                    previousPlacement
            ));
            if (previousPlacement != currentPlacement) {
                memberEntity.setPreviousPlacement(currentPlacement);

                StandardMemberManager.save(this.plugin, memberEntity);
            }
        }
    }

    private int determinePreviousPlacement(MemberEntity memberEntity) {
        int previousPlacement = memberEntity.getPreviousPlacement();
        if (previousPlacement < 0) {
            previousPlacement = RankingBuilder.MAX_ENTRIES + 1;
        }
        return previousPlacement;
    }

    private MemberEntity[] getGuildMembersArray(Guild guild) {
        return this.plugin.getMemberManager().getAll().values().stream()
                // verify if the current member is part of the current guild being executed
                .filter(r -> r.getId().getLeastSignificantBits() == guild.getIdLong())
                .sorted((o1, o2) -> Long.compare(
                        o2.getExperience(),
                        o1.getExperience()
                ))
                .limit(RankingBuilder.MAX_ENTRIES)
                .toArray(MemberEntity[]::new);
    }

    private void sendLeaderboard(GuildMessageChannel channel, RankingBuilder printer) {
        channel.getIterableHistory().takeAsync(MESSAGES_TO_DELETE).thenAccept(messages ->
                        channel.purgeMessages(messages.stream()
                                .filter(message -> !message.isPinned())
                                .filter(message -> !message.isEphemeral()).collect(ImmutableCollectors.toList())
                        ))
                .thenCompose(u -> postLeaderboard(channel, printer));
    }

    private CompletableFuture<Void> postLeaderboard(GuildMessageChannel channel, RankingBuilder printer) {
        // get the guild locale, return the guild community locale, otherwise
        // fallback to english if the guild is not a community
        Locale guildLocale = channel.getGuild().getLocale().toLocale();

        return this.plugin.getDiscordService().sendMessage(channel, printer.build(), guildLocale)
                .thenAccept(m -> {
                    // publish the message after sending it automatically
                    if (channel instanceof NewsChannel) {
                        ((NewsChannel) channel).crosspostMessageById(m.getId()).queue();
                    }
                });
    }

    private @Nullable GuildMessageChannel fetchRankingChannel(Guild guild, GuildEntity guildEntity) {
        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, guildEntity.getRankingChannel());
        if (channel == null) {
            if (guildEntity.getRankingChannel() != 0) {
                guildEntity.setRankingChannel(0);
                // if could not find the channel just delete it from the config (0)
                StandardGuildManager.save(this.plugin, guildEntity);
            }
        }
        return channel;
    }
}
