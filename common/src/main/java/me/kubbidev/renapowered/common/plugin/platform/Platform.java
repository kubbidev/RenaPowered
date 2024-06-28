package me.kubbidev.renapowered.common.plugin.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Provides information about the platform RenaPowered is running on.
 */
public interface Platform {

    /**
     * Gets the type of platform RenaPowered is running on
     *
     * @return the type of platform RenaPowered is running on
     */
    Platform.Type getType();

    /**
     * Gets the time when the application first started.
     *
     * @return the enable time
     */
    Instant getStartTime();

    /**
     * Represents a type of platform which RenaPowered can run on.
     */
    @Getter
    @AllArgsConstructor
    enum Type {
        STANDALONE("Standalone");

        private final String friendlyName;
    }
}