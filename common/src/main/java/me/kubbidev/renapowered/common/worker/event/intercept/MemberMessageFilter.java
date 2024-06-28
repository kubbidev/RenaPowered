package me.kubbidev.renapowered.common.worker.event.intercept;

import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class MemberMessageFilter implements Filter<MessageReceivedEvent> {
    protected final RenaPlugin plugin;

    public MemberMessageFilter(RenaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void doFilter(MessageReceivedEvent e, FilterChain<MessageReceivedEvent> chain) {
        try {
            if (!e.isWebhookMessage()
                    && !e.getAuthor().isBot()
                    && e.isFromGuild()
                    && e.getMessage().getType() == MessageType.DEFAULT) {
                doInternal(e, chain);
                return;
            }
        } catch (Throwable t) {
            this.plugin.getLogger().warn("Unexpected filter error", t);
        }
        chain.doFilter(e);
    }

    protected abstract void doInternal(MessageReceivedEvent event, FilterChain<MessageReceivedEvent> chain);
}