package me.kubbidev.renapowered.standalone.app.integration;

import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

public interface StandaloneSender {
    String getName();

    UUID getUniqueId();

    void sendMessage(Component component);

    boolean hasPermission(String permission);

    boolean isConsole();

    Locale getLocale();
}