package me.kubbidev.renapowered.common.sender;

import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Wrapper interface to represent a CommandSender/CommandSource within the common command implementations.
 */
public interface Sender {

    /**
     * The uuid used by the console sender.
     */
    UUID CONSOLE_UUID = new UUID(0, 0); // 00000000-0000-0000-0000-000000000000

    /**
     * The name used by the console sender.
     */
    String CONSOLE_NAME = "Console";

    /**
     * Gets the plugin instance the sender is from.
     *
     * @return the plugin
     */
    RenaPlugin getPlugin();

    /**
     * Gets the sender's username
     *
     * @return a friendly username for the sender
     */
    String getName();

    /**
     * Gets the sender's unique id.
     *
     * <p>See {@link #CONSOLE_UUID} for the console's UUID representation.</p>
     *
     * @return the sender's uuid
     */
    UUID getUniqueId();

    /**
     * Send a json message to the Sender.
     *
     * @param message the message to send.
     */
    void sendMessage(Component message);

    /**
     * Check if the Sender has a permission.
     *
     * @param permission the permission to check for
     * @return true if the sender has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Check if the Sender has a permission.
     *
     * @param permission the permission to check for
     * @return true if the sender has the permission
     */
    default boolean hasPermission(CommandPermission permission) {
        return hasPermission(permission.getPermission());
    }

    /**
     * Gets whether this sender is the console
     *
     * @return if the sender is the console
     */
    boolean isConsole();
}