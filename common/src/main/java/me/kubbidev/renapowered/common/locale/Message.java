package me.kubbidev.renapowered.common.locale;

import me.kubbidev.renapowered.common.plugin.AbstractRenaPlugin;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.plugin.bootstrap.RenaBootstrap;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.storage.StorageMetadata;
import me.kubbidev.renapowered.common.worker.util.Emote;
import me.kubbidev.renapowered.common.util.DurationFormatter;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.ApiStatus;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

/**
 * A collection of formatted messages used by the application.
 */
@ApiStatus.Experimental
public interface Message {

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd '@' HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    TextComponent OPEN_BRACKET = text('(');
    TextComponent CLOSE_BRACKET = text(')');
    TextComponent FULL_STOP = text('.');

    Component PREFIX_COMPONENT = text()
            .color(GRAY)
            .append(text('['))
            .append(text()
                    .decoration(BOLD, true)
                    .append(text('R', AQUA))
                    .append(text('P', DARK_AQUA))
            )
            .append(text(']'))
            .build();

    static TextComponent prefixed(ComponentLike component) {
        return text()
                .append(PREFIX_COMPONENT)
                .append(space())
                .append(component)
                .build();
    }

    Args1<RenaBootstrap> STARTUP_BANNER = bootstrap -> {
        Component infoLine1 = text()
                .append(text(AbstractRenaPlugin.getPluginName(), DARK_GREEN))
                .append(space())
                .append(text("v" + bootstrap.getVersion(), AQUA))
                .build();

        Component infoLine2 = text()
                .color(DARK_GRAY)
                .append(text("Running on "))
                .append(text(bootstrap.getType().getFriendlyName()))
                .append(text(" - "))
                .append(text(bootstrap.getServerBrand()))
                .build();

        // "   __   __    "
        // "  |__) |__)   "
        // "  |  \ |      "

        return joinNewline(
                text()
                        .append(text("   __  ", AQUA))
                        .append(text(" __    ", DARK_AQUA))
                        .build(),
                text()
                        .append(text("  |__) ", AQUA))
                        .append(text("|__)   ", DARK_AQUA))
                        .append(infoLine1)
                        .build(),
                text()
                        .append(text("  |  \\ ", AQUA))
                        .append(text("|      ", DARK_AQUA))
                        .append(infoLine2)
                        .build(),
                empty()
        );
    };

    Args1<String> BIRTHDAY_CELEBRATION = mentions -> text()
            // @everyone Today is a special day because it's {}'s birthday! :birthday:
            // https://tenor.com/view/happy-birthday-wishes-gif-17510075890249401150
            .append(text("@everyone"))
            .append(space())
            .append(translatable("renapowered.activitysystem.birthday.celebration", text(mentions)))
            // let's add a custom cake emoji for fun!
            .append(space())
            .append(text(Emote.BIRTHDAY.toString()))
            .append(newline())
            .append(text("https://tenor.com/view/happy-birthday-wishes-gif-17510075890249401150"))
            .build();

    Args1<String> VIEW_AVAILABLE_COMMANDS_PROMPT = label -> prefixed(translatable()
            // "&3Use &a/{} help &3to view available commands."
            .key("renapowered.commandsystem.available-commands")
            .color(DARK_AQUA)
            .args(text('/' + label + " help", GREEN))
            .append(FULL_STOP)
    );

    Args0 NO_PERMISSION_FOR_SUBCOMMANDS = () -> prefixed(translatable()
            // "&3You do not have permission to use any sub commands."
            .key("renapowered.commandsystem.no-permission-subcommands")
            .color(DARK_AQUA)
            .append(FULL_STOP)
    );

    Args0 ALREADY_EXECUTING_COMMAND = () -> prefixed(translatable()
            // "&7Another command is being executed, waiting for it to finish..."
            .key("renapowered.commandsystem.already-executing-command")
            .color(GRAY)
    );

    Args0 COMMAND_EXCEPTION_TRACE_MESSAGE = () -> text()
            // "&cException whilst executing command:"
            .color(RED)
            .append(text("**"))
            .append(translatable("renapowered.commandsystem.trace.message"))
            .append(text(":**"))
            .build();

