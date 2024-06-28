package me.kubbidev.renapowered.common.worker.commands.ranking;

import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import me.kubbidev.renapowered.common.locale.Message;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.function.Supplier;

@DiscordCommand(name = "ranking_enabled", subCommand = false)
public class REnabledCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        boolean value = Objects.requireNonNull(context.getOption("action", OptionMapping::getAsBoolean), "action");

        GuildEntity guildEntity = StandardGuildManager.fetch(
                context.getPlugin(),
                context.getGuild()
        );
        guildEntity.setRanking(value);
        StandardGuildManager.save(context.getPlugin(), guildEntity);

        Component message = value ? Message.RANKING_ON.build() : Message.RANKING_OFF.build();
        context.reply(context.deferReply(true), message);
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("ranking_enabled", "Controls the servers ranking and experience system.")
                .addOption(OptionType.BOOLEAN, "action", "Whether to enable/disable the system.", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
