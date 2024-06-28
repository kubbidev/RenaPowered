package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.worker.util.Emote;
import me.kubbidev.renapowered.common.util.ExpiringSet;
import me.kubbidev.renapowered.common.worker.util.CEmbed;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.worker.commands.suggestion.SChannelCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@DiscordCommand(name = "suggestion", subCommand = false)
public class SuggestionCommand implements InteractionCommand {

    // set a expiring cache to avoid members spamming the suggest command
    private final ExpiringSet<UUID> cooldowns = new ExpiringSet<>(1, TimeUnit.MINUTES);

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        String suggest = Objects.requireNonNull(context.getOption("suggest", OptionMapping::getAsString), "suggest");

        // compute member uuid from user and guild ids
        UUID uuid = new UUID(
                context.getAuthor().getIdLong(),
                context.getGuild().getIdLong()
        );

        if (this.cooldowns.contains(uuid)) {
            context.reply(context.deferReply(true), Message.SUGGESTION_IN_COOLDOWN.build());
            return;
        }
        this.cooldowns.add(uuid);
        GuildEntity guildEntity = StandardGuildManager.fetch(
                context.getPlugin(),
                context.getGuild()
        );
        TextChannel channel = SChannelCommand.fetchSuggestChannel(
                context.getPlugin(),
                context.getGuild(), guildEntity
        );
        if (channel == null) {
            context.reply(context.deferReply(true), Message.SUGGESTION_UNKNOWN_CHANNEL.build());
            return;
        }

        // build the embed with appropriate fields
        CEmbed embed = new CEmbed();
        embed.color(0x1663ff);
        embed.description(Message.SUGGESTION_DESCRIPTION.build(suggest, context.getAuthor()));

        // send the suggestion embed and create a new thread for it
        context.getPlugin().getDiscordService().sendMessage(channel, embed, context.getDiscordLocale().toLocale())
                .thenAccept(message -> message.createThreadChannel(suggest).queue());

        // response to the member with a green check
        context.reply(context.deferReply(true), Component.text(Emote.WHITE_CHECK_MARK.toString()));
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("suggestion", "Share your best suggestion on the topic of your choice.")
                .addOption(OptionType.STRING, "suggest", "The suggestion in question.", true)
                .setGuildOnly(true);
    }
}
