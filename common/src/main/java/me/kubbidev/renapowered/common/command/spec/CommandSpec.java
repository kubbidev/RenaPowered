package me.kubbidev.renapowered.common.command.spec;

import lombok.Getter;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An enumeration of the command defintion/usage messages used in the application.
 */
@Getter
@SuppressWarnings("SpellCheckingInspection")
public enum CommandSpec {

    SYNC("/%s sync"),
    INFO("/%s info"),
    IMPORT("/%s import <file>",
            arg("file", true)
    ),
    EXPORT("/%s export <file>",
            arg("file", false)
    ),
    RELOAD_CONFIG("/%s reloadconfig");

    private final String usage;
    private final List<Argument> args;

    CommandSpec(String usage, PartialArgument... args) {
        this.usage = usage;
        this.args = args.length == 0 ? null : Arrays.stream(args)
                .map(builder -> {
                    String key = builder.id.replace(".", "").replace(' ', '-');
                    TranslatableComponent description = Component.translatable("renapowered.usage." + key() + ".argument." + key);
                    return new Argument(builder.name, builder.required, description);
                })
                .collect(ImmutableCollectors.toList());
    }

    CommandSpec(PartialArgument... args) {
        this(null, args);
    }

    public TranslatableComponent description() {
        return Component.translatable("renapowered.usage." + this.key() + ".description");
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static PartialArgument arg(String id, String name, boolean required) {
        return new PartialArgument(id, name, required);
    }

    private static PartialArgument arg(String name, boolean required) {
        return new PartialArgument(name, name, required);
    }

    private record PartialArgument(String id, String name, boolean required) {

    }
}