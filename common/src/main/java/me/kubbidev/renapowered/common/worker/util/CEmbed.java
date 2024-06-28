package me.kubbidev.renapowered.common.worker.util;

import lombok.AllArgsConstructor;
import me.kubbidev.renapowered.common.util.ComponentSerializer;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CEmbed {
    protected final List<CField> fields = new ArrayList<>(25);
    protected int color = 0x1FFFFFFF;

    protected Component title = Component.empty();
    protected String url;
    protected Component description = Component.empty();

    protected OffsetDateTime timestamp;

    protected String thumbnail;
    protected String image;

    protected CAuthor author;
    protected CFooter footer;

    private final Object[] mutex = new Object[0];

    public CEmbed title(@NotNull Component title) {
        title(title, null);
        return this;
    }

    public CEmbed title(@NotNull Component title, String url) {
        this.title = requireNonNull(title, "title");
        this.url = url;
        return this;
    }

    public CEmbed url(String url) {
        this.url = requireNonNull(url, "url");
        return this;
    }

    public CEmbed description(@NotNull Component description) {
        this.description = requireNonNull(description, "description");
        return this;
    }

    public CEmbed timestamp(@NotNull TemporalAccessor accessor) {
        this.timestamp = toOffsetDateTime(requireNonNull(accessor, "timestamp"));
        return this;
    }

    public CEmbed color(int color) {
        this.color = color;
        return this;
    }

    public CEmbed thumbnail(String url) {
        this.thumbnail = requireNonNull(url, "thumbnail");
        return this;
    }

    public CEmbed image(String url) {
        this.image = requireNonNull(url, "image");
        return this;
    }

    public CEmbed author(@NotNull Component name) {
        author(name, null);
        return this;
    }

    public CEmbed author(@NotNull Component name, String url) {
        author(name, url, null);
        return this;
    }

    public CEmbed author(@NotNull Component name, String url, String iconUrl) {
        this.author = new CAuthor(requireNonNull(name, "author"), url, iconUrl);
        return this;
    }

    public CEmbed footer(@NotNull Component name) {
        footer(name, null);
        return this;
    }

    public CEmbed footer(@NotNull Component name, String iconUrl) {
        this.footer = new CFooter(requireNonNull(name, "footer"), iconUrl);
        return this;
    }

    public CEmbed field(@NotNull Component name, @NotNull Component value, boolean inline) {
        this.fields.add(new CField(
                requireNonNull(name, "name"),
                requireNonNull(value, "value"), inline));
        return this;
    }

    public static final Component BLANK_FIELD = Component.text('\u200E');

    public CEmbed blankField(boolean inline) {
        field(BLANK_FIELD, BLANK_FIELD, inline);
        return this;
    }

    public CEmbed clearFields() {
        this.fields.clear();
        return this;
    }

    @AllArgsConstructor
    public static class CFooter {
        private final Component text;
        private final String iconUrl;
    }

    @AllArgsConstructor
    public static class CAuthor {
        private final Component name;
        private final String url;
        private final String iconUrl;
    }

    @AllArgsConstructor
    public static class CField {
        private final Component name;
        private final Component value;
        private final boolean inline;
    }

    public MessageEmbed build(@Nullable Locale locale) {
        synchronized (this.mutex) {
            Function<Component, String> toString = c -> ComponentSerializer.serialize(c, locale);

            String t = toString.apply(this.title);
            String d = toString.apply(this.description);
            String a = toString.apply(this.author.name);
            String f = toString.apply(this.footer.text);

            return EntityBuilder.createMessageEmbed(this.url, t, d, EmbedType.RICH,
                    this.timestamp,
                    this.color,
                    new MessageEmbed.Thumbnail(this.thumbnail, null, 0, 0), null,
                    new MessageEmbed.AuthorInfo(a, this.author.url, this.author.iconUrl, null), null,
                    new MessageEmbed.Footer(f, this.footer.iconUrl, null),
                    new MessageEmbed.ImageInfo(this.image, null, 0, 0),
                    new LinkedList<>(this.fields.stream().map(e -> new MessageEmbed.Field(
                            toString.apply(e.name),
                            toString.apply(e.value), e.inline)).collect(Collectors.toList())
                    ));
        }
    }

    private static OffsetDateTime toOffsetDateTime(TemporalAccessor accessor) {
        if (accessor instanceof OffsetDateTime) {
            return (OffsetDateTime) accessor;
        } else {
            ZoneOffset offset;
            try {
                offset = ZoneOffset.from(accessor);
            } catch (DateTimeException ignore) {
                offset = ZoneOffset.UTC;
            }

            try {
                LocalDateTime ldt = LocalDateTime.from(accessor);
                return OffsetDateTime.of(ldt, offset);
            } catch (DateTimeException ignore) {
                try {
                    Instant instant = Instant.from(accessor);
                    return OffsetDateTime.ofInstant(instant, offset);
                } catch (DateTimeException e) {
                    throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: "
                            + accessor.getClass().getSimpleName(), e);
                }
            }
        }
    }
}
