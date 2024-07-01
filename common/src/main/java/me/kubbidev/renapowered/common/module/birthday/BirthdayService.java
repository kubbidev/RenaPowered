package me.kubbidev.renapowered.common.module.birthday;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.AbstractRenaPlugin;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import me.kubbidev.renapowered.common.util.ScheduledTask;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class BirthdayService extends ScheduledTask implements AutoCloseable {
    private static final long BIRTHDAY_GUILD   = 935184905644683284L;
    private static final long BIRTHDAY_CHANNEL = 935184908480028684L;

    private static final String BIRTHDAY_PREFIX = "\uD83C\uDF89 ";
    private final RenaPlugin plugin;

    public BirthdayService(RenaPlugin plugin) {
        super(plugin.getBootstrap().getScheduler(), scheduleSettings(1, TimeUnit.DAYS));
        this.plugin = plugin;
    }

    @Override
    public void whenScheduled() {
        Set<User> usersCelebrating = mapBirthdayUsers(getBirthdaysOf(LocalDate.now()));
        renderActivity(usersCelebrating);
    }

    @InitialLocalTime(hour = 0, minute = 0, second = 0)
    @Override
    public void run() {
        Set<User> usersCelebrating = mapBirthdayUsers(getBirthdaysOf(LocalDate.now()));
        renderActivity(usersCelebrating);

        if (usersCelebrating.isEmpty()) {
            return;
        }

        ShardManager shardManager = this.plugin.getDiscordService().getShardManager();
        if (shardManager == null) {
            return;
        }

        Guild guild = shardManager.getGuildById(BIRTHDAY_GUILD);
        if (guild == null) {
            return;
        }

        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, BIRTHDAY_CHANNEL);
        if (channel == null) {
            return;
        }

        // collect all users as mention into a single string
        String mentions = usersCelebrating.stream()
                .map(IMentionable::getAsMention).collect(Collectors.joining(","));

        // when everything is ready, send the happy birthday message
        this.plugin.getDiscordService()
                .sendMessage(channel, Message.BIRTHDAY_CELEBRATION.build(mentions), guild.getLocale().toLocale())
                .thenAccept(message -> message.addReaction(Emoji.fromUnicode("U+1F382")).queue());
    }

    @Override
    public void close() {
        cancel();
    }

    @AllArgsConstructor
    public enum Birthday {
        YAYOU    (18, 2,  637522127452373003L),
        LUXUROW  (28, 2,  469945103608446976L),
        SPARTIAT (5,  4,  536180603461173252L),
        RENA     (9,  4,  904281685607211028L),
        KUBBI    (20, 4,  537669034137616384L),
        TOMTOUMI (30, 7,  703286722884141076L),
        GABZ     (14, 8,  509439886433189888L),
        NOMISSAKA(28, 8,  750332620935528448L),
        HDZL     (8,  10, 854702147270344725L),
        NOLUXIS  (29, 10, 876734806262677534L),
        GEDEON   (6,  11, 630112009458810927L),
        LIEBE    (12, 11, 769848994410659870L);

        private final int day;
        private final int month;
        private final long userId;
    }

    public void renderActivity(Set<User> birthdays) {
        Activity activity = Activity.playing(AbstractRenaPlugin.getPluginName());

        if (!birthdays.isEmpty()) {
            String formattedBirthdays = birthdays.stream().map(User::getEffectiveName)
                    .collect(Collectors.joining(", ", BIRTHDAY_PREFIX, "'s birthday"));

            activity = Activity.customStatus(formattedBirthdays);
        }
        this.plugin.getDiscordService().renderActivity(activity);
    }

    private Set<Birthday> getBirthdaysOf(LocalDate localDate) {
        int monthValue = localDate.getMonthValue();
        int dayOfMonth = localDate.getDayOfMonth();

        return Arrays.stream(Birthday.values()).filter(birthday -> birthday.month == monthValue && birthday.day == dayOfMonth)
                .collect(ImmutableCollectors.toSet());
    }

    private Set<User> mapBirthdayUsers(Set<Birthday> birthdays) {
        Set<User> users = new HashSet<>(birthdays.size());

        for (Birthday birthday : birthdays) {
            User user = this.plugin.getDiscordService().getShardManager().getUserById(birthday.userId);
            if (user == null) {
                this.plugin.getLogger().warn("Could not celebrate: '" + birthday.userId + "' birthday");
                continue;
            }
            users.add(user);
        }
        return users;
    }
}
