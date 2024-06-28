package me.kubbidev.renapowered.common.command.util;

import me.kubbidev.renapowered.common.command.abstraction.Command;
import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.sender.Sender;

public abstract class ArgumentException extends CommandException {

    public static class DetailedUsage extends ArgumentException {
        @Override
        protected void handle(Sender sender) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(Sender sender, String label, Command<?> command) {
            command.sendDetailedUsage(sender, label);
        }
    }
}