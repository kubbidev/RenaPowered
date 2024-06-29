package me.kubbidev.renapowered.common.commands;

import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.abstraction.SingleCommand;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.util.Predicates;

public class SyncCommand extends SingleCommand {
    public SyncCommand() {
        super(CommandSpec.SYNC, "Sync", CommandPermission.SYNC, Predicates.alwaysFalse());
    }

    @Override
    public void execute(RenaPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        Message.UPDATE_TASK_REQUEST.send(sender);
        plugin.getSyncTaskBuffer().request().join();
        Message.UPDATE_TASK_COMPLETE.send(sender);
    }
}