    Args1<Integer> COMMAND_EXCEPTION_TRACE_OVERFLOW = overflow -> text()
            // "&f... and {} more"
            .color(WHITE)
            .content("... ")
            .append(translatable()
                    .key("renapowered.commandsystem.trace.overflow")
                    .args(text(overflow))
            )
            .build();

    Args1<String> EXPORT_LOG = msg -> prefixed(text()
            // "&3EXPORT &3&l> &f{}"
            .append(translatable("renapowered.logs.export-prefix", DARK_AQUA))
            .append(space())
            .append(text('>', DARK_AQUA, BOLD))
            .append(space())
            .append(text(msg, WHITE))
    );

    Args1<String> EXPORT_LOG_PROGRESS = msg -> prefixed(text()
            // "&3EXPORT &3&l> &7{}"
            .append(translatable("renapowered.logs.export-prefix", DARK_AQUA))
            .append(space())
            .append(text('>', DARK_AQUA, BOLD))
            .append(space())
            .append(text(msg, GRAY))
    );

    Args0 COMMAND_NOT_RECOGNISED = () -> prefixed(translatable()
            // "&cCommand not recognised."
            .key("renapowered.commandsystem.command-not-recognised")
            .color(RED)
            .append(FULL_STOP)
    );

    Args2<String, Component> COMMAND_USAGE_DETAILED_HEADER = (name, usage) -> joinNewline(
            // "&3&lCommand Usage &3- &b{}"
            // "&b> &7{}"
            prefixed(text()
                    .append(translatable("renapowered.commandsystem.usage.usage-header", DARK_AQUA, BOLD))
                    .append(text(" - ", DARK_AQUA))
                    .append(text(name, AQUA))),
            prefixed(text()
                    .append(text('>', AQUA))
                    .append(space())
                    .append(text().color(GRAY).append(usage)))
    );

    Args0 COMMAND_USAGE_DETAILED_ARGS_HEADER = () -> prefixed(translatable()
            // "&3Arguments:"
            .key("renapowered.commandsystem.usage.arguments-header")
            .color(DARK_AQUA)
            .append(text(':'))
    );

    Args2<Component, Component> COMMAND_USAGE_DETAILED_ARG = (arg, usage) -> prefixed(text()
            // "&b- {}&3 -> &7{}"
            .append(text('-', AQUA))
            .append(space())
            .append(arg)
            .append(text(" -> ", DARK_AQUA))
            .append(text().color(GRAY).append(usage))
    );

    Args1<String> REQUIRED_ARGUMENT = name -> text()
            .color(DARK_GRAY)
            .append(text('<'))
            .append(text(name, GRAY))
            .append(text('>'))
            .build();

    Args1<String> OPTIONAL_ARGUMENT = name -> text()
            .color(DARK_GRAY)
            .append(text('['))
            .append(text(name, GRAY))
            .append(text(']'))
            .build();

