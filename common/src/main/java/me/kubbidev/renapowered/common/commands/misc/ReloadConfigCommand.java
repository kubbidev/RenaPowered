package me.kubbidev.renapowered.common.commands.misc;

import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.abstraction.SingleCommand;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.Predicates;

public class ReloadConfigCommand extends SingleCommand {
    public ReloadConfigCommand() {
        super(CommandSpec.RELOAD_CONFIG, "ReloadConfig", CommandPermission.RELOAD_CONFIG, Predicates.alwaysFalse());
    }

    @Override
    public void execute(RenaPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        plugin.getConfiguration().reload();
        Message.RELOAD_CONFIG_SUCCESS.send(sender);
    }
}