package me.kubbidev.renapowered.common.listeners;

import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.model.manager.StandardMemberManager;
import me.kubbidev.renapowered.common.model.manager.StandardUserManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.ExpiringSet;
import me.kubbidev.renapowered.common.worker.event.EventHandler;
import me.kubbidev.renapowered.common.worker.listener.DiscordEventListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@EventHandler
public class MemberListener extends DiscordEventListener {
    private final ExpiringSet<Long> statusCache = new ExpiringSet<>(1, TimeUnit.MINUTES);

    public MemberListener(RenaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        MemberEntity memberEntity = StandardMemberManager.fetch(this.plugin, event.getMember());

        String effectiveName = event.getMember().getEffectiveName();
        String effectiveMember = memberEntity.getEffectiveName();

        if (!Objects.equals(effectiveName, effectiveMember)) {
            memberEntity.setEffectiveName(effectiveName);

            StandardMemberManager.save(this.plugin, memberEntity);
        }
    }

    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        OnlineStatus newStatus = event.getNewOnlineStatus();
        OnlineStatus oldStatus = event.getOldOnlineStatus();

        User user = event.getUser();
        if (user.isBot()) {
            return;
        }

        if ((newStatus == OnlineStatus.OFFLINE || newStatus == OnlineStatus.INVISIBLE)
                && oldStatus != OnlineStatus.OFFLINE
                && oldStatus != OnlineStatus.INVISIBLE) {

            // event are executed multiple times for each mutual guild, we want this only once at least per minute
            if (!this.statusCache.add(user.getIdLong())) {
                return;
            }
            UserEntity userEntity = StandardUserManager.fetch(this.plugin, user);
            userEntity.setLastSeen(System.currentTimeMillis());

            StandardUserManager.save(this.plugin, userEntity);
        }
    }
}
