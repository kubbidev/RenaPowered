package me.kubbidev.renapowered.common.ranking.util;

import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.ranking.RankingService;
import me.kubbidev.renapowered.common.worker.util.Emote;
import me.kubbidev.renapowered.common.worker.util.CEmbed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code RankingBuilder} class is used to build a ranking leaderboard.
 * It provides methods to add entries to the leaderboard and generate an embed
 * representing the leaderboard.
 */
public class RankingBuilder {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    private final static int EMBED_COLOR = 0xffe193;
    public final static int MAX_ENTRIES = 16;

    private final List<Entry> entries = new ArrayList<>(MAX_ENTRIES);
    private final RankingService rankingService;

    /**
     * Constructs a new {@code RankingBuilder} with the specified {@code RankingService}.
     *
     * @param rankingService the ranking service to use for building the leaderboard
     */
    public RankingBuilder(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Adds an entry to the leaderboard.
     *
     * @param entry the entry to add
     */
    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    /**
     * Builds the leaderboard as a {@code CEmbed} object.
     *
     * @return the built {@code CEmbed} representing the leaderboard
     */
    public CEmbed build() {
        CEmbed embed = new CEmbed();
        embed.author(Message.LEADERBOARD_AUTHOR.build());
        embed.thumbnail("https://static.wikia.nocookie.net/gensin-impact/images/1/17/Achievement_Wonders_of_the_World.png");
        embed.color(EMBED_COLOR);

        LocalDateTime nextDate = this.rankingService.getNextScheduleDate();
        LocalDateTime pastDate = nextDate.minus(
                this.rankingService.getScheduleSettings().duration(),
                this.rankingService.getScheduleSettings().unit().toChronoUnit()
        );

        embed.title(Message.LEADERBOARD_TITLE.build(nextDate, pastDate));
        embed.field(CEmbed.BLANK_FIELD, createNameField(), true);
        embed.field(CEmbed.BLANK_FIELD, createExperienceField(), true);
        embed.field(CEmbed.BLANK_FIELD, createLevelField(), true);
        embed.field(CEmbed.BLANK_FIELD, createNextUpdateField(), false);
        return embed;
    }

    /**
     * Represents an entry in the leaderboard.
     *
     * @param name          the name of the entry
     * @param experience    the experience points of the entry
     * @param placement     the current placement of the entry
     * @param lastPlacement the last placement of the entry
     */
    public record Entry(String name, long experience, int placement, int lastPlacement) {

    }

    private Component createNextUpdateField() {
        Instant nextMonday = this.rankingService.getNextScheduleDate()
                .atZone(ZoneId.systemDefault()).toInstant();

        return Message.LEADERBOARD_NEXT_UPDATE.build(nextMonday);
    }

    private Component createLevelField() {
        TextComponent.Builder builder = Component.text();
        this.entries.forEach(entry -> {
            builder.append(Message.LEADERBOARD_LEVEL_FIELD.build(RankingUtil.getLevelFromExp(entry.experience)));
            builder.append(Component.newline());
        });
        return builder.build();
    }

    private Component createExperienceField() {
        TextComponent.Builder builder = Component.text();
        for (Entry entry : this.entries) {
            builder.append(Message.LEADERBOARD_EXPERIENCE_FIELD.build(formatExperience(entry.experience)));
            builder.append(Component.newline());
        }
        return builder.build();
    }

    private Component createNameField() {
        TextComponent.Builder builder = Component.text();
        for (Entry entry : this.entries) {
            Emote emoteIcon = Emote.EQUAL;

            int placement = entry.placement;
            int lastPlace = entry.lastPlacement;
            if (placement > lastPlace) {
                emoteIcon = Emote.RED_TRIANGLE;
            } else if (placement < lastPlace) {
                emoteIcon = Emote.GREEN_TRIANGLE;
            }

            builder.append(Message.LEADERBOARD_NAME_FIELD.build(emoteIcon, placement, truncateString(entry.name, 12)));
            builder.append(Component.newline());
        }
        return builder.build();
    }

    /**
     * Truncates the given string to the specified maximum length, adding "..." if it exceeds that length.
     *
     * @param message  the string to truncate
     * @param maxLength the maximum length of the string
     * @return the truncated string
     */
    public static String truncateString(String message, int maxLength) {
        return message.length() > maxLength
                ? message.substring(0, maxLength) + "..."
                : message;
    }

    /**
     * Formats the given experience value, appending "K" if the value is 1000 or more.
     *
     * @param experience the experience value to format
     * @return the formatted experience string
     */
    public static String formatExperience(long experience) {
        if (Math.abs(experience) >= 1000L) {
            double formattedValue = (double) experience / 1000.0;
            return DECIMAL_FORMAT.format(formattedValue) + "K";
        } else {
            return String.valueOf(experience);
        }
    }
}
