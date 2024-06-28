package me.kubbidev.renapowered.common.dependencies.relocation;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class Relocation {
    private static final String RELOCATION_PREFIX = "me.kubbidev.renapowered.lib.";

    public static Relocation of(String id, String pattern) {
        return new Relocation(pattern.replace("{}", "."), RELOCATION_PREFIX + id);
    }

    private final String pattern;
    private final String relocatedPattern;

    private Relocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Relocation other))
            return false;

        return Objects.equals(this.pattern, other.pattern) &&
                Objects.equals(this.relocatedPattern, other.relocatedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pattern, this.relocatedPattern);
    }
}