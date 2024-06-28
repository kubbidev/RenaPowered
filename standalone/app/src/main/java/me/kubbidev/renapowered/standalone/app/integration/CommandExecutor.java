package me.kubbidev.renapowered.standalone.app.integration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal command executor interface.
 */
public interface CommandExecutor {

    CompletableFuture<Void> execute(StandaloneSender player, String command);

    List<String> tabComplete(StandaloneSender player, String command);

    default CompletableFuture<Void> execute(String command) {
        return execute(StandaloneUser.INSTANCE, command);
    }

    default List<String> tabComplete(String command) {
        return tabComplete(StandaloneUser.INSTANCE, command);
    }
}