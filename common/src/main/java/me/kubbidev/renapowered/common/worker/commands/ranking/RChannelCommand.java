package me.kubbidev.renapowered.common.worker.commands.ranking;

import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.locale.Message;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

@DiscordCommand(name = "ranking_channel", visible = true)
public class RChannelCommand implements InteractionCommand {
    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        GuildChannel channel = Objects.requireNonNull(context.getOption("channel", OptionMapping::getAsChannel), "channel");
        GuildEntity guildEntity = StandardGuildManager.fetch(
                context.getPlugin(),
                context.getGuild()
        );
        guildEntity.setRankingChannel(channel.getIdLong());
        StandardGuildManager.save(context.getPlugin(), guildEntity);

        context.reply(context.deferReply(true), Message.RANKING_CHANNEL_UPDATED.build(channel));
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("ranking_channel", "Configure the channel were the leaderboard will be broadcast.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to broadcast.", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
