package me.kubbidev.renapowered.common.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.kubbidev.renapowered.common.command.abstraction.Command;
import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.tabcomplete.CompletionSupplier;
import me.kubbidev.renapowered.common.command.tabcomplete.TabCompleter;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.commands.*;
import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.AbstractRenaPlugin;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerTask;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.util.ExpiringSet;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Root command manager for the '/renapowered' command.
 */
public class CommandManager {

    @Getter
    private final RenaPlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("renapowered-command-executor")
            .build()
    );
    private final AtomicBoolean executingCommand = new AtomicBoolean(false);
    private final ExpiringSet<UUID> userRateLimit = new ExpiringSet<>(500, TimeUnit.MILLISECONDS);

    @Getter(onMethod_ = @VisibleForTesting)
    private final Map<String, Command<?>> mainCommands;

    public CommandManager(RenaPlugin plugin) {
        this.plugin = plugin;
        this.mainCommands = ImmutableList.<Command<?>>builder()
                .addAll(plugin.getExtraCommands())
                .add(new SyncCommand())
                .add(new InfoCommand())
                .add(new ImportCommand())
                .add(new ExportCommand())
                .add(new ReloadConfigCommand())
                .build()
                .stream()
                .collect(ImmutableCollectors.toMap(c -> c.getName().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public CompletableFuture<Void> executeCommand(Sender sender, String label, List<String> args) {
        UUID uniqueId = sender.getUniqueId();
        if (this.plugin.getConfiguration().get(ConfigKeys.COMMANDS_RATE_LIMIT) && !sender.isConsole() && !this.userRateLimit.add(uniqueId)) {
            this.plugin.getLogger().warn("User '" + uniqueId + "' is spamming RenaPowered commands. Ignoring further inputs.");
            return CompletableFuture.completedFuture(null);
        }

        SchedulerAdapter scheduler = this.plugin.getBootstrap().getScheduler();
        List<String> argsCopy = new ArrayList<>(args);

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            Message.ALREADY_EXECUTING_COMMAND.send(sender);
        }

        // a reference to the thread being used to execute the command
        AtomicReference<Thread> executorThread = new AtomicReference<>();
        // a reference to the timeout task scheduled to catch if this command takes too long to execute
        AtomicReference<SchedulerTask> timeoutTask = new AtomicReference<>();

        // schedule the actual execution of the command using the command executor service
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // set flags
            executorThread.set(Thread.currentThread());
            this.executingCommand.set(true);

            // actually try to execute the command
            try {
                execute(sender, label, args);
            } catch (Throwable e) {
                // catch any exception
                this.plugin.getLogger().severe("Exception whilst executing command: " + args, e);
            } finally {
                // unset flags
                this.executingCommand.set(false);
                executorThread.set(null);

                // cancel the timeout task
                SchedulerTask timeout;
                if ((timeout = timeoutTask.get()) != null) {
                    timeout.cancel();
                }
            }
        }, this.executor);

        // schedule another task to catch if the command doesn't complete after 10 seconds
        timeoutTask.set(scheduler.asyncLater(() -> {
            if (!future.isDone()) {
                handleCommandTimeout(executorThread, argsCopy);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, List<String> args) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            this.plugin.getLogger().warn("Command execution " + args + " has not completed - is another command execution blocking it?");
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                    .map(el -> "  " + el.toString())
                    .collect(Collectors.joining("\n"));
            this.plugin.getLogger().warn("Command execution " + args + " has not completed. Trace: \n" + stackTrace);
        }
    }

    public boolean hasPermissionForAny(Sender sender) {
        return this.mainCommands.values().stream().anyMatch(c -> c.shouldDisplay() && c.isAuthorized(sender));
    }

    private void execute(Sender sender, String label, List<String> arguments) {
        // Handle no arguments
        if (arguments.isEmpty() || arguments.size() == 1 && arguments.get(0).trim().isEmpty()) {
            sender.sendMessage(Message.prefixed(Component.text()
                    .color(NamedTextColor.DARK_GREEN)
                    .append(Component.text("Running "))
                    .append(Component.text(AbstractRenaPlugin.getPluginName(), NamedTextColor.AQUA))
                    .append(Component.space())
                    .append(Component.text("v" + this.plugin.getBootstrap().getVersion(), NamedTextColor.AQUA))
                    .append(Message.FULL_STOP)
            ));

            if (hasPermissionForAny(sender)) {
                Message.VIEW_AVAILABLE_COMMANDS_PROMPT.send(sender, label);
                return;
            }

            Message.NO_PERMISSION_FOR_SUBCOMMANDS.send(sender);
            return;
        }
        // Look for the main command.
        Command<?> main = this.mainCommands.get(arguments.get(0).toLowerCase(Locale.ROOT));

        // Main command not found
        if (main == null) {
            sendCommandUsage(sender, label);
            return;
        }

        // Check the Sender has permission to use the main command.
        if (!main.isAuthorized(sender)) {
            sendCommandUsage(sender, label);
            return;
        }

        arguments.remove(0); // remove the main command arg.

        // Check the correct number of args were given for the main command
        if (main.getArgumentCheck().test(arguments.size())) {
            main.sendDetailedUsage(sender, label);
            return;
        }

        // Try to execute the command.
        try {
            main.execute(this.plugin, sender, null, new ArgumentList(arguments), label);
        } catch (CommandException e) {
            e.handle(sender, label, main);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public List<String> tabCompleteCommand(Sender sender, List<String> arguments) {
        List<Command<?>> mains = this.mainCommands.values().stream()
                .filter(Command::shouldDisplay)
                .filter(m -> m.isAuthorized(sender))
                .collect(ImmutableCollectors.toList());

        return TabCompleter.create()
                .at(0, CompletionSupplier.startsWith(() -> mains.stream().map(c -> c.getName().toLowerCase(Locale.ROOT))))
                .from(1, partial -> mains.stream()
                        .filter(m -> m.getName().equalsIgnoreCase(arguments.get(0)))
                        .findFirst()
                        .map(cmd -> cmd.tabComplete(this.plugin, sender, new ArgumentList(arguments.subList(1, arguments.size()))))
                        .orElse(Collections.emptyList())
                )
                .complete(arguments);
    }

    private void sendCommandUsage(Sender sender, String label) {
        sender.sendMessage(Message.prefixed(Component.text()
                .color(NamedTextColor.DARK_GREEN)
                .append(Component.text("Running "))
                .append(Component.text(AbstractRenaPlugin.getPluginName(), NamedTextColor.AQUA))
                .append(Component.space())
                .append(Component.text("v" + this.plugin.getBootstrap().getVersion(), NamedTextColor.AQUA))
                .append(Message.FULL_STOP)
        ));

        this.mainCommands.values().stream()
                .filter(Command::shouldDisplay)
                .filter(c -> c.isAuthorized(sender))
                .forEach(c -> sender.sendMessage(Component.text()
                        .append(Component.text('>'))
                        .append(Component.space())
                        .append(Component.text(String.format(c.getUsage(), label)))
                        .build()
                ));
    }
}