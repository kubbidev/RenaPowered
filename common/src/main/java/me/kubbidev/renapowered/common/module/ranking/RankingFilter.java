package me.kubbidev.renapowered.common.module.ranking;

import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.worker.event.intercept.FilterChain;
import me.kubbidev.renapowered.common.worker.event.intercept.MemberMessageFilter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RankingFilter extends MemberMessageFilter {
    public RankingFilter(RenaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void doInternal(MessageReceivedEvent e, FilterChain<MessageReceivedEvent> chain) {
        try {
            this.plugin.getDiscordService().getRankingService().onMessage(e);
        } finally {
            chain.doFilter(e);
        }
    }
}