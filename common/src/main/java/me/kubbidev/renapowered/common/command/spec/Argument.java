package me.kubbidev.renapowered.common.command.spec;

import me.kubbidev.renapowered.common.locale.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public record Argument(String name, boolean required, TranslatableComponent description) {

    public Component asPrettyString() {
        return (this.required ? Message.REQUIRED_ARGUMENT : Message.OPTIONAL_ARGUMENT).build(this.name);
    }
}