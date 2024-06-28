package me.kubbidev.renapowered.common.worker.commands;

import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.model.manager.StandardUserManager;
import me.kubbidev.renapowered.common.worker.util.CEmbed;
import me.kubbidev.renapowered.common.worker.command.CommandContext;
import me.kubbidev.renapowered.common.worker.command.DiscordCommand;
import me.kubbidev.renapowered.common.worker.command.InteractionCommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@DiscordCommand(name = "profile", subCommand = false)
public class ProfileCommand implements InteractionCommand {

    @Override
    public void onPerform(CommandContext context) {
        Objects.requireNonNull(context.getGuild(), "guild");
        Objects.requireNonNull(context.getMember(), "member");

        Member author = context.getMember();
        Member member = context.getOption("user", OptionMapping::getAsMember);
        if (member == null) {
            member = context.getMember();
        }
        UserEntity userEntity = StandardUserManager.fetch(
                context.getPlugin(),
                member.getUser()
        );
        Map<Activity.ActivityType, List<Activity>> activities = member.getActivities().stream()
                .collect(Collectors.groupingBy(Activity::getType));

        CEmbed builder = new CEmbed();
        builder.color(0x1663ff);
        builder.thumbnail(member.getEffectiveAvatarUrl());
        builder.timestamp(Instant.now());
        builder.author(Message.PROFILE_TITLE.build(member.getEffectiveName()));
        builder.footer(Message.REQUESTED_BY.build(author.getEffectiveName()), author.getEffectiveAvatarUrl());
        builder.description(Message.PROFILE_DESCRIPTION.build(getUsername(member), member.getOnlineStatus(), userEntity.getLastSeen(), activities));

        member.getUser().retrieveProfile().submit().thenAccept(profile -> {
            ImageProxy userBanner = profile.getBanner();
            if (userBanner != null) {
                builder.color(profile.getAccentColorRaw());
                builder.image(userBanner.getUrl(1024));
            }
            context.reply(context.deferReply(false), builder);
        });
    }

    @Override
    public Supplier<SlashCommandData> getSlashCommand() {
        return () -> Commands.slash("profile", "Display the supplied user profile.")
                .addOption(OptionType.USER, "user", "The user to show his profile.")
                .setGuildOnly(true);
    }

    private Component getUsername(Member member) {
        String username = member.getUser().getName();
        String effective = member.getEffectiveName();

        if (!Objects.equals(username, effective)) {
            username += " (" + effective + ")";
        }
        return Component.text(username);
    }
}
