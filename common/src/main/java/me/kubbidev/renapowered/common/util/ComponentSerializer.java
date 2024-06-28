package me.kubbidev.renapowered.common.util;

import me.kubbidev.renapowered.common.locale.TranslationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class ComponentSerializer {
    private static final PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.plainText();

    private ComponentSerializer() {
    }

    public static String serialize(Component component, @Nullable Locale locale) {
        return SERIALIZER.serialize(TranslationManager.render(component, locale));
    }

    public static Component deserialize(String content) {
        return SERIALIZER.deserialize(content);
    }
}
