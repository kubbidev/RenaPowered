package me.kubbidev.renapowered.common.worker.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DiscordCommand {

    String name();

    boolean visible();
}
