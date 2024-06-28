package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class MessageFilterFactory extends BaseEventFilterFactory<MessageReceivedEvent> {

    public MessageFilterFactory(List<Filter<MessageReceivedEvent>> filters) {
        super(filters);
    }

    @Override
    public Class<MessageReceivedEvent> getType() {
        return MessageReceivedEvent.class;
    }
}
