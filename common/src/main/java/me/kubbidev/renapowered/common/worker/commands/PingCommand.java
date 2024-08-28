package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;

@DiscordCommand(name = "ping", visible = true)
public class PingCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        context.reply(context.deferReply(true), Component.text("Pong!"));
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("ping", "Pong!");
    }
}
