package me.kubbidev.renapowered.common.model.manager;

import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.manager.abstraction.AbstractManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.dv8tion.jda.api.entities.Member;

import java.util.Objects;
import java.util.UUID;

public class StandardMemberManager extends AbstractManager<UUID, MemberEntity> {

    public static MemberEntity fetch(RenaPlugin plugin, Member member) {
        MemberEntity entity = fetch(plugin, new UUID(
                member.getUser().getIdLong(),
                member.getGuild().getIdLong()
        ));

        if (updateIfRequired(member, entity)) {
            save(plugin, entity);
        }
        return entity;
    }

    public static MemberEntity fetch(RenaPlugin plugin, UUID uuid) {
        StandardMemberManager memberManager = plugin.getMemberManager();

        // load the entity from the local data storage first to avoid
        // useless remote database queries
        MemberEntity entity = memberManager.getIfLoaded(uuid);
        if (entity == null) {
            entity = plugin.getStorage()
                    .loadEntity(MemberEntity.class, uuid, memberManager).join();
        }

        return entity;
    }

    public static void save(RenaPlugin plugin, MemberEntity member) {
        try {
            plugin.getStorage().saveEntity(member).get();
        } catch (Exception e) {
            plugin.getLogger().warn("Error whilst saving member", e);
        }
    }

    private static boolean updateIfRequired(Member member, MemberEntity memberEntity) {
        boolean shouldSave = false;

        if (!Objects.equals(member.getEffectiveName(), memberEntity.getEffectiveName())) {
            memberEntity.setEffectiveName(member.getEffectiveName());
            shouldSave = true;
        }

        return shouldSave;
    }

    @Override
    public MemberEntity apply(UUID id) {
        return new MemberEntity(id);
    }
}