    Args0 EXPORT_ALREADY_RUNNING = () -> prefixed(text()
            // "&cAnother export process is already running. Please wait for it to finish and try again."
            .color(RED)
            .append(translatable("renapowered.command.export.already-running"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("renapowered.command.misc.wait-to-finish"))
            .append(FULL_STOP)
    );

    Args1<String> FILE_NOT_WITHIN_DIRECTORY = file -> prefixed(text()
            // "&cError: File &4{}&c must be a direct child of the data directory."
            .color(RED)
            .append(translatable("renapowered.command.import.error-term"))
            .append(text(": "))
            .append(translatable("renapowered.command.misc.file-must-be-in-data", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_FILE_ALREADY_EXISTS = file -> prefixed(text()
            // "&cError: File &4{}&c already exists."
            .color(RED)
            .append(translatable("renapowered.command.export.error-term"))
            .append(text(": "))
            .append(translatable("renapowered.command.export.file.already-exists", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_FILE_NOT_WRITABLE = file -> prefixed(text()
            // "&cError: File &4{}&c is not writable."
            .color(RED)
            .append(translatable("renapowered.command.export.error-term"))
            .append(text(": "))
            .append(translatable("renapowered.command.export.file.not-writable", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args0 EXPORT_FILE_FAILURE = () -> prefixed(translatable()
            // "&cAn unexpected error occured whilst writing to the file."
            .key("renapowered.command.export.file-unexpected-error-writing")
            .color(RED)
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_FILE_SUCCESS = file -> prefixed(translatable()
            // "&aSuccessfully exported to &b{}&a."
            .key("renapowered.command.export.file.success")
            .color(GREEN)
            .args(text(file, AQUA))
            .append(FULL_STOP)
    );

    Args0 IMPORT_ALREADY_RUNNING = () -> prefixed(text()
            // "&cAnother import process is already running. Please wait for it to finish and try again."
            .color(RED)
            .append(translatable("renapowered.command.import.already-running"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("renapowered.command.misc.wait-to-finish"))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_FILE_DOESNT_EXIST = file -> prefixed(text()
            // "&cError: File &4{}&c does not exist."
            .color(RED)
            .append(translatable("renapowered.command.import.error-term"))
            .append(text(": "))
            .append(translatable("renapowered.command.import.file.doesnt-exist", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_FILE_NOT_READABLE = file -> prefixed(text()
            // "&cError: File &4{}&c is not readable."
            .color(RED)
            .append(translatable("renapowered.command.import.error-term"))
            .append(text(": "))
            .append(translatable("renapowered.command.import.file.not-readable", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args0 IMPORT_FILE_READ_FAILURE = () -> prefixed(text()
            // "&cAn unexpected error occured whilst reading from the import file. (is it the correct format?)"
            .color(RED)
            .append(translatable("renapowered.command.import.file.unexpected-error-reading"))
            .append(FULL_STOP)
            .append(space())
            .append(text()
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.import.file.correct-format"))
                    .append(CLOSE_BRACKET)
            )
    );

    Args3<Integer, Integer, Integer> IMPORT_PROGRESS = (percent, processed, total) -> prefixed(text()
            // "&b(Import) &b-> &f{}&f% complete &7- &b{}&f/&b{} &foperations complete."
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(translatable("renapowered.command.import.progress.percent", WHITE, text(percent)))
            .append(text(" - ", GRAY))
            .append(translatable()
                    .key("renapowered.command.import.progress.operations")
                    .color(WHITE)
                    .args(text(processed, AQUA), text(total, AQUA))
                    .append(FULL_STOP)
            )
    );

    Args0 IMPORT_START = () -> prefixed(text()
            // "&b(Import) &b-> &fStarting import process."
            .color(WHITE)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(translatable("renapowered.command.import.starting"))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_INFO = msg -> prefixed(text()
            // "&b(Import) &b-> &f{}."
            .color(WHITE)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(text(msg))
            .append(FULL_STOP)
    );

    Args1<Double> IMPORT_END_COMPLETE = seconds -> prefixed(text()
            // "&b(Import) &a&lCOMPLETED &7- took &b{} &7seconds."
            .color(GRAY)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(space())
            .append(translatable("renapowered.command.import.completed", GREEN, BOLD))
            .append(text(" - "))
            .append(translatable("renapowered.command.import.duration", text(seconds, AQUA)))
            .append(FULL_STOP)
    );


    Args0 UPDATE_TASK_REQUEST = () -> prefixed(translatable()
            // "&bAn update task has been requested. Please wait..."
            .color(AQUA)
            .key("renapowered.command.update-task.request")
            .append(FULL_STOP)
    );

    Args0 UPDATE_TASK_COMPLETE = () -> prefixed(translatable()
            // "&aUpdate task complete."
            .color(GREEN)
            .key("renapowered.command.update-task.complete")
            .append(FULL_STOP)
    );

    Args0 RELOAD_CONFIG_SUCCESS = () -> prefixed(translatable()
            // "&aThe configuration file was reloaded. &7(some options will only apply after the server has restarted)"
            .key("renapowered.command.reload-config.success")
            .color(GREEN)
            .append(FULL_STOP)
            .append(space())
            .append(text()
                    .color(GRAY)
                    .append(OPEN_BRACKET)
                    .append(translatable("renapowered.command.reload-config.restart-note"))
                    .append(CLOSE_BRACKET)
            )
    );

    Args2<RenaPlugin, StorageMetadata> INFO = (plugin, storageMeta) -> joinNewline(
            // "&2Running &bRenaPowered v{}&2 by &bkubbidev&2."
            // "&f-  &3Platform: &f{}"
            // "&f-  &3Server Brand: &f{}"
            // "&f-  &3Server Version:"
            // "     &f{}"
            // "&f-  &bStorage:"
            // "     &3Type: &f{}"
            // "     &3Some meta value: {}"
            // "&f-  &bInstance:"
            // "     &3Uptime: &7{}"
            // "     &3Local Data: &a{} &7guilds, &a{} &7users, &a{} &7members",
            prefixed(translatable()
                    .key("renapowered.command.info.running-plugin")
                    .color(DARK_GREEN)
                    .append(space())
                    .append(text(AbstractRenaPlugin.getPluginName(), AQUA))
                    .append(space())
                    .append(text("v" + plugin.getBootstrap().getVersion(), AQUA))
                    .append(text(" by "))
                    .append(text("kubbidev", AQUA))
                    .append(FULL_STOP)),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("renapowered.command.info.platform-key"))
                    .append(text(": "))
                    .append(text(plugin.getBootstrap().getType().getFriendlyName(), WHITE))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("renapowered.command.info.server-brand-key"))
                    .append(text(": "))
                    .append(text(plugin.getBootstrap().getServerBrand(), WHITE))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("renapowered.command.info.server-version-key"))
                    .append(text(':'))),
            prefixed(text()
                    .color(WHITE)
                    .append(text("     "))
                    .append(text(plugin.getBootstrap().getServerVersion()))),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("renapowered.command.info.storage-key"))
                    .append(text(':'))),
            prefixed(text()
                    .apply(builder -> {
                        builder.append(text()
                                .color(DARK_AQUA)
                                .append(text("     "))
                                .append(translatable("renapowered.command.info.storage-type-key"))
                                .append(text(": "))
                                .append(text(plugin.getStorage().getName(), WHITE))
                        );

                        if (storageMeta.connected() != null) {
                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("renapowered.command.info.storage.meta.connected-key"))
                                    .append(text(": "))
                                    .append(formatBoolean(storageMeta.connected()))
                            ));
                        }

                        if (storageMeta.ping() != null) {
                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("renapowered.command.info.storage.meta.ping-key"))
                                    .append(text(": "))
                                    .append(text(storageMeta.ping() + "ms", GREEN))
                            ));
                        }

                        if (storageMeta.sizeBytes() != null) {
                            DecimalFormat format = new DecimalFormat("#.##");
                            String size = format.format(storageMeta.sizeBytes() / 1048576D) + "MB";

                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("renapowered.command.info.storage.meta.file-size-key"))
                                    .append(text(": "))
                                    .append(text(size, GREEN))
                            ));
                        }
                    })),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("renapowered.command.info.instance-key"))
                    .append(text(':'))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("renapowered.command.info.uptime-key"))
                    .append(text(": "))
                    .append(text().color(GRAY).append(DurationFormatter.CONCISE_LOW_ACCURACY.format(Duration.between(plugin.getBootstrap().getStartTime(), Instant.now()))))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("renapowered.command.info.local-data-key"))
                    .append(text(": "))
                    .append(translatable()
                            .key("renapowered.command.info.local-data")
                            .color(GRAY)
                            .args(
                                    text(plugin.getGuildManager().getAll().size(), GREEN),
                                    text(plugin.getUserManager().getAll().size(), GREEN),
                                    text(plugin.getMemberManager().getAll().size(), GREEN)
                            )
                    ))
    );

    Args1<String> REQUESTED_BY = username -> translatable()
            // Requested by {}
            .key("renapowered.embedsystem.requested")
            .args(text(username))
            .build();

    Args0 LEADERBOARD_AUTHOR = () -> translatable()
            // This week's leaderboard:
            .key("renapowered.rankingsystem.leaderboard.author")
            .append(text(':'))
            .build();

    Args2<LocalDateTime, LocalDateTime> LEADERBOARD_TITLE = (nextDate, lastDate) -> {
        // `{}` - `{}`
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return text()
                .append(text('`'))
                .append(text(lastDate.format(formatter))).append(text("` - `"))
                .append(text(nextDate.format(formatter)))
                .append(text('`'))
                .build();
    };

    Args1<Instant> LEADERBOARD_NEXT_UPDATE = nextInstant -> text()
            // *Next update of the ranking ::* <t:{}:R>
            .append(text('*'))
            .append(translatable("renapowered.rankingsystem.leaderboard.next-update"))
            .append(text(" ::* <t:"))
            .append(text(nextInstant.getEpochSecond()))
            .append(text(":R>"))
            .build();

    Args1<Integer> LEADERBOARD_LEVEL_FIELD = level -> text()
            // {}level: `{}`
            .append(text(Emote.EMPTY.toString()))
            .append(text("level: `"))
            .append(text(level))
            .append(text('`'))
            .build();

    Args1<String> LEADERBOARD_EXPERIENCE_FIELD = formattedExp -> text()
            // {}exp: `{}`
            .append(text(Emote.EMPTY.toString()))
            .append(text("exp: `"))
            .append(text(formattedExp))
            .append(text('`'))
            .build();

    Args3<Emote, Integer, String> LEADERBOARD_NAME_FIELD = (emote, placement, username) -> text()
            // {} `#{}` **{}**
            .append(text(emote.toString()))
            .append(text(" `#"))
            .append(text(placement))
            .append(text("` **"))
            .append(text(username))
            .append(text("**"))
            .build();

    Args0 ABOUT_TOTAL_TITLE = () -> translatable()
            // Total
            .key("renapowered.command.about.embed.total.title")
            .build();

    Args1<ShardManager> ABOUT_TOTAL_FIELD = shardManager -> text()
            // ```yaml\nGuilds: {}\n\nUsers: {}\n```
            .append(text("```yaml"))
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.total.guilds")
                    .append(text(": "))
                    .append(text(shardManager.getGuildCache().size())))
            .append(newline())
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.total.users")
                    .append(text(": "))
                    .append(text(shardManager.getUserCache().size())))
            .append(newline())
            .append(text("```"))
            .build();

    Args0 ABOUT_METADATA_TITLE = () -> translatable()
            // Metadata
            .key("renapowered.command.about.embed.metadata.title")
            .build();

    Args1<RenaBootstrap> ABOUT_METADATA_FIELD = bootstrap -> text()
            // ```yaml\nVersion: {}\n\nPlatform: {}\n```
            .append(text("```yaml"))
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.metadata.version")
                    .append(text(": "))
                    .append(text(bootstrap.getVersion())))
            .append(newline())
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.metadata.platform")
                    .append(text(": "))
                    .append(text(bootstrap.getType().getFriendlyName())))
            .append(newline())
            .append(text("```"))
            .build();

    Args0 ABOUT_OTHER_TITLE = () -> translatable()
            // Other stats
            .key("renapowered.command.about.embed.other.title")
            .build();

    Args1<RenaBootstrap> ABOUT_OTHER_FIELD = bootstrap -> text()
            // ```yaml\nJava: {}\n\nUptime: {}\n```
            .append(text("```yaml"))
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.other.java")
                    .append(text(": "))
                    .append(text(System.getProperty("java.vendor.version"))))
            .append(newline())
            .append(newline())
            .append(translatable()
                    .key("renapowered.command.about.embed.other.uptime")
                    .append(text(": "))
                    .append(DurationFormatter.CONCISE_LOW_ACCURACY.format(Duration.between(bootstrap.getStartTime(), Instant.now()))))
            .append(newline())
            .append(text("```"))
            .build();

    Args0 ABOUT_AUTHOR = () -> translatable()
            // Statistics
            .key("renapowered.command.about.embed.statistics")
            .build();

    Args1<String> PROFILE_TITLE = username -> translatable()
            // Information about {}
            .key("renapowered.command.profile.information")
            .args(text(username))
            .build();

    Args4<Component, OnlineStatus, Long, Map<Activity.ActivityType, List<Activity>>> PROFILE_DESCRIPTION = (username, status, lastSeenMillis, activitiesMap) -> {
        String statusKey = "renapowered.command.profile.status."
                + status.name().toLowerCase(Locale.ROOT)
                .replace("_", "-")
                .replace(" ", "-");

        Emote onlineStatusEmote = (status == OnlineStatus.UNKNOWN ? Emote.EMPTY : Emote.valueOf(status.name()));
        Component onlineStatus = text()
                .append(text(onlineStatusEmote.toString()))
                .append(translatable(statusKey))
                .build();

        Component lastSeen = status != OnlineStatus.OFFLINE && status != OnlineStatus.INVISIBLE
                ? translatable("renapowered.command.profile.last-seen.online")
                : text("<t:" + Instant.ofEpochMilli(lastSeenMillis).getEpochSecond() + ":R>");

        Component activities;
        if (activitiesMap.isEmpty()) {
            activities = Component.translatable("renapowered.command.profile.activity.empty");
        } else {
            Map<Component, String> activitiesFormatted = activitiesMap.entrySet().stream()
                    .filter(a -> a.getKey() != Activity.ActivityType.CUSTOM_STATUS)
                    .collect(ImmutableCollectors.toMap(
                            entry -> {
                                String activityKey = "renapowered.command.profile.activity."
                                        + entry.getKey().name().toLowerCase(Locale.ROOT)
                                        .replace("_", "-")
                                        .replace(" ", "-");
                                return Component.translatable(activityKey);
                            },
                            entry -> entry.getValue().stream()
                                    .map(activity -> {
                                        String activityName = activity.getName();
                                        String activityUrl = activity.getUrl();
                                        if (activityUrl != null) {
                                            activityName = "[" + activityName + "](" + activityUrl + ")";
                                        }
                                        return activityName;
                                    })
                                    .collect(Collectors.joining(", "))
                    ));
            TextComponent.Builder builder = Component.text();
            activitiesFormatted.forEach((key, value) -> {
                builder.append(Component.text("- **")).append(key);
                builder.append(Component.text(":** ")).append(Component.text(value));
                builder.append(Component.newline());
            });
            activities = builder.build();
        }
        return text()
                // > You can add here some useful info about yourself using {} command
                //
                // **__Common information__**
                // > **Username:** {}
                // > **Status:** {}
                // > **Last seen:** {}
                //
                // **__Activities__**
                // {}
                .append(text("> "))
                .append(translatable()
                        .key("renapowered.command.profile.biography")
                        .args(text("`/bio`")) // TODO: implement /bio slash command in future
                        .append(FULL_STOP))
                .append(newline())
                .append(newline())
                .append(text("**__")).append(translatable("renapowered.command.profile.commons"))
                .append(text("__**"))
                .append(newline())
                .append(text()
                        .append(text("> **"))
                        .append(translatable("renapowered.command.profile.username"))
                        .append(text(":** "))
                        .append(username))
                .append(newline())
                .append(text()
                        .append(text("> **"))
                        .append(translatable("renapowered.command.profile.status"))
                        .append(text(":** "))
                        .append(onlineStatus))
                .append(newline())
                .append(text()
                        .append(text("> **"))
                        .append(translatable("renapowered.command.profile.last-seen"))
                        .append(text(":** "))
                        .append(lastSeen))
                .append(newline())
                .append(newline())
                .append(text("**__")).append(translatable("renapowered.command.profile.activities"))
                .append(text("__**"))
                .append(newline())
                .append(activities)
                .build();
    };

    Args0 SUGGESTION_IN_COOLDOWN = () -> translatable()
            // Please wait before sending another suggestion...
            .key("renapowered.command.suggestion.exception.in-cooldown")
            .build();

    Args0 SUGGESTION_UNKNOWN_CHANNEL = () -> translatable()
            // It seems that this server does not support suggestions.
            .key("renapowered.command.suggestion.exception.unknown.channel")
            .append(FULL_STOP)
            .build();

    Args2<String, User> SUGGESTION_DESCRIPTION = (suggestion, user) -> text()
            // > **Suggestion:**
            // > `{}`
            //
            // **__Information__**
            // > **Suggester:** {}
            // > **Suggested on:** {}
            //
            // **__Note__**
            // > Please feel free to engage in a discussion about this suggestion in the thread below!
            .append(text()
                    .append(text("> **"))
                    .append(translatable("renapowered.command.suggestion.embed.suggestion"))
                    .append(text(":**"))
                    .append(newline())
                    .append(text("> `"))
                    .append(text(suggestion))
                    .append(text('`')))
            .append(newline())
            .append(newline())
            .append(text("**__")).append(translatable("renapowered.command.suggestion.embed.information"))
            .append(text("__**"))
            .append(newline())
            .append(text()
                    .append(text("> **"))
                    .append(translatable("renapowered.command.suggestion.embed.suggester"))
                    .append(text(":** "))
                    .append(text(user.getAsMention())))
            .append(newline())
            .append(text()
                    .append(text("> **"))
                    .append(translatable("renapowered.command.suggestion.embed.suggested"))
                    .append(text(":** <t:" + Instant.now().getEpochSecond() + ":F>")))
            .append(newline())
            .append(newline())
            .append(text("**__")).append(translatable("renapowered.command.suggestion.embed.note"))
            .append(text("__**"))
            .append(newline())
            .append(text()
                    .append(text("> "))
                    .append(translatable("renapowered.command.suggestion.embed.note-content")))
            .build();

    Args0 SUGGESTION_UNKNOWN = () -> translatable()
            // That suggestion doesn't exist!
            .key("renapowered.command.suggestion.exception.unknown")
            .build();

    Args0 SUGGESTION_APPROVE_TITLE = () -> translatable()
            // This suggestion have been approved! {}
            .key("renapowered.command.suggestion.approve.approved")
            .append(space())
            .append(text(Emote.WHITE_CHECK_MARK.toString()))
            .build();

    Args0 SUGGESTION_APPROVE_FOOTER = () -> translatable()
            // Suggestion approved
            .key("renapowered.command.suggestion.approve.approved-on")
            .build();

    Args0 SUGGESTION_APPROVE_RESPONSE = () -> translatable()
            // The suggestion is now mark as approved.
            .key("renapowered.command.suggestion.approve.response")
            .append(FULL_STOP)
            .build();

    Args0 SUGGESTION_DISAPPROVE_TITLE = () -> translatable()
            // This suggestion have been disapproved! {}
            .key("renapowered.command.suggestion.disapprove.disapproved")
            .append(space())
            .append(text(Emote.RED_CROSS.toString()))
            .build();

    Args0 SUGGESTION_DISAPPROVE_FOOTER = () -> translatable()
            // Suggestion disapproved
            .key("renapowered.command.suggestion.disapprove.disapproved-on")
            .build();

    Args0 SUGGESTION_DISAPPROVE_RESPONSE = () -> translatable()
            // The suggestion is now mark as disapproved.
            .key("renapowered.command.suggestion.disapprove.response")
            .append(FULL_STOP)
            .build();

    Args1<Channel> SUGGESTION_CHANNEL_UPDATED = channel -> translatable()
            // From now on, {} will be the transmission channel for all suggestions.
            .key("renapowered.command.suggestion.channel.updated")
            .args(text(channel.getAsMention()))
            .append(FULL_STOP)
            .build();

    Args1<Channel> RANKING_CHANNEL_UPDATED = channel -> translatable()
            // From now on, {} will be the transmission channel for the classification.
            .key("renapowered.command.ranking.channel.updated")
            .args(text(channel.getAsMention()))
            .append(FULL_STOP)
            .build();

    Args0 RANKING_ON = () -> translatable()
            // The ranking and experience system are now enabled on the server.
            .key("renapowered.command.ranking.enabled.on")
            .append(FULL_STOP)
            .build();

    Args0 RANKING_OFF = () -> translatable()
            // The ranking and experience system are now disabled on the server.
            .key("renapowered.command.ranking.enabled.off")
            .append(FULL_STOP)
            .build();

    static Component formatBoolean(boolean bool) {
        return bool ? text("true", GREEN) : text("false", RED);
    }

    static Component joinNewline(final ComponentLike... components) {
        return join(JoinConfiguration.newlines(), components);
    }

    interface Args0 {
        Component build();

        default void send(Sender sender) {
            sender.sendMessage(build());
        }
    }

    interface Args1<A0> {
        Component build(A0 arg0);

        default void send(Sender sender, A0 arg0) {
            sender.sendMessage(build(arg0));
        }
    }

    interface Args2<A0, A1> {
        Component build(A0 arg0, A1 arg1);

        default void send(Sender sender, A0 arg0, A1 arg1) {
            sender.sendMessage(build(arg0, arg1));
        }
    }

    interface Args3<A0, A1, A2> {
        Component build(A0 arg0, A1 arg1, A2 arg2);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2) {
            sender.sendMessage(build(arg0, arg1, arg2));
        }
    }

    interface Args4<A0, A1, A2, A3> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3));
        }
    }

    interface Args5<A0, A1, A2, A3, A4> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4));
        }
    }

    interface Args6<A0, A1, A2, A3, A4, A5> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4, arg5));
        }
    }
}
