package me.kubbidev.renapowered.common.command.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kubbidev.renapowered.common.sender.Sender;

/**
 * An enumeration of the permissions required to execute built in RenaPowered commands.
 */
@Getter
public enum CommandPermission {

    SYNC("sync", Type.NONE),
    INFO("info", Type.NONE),
    IMPORT("import", Type.NONE),
    EXPORT("export", Type.NONE),
    RELOAD_CONFIG("reloadconfig", Type.NONE);

    public static final String ROOT = "renapowered.";

    private final String node;
    private final String permission;

    private final Type type;

    CommandPermission(String node, Type type) {
        this.type = type;

        if (type == Type.NONE) {
            this.node = node;
        } else {
            this.node = type.tag + "." + node;
        }

        this.permission = ROOT + this.node;
    }

    public boolean isAuthorized(Sender sender) {
        return sender.hasPermission(this);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        NONE(null);

        private final String tag;
    }
}