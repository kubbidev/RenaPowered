package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;

import java.util.function.Supplier;

@DiscordCommand(name = "ping", subCommand = false)
public class PingCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        context.reply(context.deferReply(true), Component.text("Pong!"))
                .thenAccept(message -> {
                    // add a reaction to let others ping pong
                    message.addReaction(Emoji.fromUnicode("U+1F3D3")).queue();
                });
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("ping", "Pong!");
    }
}
