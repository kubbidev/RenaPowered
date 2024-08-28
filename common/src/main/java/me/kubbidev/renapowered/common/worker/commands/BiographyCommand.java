package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.manager.StandardMemberManager;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

@DiscordCommand(name = "bio", visible = true)
public class BiographyCommand implements InteractionCommand {
    private static final int BIOGRAPHY_MAX_LENGTH = 192;

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        String message = context.getOption("message", OptionMapping::getAsString);
        if (message != null) {
            message = message.replace("\\n", " ");

            if (message.length() > BIOGRAPHY_MAX_LENGTH) {
                message = message.substring(0, BIOGRAPHY_MAX_LENGTH) + "...";
            }
        }

        MemberEntity memberEntity = StandardMemberManager.fetch(
                context.getPlugin(),
                context.getMember()
        );
        memberEntity.setBiography(message);
        StandardMemberManager.save(context.getPlugin(), memberEntity);

        context.reply(context.deferReply(true), Message.BIOGRAPHY_SUCCESSFULLY.build());
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("bio", "Display a pretty description in your profile (empty execution will reset your bio).")
                .addOption(OptionType.STRING, "message", "The biography to display.")
                .setGuildOnly(true);
    }
}
