package me.kubbidev.renapowered.standalone;

import me.kubbidev.renapowered.common.sender.SenderFactory;
import me.kubbidev.renapowered.common.locale.TranslationManager;
import me.kubbidev.renapowered.standalone.app.integration.StandaloneSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class StandaloneSenderFactory extends SenderFactory<RStandalonePlugin, StandaloneSender> {

    public StandaloneSenderFactory(RStandalonePlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getName(StandaloneSender sender) {
        return sender.getName();
    }

    @Override
    protected UUID getUniqueId(StandaloneSender sender) {
        return sender.getUniqueId();
    }

    @Override
    protected void sendMessage(StandaloneSender sender, Component message) {
        Component rendered = TranslationManager.render(message, sender.getLocale());
        sender.sendMessage(rendered);
    }

    @Override
    protected boolean hasPermission(StandaloneSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected boolean isConsole(StandaloneSender sender) {
        return sender.isConsole();
    }

    @Override
    protected boolean shouldSplitNewlines(StandaloneSender sender) {
        return true;
    }
}