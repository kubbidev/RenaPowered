package me.kubbidev.renapowered.common.sender;

import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Factory class to make a thread-safe sender instance
 *
 * @param <P> the plugin type
 * @param <T> the command sender type
 */
public abstract class SenderFactory<P extends RenaPlugin, T> implements AutoCloseable {
    private final P plugin;

    public SenderFactory(P plugin) {
        this.plugin = plugin;
    }

    protected P getPlugin() {
        return this.plugin;
    }

    protected abstract UUID getUniqueId(T sender);

    protected abstract String getName(T sender);

    protected abstract void sendMessage(T sender, Component message);

    protected abstract boolean hasPermission(T sender, String node);

    protected abstract boolean isConsole(T sender);

    protected boolean shouldSplitNewlines(T sender) {
        return isConsole(sender);
    }

    public final Sender wrap(T sender) {
        Objects.requireNonNull(sender, "sender");
        return new AbstractSender<>(this.plugin, this, sender);
    }

    @Override
    public void close() {

    }
}