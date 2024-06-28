package me.kubbidev.renapowered.standalone;

import me.kubbidev.renapowered.common.command.CommandManager;
import me.kubbidev.renapowered.common.command.util.ArgumentTokenizer;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.standalone.app.integration.CommandExecutor;
import me.kubbidev.renapowered.standalone.app.integration.StandaloneSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StandaloneCommandManager extends CommandManager implements CommandExecutor {
    private final RStandalonePlugin plugin;

    public StandaloneCommandManager(RStandalonePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> execute(StandaloneSender player, String command) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(player);
        List<String> arguments = ArgumentTokenizer.EXECUTE.tokenizeInput(command);
        return executeCommand(wrapped, "rp", arguments);
    }

    @Override
    public List<String> tabComplete(StandaloneSender player, String command) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(player);
        List<String> arguments = ArgumentTokenizer.TAB_COMPLETE.tokenizeInput(command);
        return tabCompleteCommand(wrapped, arguments);
    }
}
