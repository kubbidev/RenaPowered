package me.kubbidev.renapowered.common.worker.commands.suggestion;

import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.GuildEntity;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@DiscordCommand(name = "suggestion_channel", subCommand = false)
public class SChannelCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        GuildChannel channel = Objects.requireNonNull(context.getOption("channel", OptionMapping::getAsChannel), "channel");
        GuildEntity guildEntity = StandardGuildManager.fetch(
                context.getPlugin(),
                context.getGuild()
        );
        guildEntity.setSuggestChannel(channel.getIdLong());
        StandardGuildManager.save(context.getPlugin(), guildEntity);

        context.reply(context.deferReply(true), Message.SUGGESTION_CHANNEL_UPDATED.build(channel));
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("suggestion_channel", "Configure the channel were the suggestion will be displayed.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to be displayed.", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }

    public static @Nullable TextChannel fetchSuggestChannel(RenaPlugin plugin, Guild guild, GuildEntity guildEntity) {
        TextChannel channel = guild.getChannelById(TextChannel.class, guildEntity.getSuggestChannel());
        if (channel == null) {
            if (guildEntity.getSuggestChannel() != 0) {
                guildEntity.setSuggestChannel(0);
                // if could not find the channel just delete it from the config (0)
                // and save guild config changes
                StandardGuildManager.save(plugin, guildEntity);
            }
        }
        return channel;
    }
}
