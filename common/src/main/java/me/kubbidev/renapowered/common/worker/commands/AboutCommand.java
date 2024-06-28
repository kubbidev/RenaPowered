package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.worker.util.CEmbed;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.locale.Message;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

@DiscordCommand(name = "about", subCommand = false)
public class AboutCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        CEmbed embed = new CEmbed();
        embed.author(Message.ABOUT_AUTHOR.build());
        embed.footer(Message.REQUESTED_BY.build(
                context.getAuthor().getEffectiveName()),
                context.getAuthor().getEffectiveAvatarUrl()
        );
        embed.timestamp(ZonedDateTime.now());
        embed.color(0x1663ff);
        embed.field(
                Message.ABOUT_TOTAL_TITLE.build(),
                Message.ABOUT_TOTAL_FIELD.build(context.getPlugin().getDiscordService().getShardManager()), false
        );
        embed.field(
                Message.ABOUT_METADATA_TITLE.build(),
                Message.ABOUT_METADATA_FIELD.build(context.getPlugin().getBootstrap()), false
        );
        embed.field(
                Message.ABOUT_OTHER_TITLE.build(),
                Message.ABOUT_OTHER_FIELD.build(context.getPlugin().getBootstrap()), false
        );

        // send embed in text channel (no user only message)
        context.reply(context.deferReply(false), embed);
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("about", "Prints general information about the active bot instance.");
    }
}
