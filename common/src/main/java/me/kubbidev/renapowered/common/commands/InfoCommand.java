package me.kubbidev.renapowered.common.commands;

import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.abstraction.SingleCommand;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.Predicates;

public class InfoCommand extends SingleCommand {
    public InfoCommand() {
        super(CommandSpec.INFO, "Info", CommandPermission.INFO, Predicates.alwaysFalse());
    }

    @Override
    public void execute(RenaPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        Message.INFO.send(sender, plugin, plugin.getStorage().getMeta());
    }
}
