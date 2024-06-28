package me.kubbidev.renapowered.common.worker.command;

import lombok.Getter;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.ComponentSerializer;
import me.kubbidev.renapowered.common.worker.util.CEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Event class used to parse and provide Information about a command execution.
 */
public class CommandContext {

    @Getter
    private final RenaPlugin plugin;
    private final CommandInteraction interaction;

    @Nullable
    private InteractionHook hookedInteraction;

    public CommandContext(RenaPlugin plugin, CommandInteraction interaction) {
        this.plugin = plugin;
        this.interaction = interaction;
    }

    public CompletableFuture<Message> reply(InteractionHook interactionHook, MessageCreateData message) {
        return this.plugin.getDiscordService().sendMessageSilent(interactionHook::sendMessage, message);
    }

    public CompletableFuture<Message> replyTemp(InteractionHook interactionHook, MessageCreateData message, long delay, TimeUnit unit) {
        return this.plugin.getDiscordService().sendTempMessageSilent(interactionHook::sendMessage, message, delay, unit);
    }

    public CompletableFuture<Message> reply(InteractionHook interactionHook, Component message) {
        return this.plugin.getDiscordService().sendMessageSilent(interactionHook::sendMessage,
                ComponentSerializer.serialize(message, getDiscordLocale().toLocale()));
    }

    public CompletableFuture<Message> replyTemp(InteractionHook interactionHook, Component message, long delay, TimeUnit unit) {
        return this.plugin.getDiscordService().sendTempMessageSilent(interactionHook::sendMessage,
                ComponentSerializer.serialize(message, getDiscordLocale().toLocale()), delay, unit);
    }

    public CompletableFuture<Message> reply(InteractionHook interactionHook, CEmbed embed) {
        return this.plugin.getDiscordService().sendMessageSilent(builder -> interactionHook.sendMessage(new MessageCreateBuilder()
                .setEmbeds(embed.build(getDiscordLocale().toLocale()))
                .build()), embed);
    }

    public CompletableFuture<Message> replyTemp(InteractionHook interactionHook, CEmbed embed, long delay, TimeUnit unit) {
        return this.plugin.getDiscordService().sendTempMessageSilent(builder -> interactionHook.sendMessage(new MessageCreateBuilder()
                .setEmbeds(embed.build(getDiscordLocale().toLocale()))
                .build()), embed, delay, unit);
    }

    public User getAuthor() {
        return this.interaction.getUser();
    }

    public @Nullable Member getMember() {
        return this.interaction.getMember();
    }

    public @Nullable Guild getGuild() {
        return this.interaction.getGuild();
    }

    public MessageChannel getChannel() {
        return this.interaction.getMessageChannel();
    }

    public DiscordLocale getDiscordLocale() {
        return this.interaction.getUserLocale();
    }

    public @Nullable <T> T getOption(String name, Function<OptionMapping, T> mapping) {
        return this.interaction.getOption(name, mapping);
    }

    public InteractionHook deferReply(boolean ephemeral) {
        if (this.hookedInteraction == null) {
            this.hookedInteraction = this.interaction.deferReply().setEphemeral(ephemeral).submit().join();
        }
        return this.hookedInteraction;
    }
}