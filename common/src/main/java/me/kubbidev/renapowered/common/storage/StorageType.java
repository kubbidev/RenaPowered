package me.kubbidev.renapowered.common.storage;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;

@Getter
public enum StorageType {

    // Remote databases
    MONGODB("MongoDB", "mongodb"),

    // Custom
    CUSTOM("Custom", "custom");

    private final String name;
    private final List<String> identifiers;

    StorageType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static StorageType parse(String name, StorageType def) {
        for (StorageType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }
}