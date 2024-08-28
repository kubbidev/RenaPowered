package me.kubbidev.renapowered.common.command.abstraction;

import lombok.Getter;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.Argument;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An abstract command class
 *
 * @param <T> the argument type required by the command
 */
public abstract class Command<T> {

    /**
     * The commands specification.
     * <p>
     * Contains details about usage, description, etc
     */
    @Getter
    private final CommandSpec spec;

    /**
     * The name of the command. Should be properly capitalised.
     */
    @Getter
    private final String name;

    /**
     * The permission required to use this command. Nullable.
     */
    @Nullable
    private final CommandPermission permission;

    /**
     * A predicate used for testing the size of the arguments list passed to this command
     */
    @Getter
    private final Predicate<Integer> argumentCheck;

    public Command(CommandSpec spec, String name, @Nullable CommandPermission permission, Predicate<Integer> argumentCheck) {
        this.spec = spec;
        this.name = name;
        this.permission = permission;
        this.argumentCheck = argumentCheck;
    }

    /**
     * Gets the permission required by this command, if present
     *
     * @return the command permission
     */
    public Optional<CommandPermission> getPermission() {
        return Optional.ofNullable(this.permission);
    }

    /**
     * Gets the commands description.
     *
     * @return the description
     */
    public Component getDescription() {
        return getSpec().description();
    }

    /**
     * Gets the usage of this command.
     * Will only return a non empty result for main commands.
     *
     * @return the usage of this command.
     */
    public String getUsage() {
        String usage = getSpec().getUsage();
        return usage == null ? "" : usage;
    }

    /**
     * Gets the arguments required by this command
     *
     * @return the commands arguments
     */
    public Optional<List<Argument>> getArgs() {
        return Optional.ofNullable(getSpec().getArgs());
    }

    // Main execution method for the command.
    public abstract void execute(RenaPlugin plugin, Sender sender, T target, ArgumentList args, String label) throws CommandException;

    // Tab completion method - default implementation is provided as some commands do not provide tab completions.
    public List<String> tabComplete(RenaPlugin plugin, Sender sender, ArgumentList args) {
        return Collections.emptyList();
    }

    /**
     * Sends a brief command usage message to the Sender.
     * If this command has child commands, the children are listed. Otherwise, a basic usage message is sent.
     *
     * @param sender the sender to send the usage to
     * @param label the label used when executing the command
     */
    public abstract void sendUsage(Sender sender, String label);

    /**
     * Sends a detailed command usage message to the Sender.
     * If this command has child commands, nothing is sent. Otherwise, a detailed messaging containing a description
     * and argument usage is sent.
     *
     * @param sender the sender to send the usage to
     * @param label the label used when executing the command
     */
    public abstract void sendDetailedUsage(Sender sender, String label);

    /**
     * Returns true if the sender is authorised to use this command
     * <p>
     * Commands with children are likely to override this method to check for permissions based upon whether
     * a sender has access to any sub commands.
     *
     * @param sender the sender
     * @return true if the sender has permission to use this command
     */
    public boolean isAuthorized(Sender sender) {
        return this.permission == null || this.permission.isAuthorized(sender);
    }

    /**
     * Gets if this command should be displayed in command listings, or "hidden"
     *
     * @return if the command should be displayed
     */
    public boolean shouldDisplay() {
        return true;
    }
}
