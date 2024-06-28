package me.kubbidev.renapowered.standalone.app.integration;

import me.kubbidev.renapowered.standalone.app.RenaApplication;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;

import java.util.Locale;
import java.util.UUID;

/**
 * The sender instance used for the console / users executing commands
 * on a standalone instance of RenaPowered
 */
public class StandaloneUser implements StandaloneSender {

    private static final UUID UUID = new UUID(0, 0);

    public static final StandaloneUser INSTANCE = new StandaloneUser();

    private StandaloneUser() {
    }

    @Override
    public String getName() {
        return "StandaloneUser";
    }

    @Override
    public UUID getUniqueId() {
        return UUID;
    }

    @Override
    public void sendMessage(Component component) {
        RenaApplication.LOGGER.info(ANSIComponentSerializer.ansi().serialize(component));
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
}