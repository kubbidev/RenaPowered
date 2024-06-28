package me.kubbidev.renapowered.common.util;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utilities for working with {@link UUID}s.
 */
public final class Uuids {
    private Uuids() {
    }

    public static final Predicate<String> PREDICATE = s -> parse(s) != null;

    public static @Nullable UUID fromString(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static @Nullable UUID parse(String s) {
        UUID uuid = fromString(s);
        if (uuid == null && s.length() == 32) {
            try {
                uuid = new UUID(
                        Long.parseUnsignedLong(s.substring(0, 16), 16),
                        Long.parseUnsignedLong(s.substring(16), 16)
                );
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return uuid;
    }
}