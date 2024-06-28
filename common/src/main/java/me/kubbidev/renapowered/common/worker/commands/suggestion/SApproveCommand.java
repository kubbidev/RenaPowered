package me.kubbidev.renapowered.common.worker.commands.suggestion;

import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.util.ComponentSerializer;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.GuildEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

@DiscordCommand(name = "suggestion_approve", subCommand = false)
public class SApproveCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        String messageId = Objects.requireNonNull(context.getOption("message", OptionMapping::getAsString), "message");
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

        channel.retrieveMessageById(messageId).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            // only modify the first embed if found (should always happened)
            if (embeds.size() == 1) {
                EmbedBuilder builder = new EmbedBuilder(embeds.get(0));
                builder.setColor(0x30e658);

                Locale locale = context.getDiscordLocale().toLocale();
                builder.setTitle(ComponentSerializer.serialize(Message.SUGGESTION_APPROVE_TITLE.build(), locale));
                builder.setFooter(ComponentSerializer.serialize(Message.SUGGESTION_APPROVE_FOOTER.build(), locale));
                builder.setTimestamp(Instant.now());
                // edit the embed builder with the new one
                message.editMessageEmbeds(builder.build()).queue();
            }
            ThreadChannel thread = message.getStartedThread();
            if (thread != null && !thread.isLocked()) {
                thread.getManager().setLocked(true).queue();
            }
            context.reply(context.deferReply(true), Message.SUGGESTION_APPROVE_RESPONSE.build());
        }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e ->
                // unknown message, cannot fetch this message...
                context.reply(context.deferReply(true), Message.SUGGESTION_UNKNOWN.build())
        ));
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("suggestion_approve", "Mark the suggestion provided as approved.")
                .addOption(OptionType.STRING, "message", "The message id of the suggestion.", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
