package me.kubbidev.renapowered.common.command.util;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * A list of {@link String} arguments, with extra methods to help
 * with parsing.
 */
public class ArgumentList extends ForwardingList<String> {
    private final List<String> backingList;

    public ArgumentList(List<String> backingList) {
        this.backingList = backingList;
    }

    @Override
    protected List<String> delegate() {
        return this.backingList;
    }

    @Override
    public String get(int index) throws IndexOutOfBoundsException {
        return super.get(index).replace("{SPACE}", " ");
    }

    public boolean indexOutOfBounds(int index) {
        return index < 0 || index >= size();
    }

    public String getOrDefault(int index, String defaultValue) {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }
        return get(index);
    }

    @Override
    public @NotNull ArgumentList subList(int fromIndex, int toIndex) {
        return new ArgumentList(super.subList(fromIndex, toIndex));
    }

    public int getIntOrDefault(int index, int defaultValue) {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getLowercase(int index, Predicate<? super String> test) throws ArgumentException.DetailedUsage {
        String arg = get(index).toLowerCase(Locale.ROOT);
        if (!test.test(arg)) {
            throw new ArgumentException.DetailedUsage();
        }
        return arg;
    }

    public boolean getBooleanOrInsert(int index, boolean defaultValue) {
        if (!indexOutOfBounds(index)) {
            String arg = get(index);
            if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(arg);
            }
        }

        add(index, Boolean.toString(defaultValue));
        return defaultValue;
    }
}