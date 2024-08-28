package me.kubbidev.renapowered.common.worker.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerTask;
import me.kubbidev.renapowered.common.util.ComponentSerializer;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import me.kubbidev.renapowered.common.worker.commands.*;
import me.kubbidev.renapowered.common.worker.commands.ranking.RChannelCommand;
import me.kubbidev.renapowered.common.worker.commands.ranking.REnabledCommand;
import me.kubbidev.renapowered.common.worker.event.EventHandler;
import me.kubbidev.renapowered.common.worker.listener.DiscordEventListener;
import me.kubbidev.renapowered.common.util.StackTracePrinter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@EventHandler
public class InteractionManager extends DiscordEventListener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("renapowered-interaction-executor")
            .build()
    );

    // how many lines should we include in each stack trace send as a message
    private static final int STACK_TRUNCATION = 15;

    private static final StackTracePrinter CHAT_UNFILTERED_PRINTER = StackTracePrinter.builder()
            .truncateLength(STACK_TRUNCATION)
            .build();

    private final AtomicBoolean executingCommand = new AtomicBoolean(false);

    @Getter(onMethod_ = @VisibleForTesting)
    private final Map<String, InteractionCommand> commands;

    public InteractionManager(RenaPlugin plugin) {
        super(plugin);
        this.commands = ImmutableList.<InteractionCommand>builder()
                .add(new AboutCommand())
                .add(new PingCommand())
                .add(new ProfileCommand())
                .add(new RChannelCommand())
                .add(new REnabledCommand())
                .build()
                .stream()
                .collect(ImmutableCollectors.toMap(command -> getDiscordCommand(command).name().toLowerCase(Locale.ROOT), Function.identity()));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        execute(event, event.getName());
    }

    public DiscordCommand getDiscordCommand(InteractionCommand command) {
        DiscordCommand annotation = command.getClass().getAnnotation(DiscordCommand.class);
        if (annotation == null) {
            throw new NoSuchElementException("The command: " + command.getClass().getSimpleName()
                    + " is not annotated with the DiscordCommand annotation");
        }
        return annotation;
    }

    public void buildCommands(JDA jda) {
        CommandListUpdateAction action = jda.updateCommands();

        for (InteractionCommand command : this.commands.values()) {
            DiscordCommand annotation = getDiscordCommand(command);
            if (annotation.visible()) {
                action = action.addCommands(command.getSlashCommand());
            }
        }
        action.queue();
    }

    public CompletableFuture<Void> execute(CommandInteraction interaction, String label) {
        SchedulerAdapter scheduler = this.plugin.getBootstrap().getScheduler();

        @Nullable Locale guildLocale;
        if (interaction.isFromGuild()) {
            guildLocale = interaction.getGuildLocale().toLocale();
        } else {
            guildLocale = null;
        }

        MessageChannel channel = interaction.getMessageChannel();

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            this.plugin.getDiscordService().sendMessage(channel, Message.ALREADY_EXECUTING_COMMAND.build(), guildLocale);
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
                executeCommand(interaction, guildLocale, label);
            } catch (Throwable e) {
                sendCommandTrace(interaction, guildLocale, e);
                // catch any exception
                this.plugin.getLogger().severe("Exception whilst executing command: " + label, e);
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
                handleCommandTimeout(executorThread, label);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void sendCommandTrace(CommandInteraction interaction, Locale guildLocale, Throwable e) {
        List<ComponentLike> trace = new ArrayList<>();
        trace.add(Message.COMMAND_EXCEPTION_TRACE_MESSAGE.build());
        trace.add(Component.text("```"));

        trace.add(Component.text(e.getMessage()));
        Consumer<StackTraceElement> printer = StackTracePrinter.elementToString(str ->
                trace.add(Component.text("  at " + str))
        );

        int overflow = CHAT_UNFILTERED_PRINTER.process(e.getStackTrace(), printer);
        if (overflow != 0) {
            trace.add(Message.COMMAND_EXCEPTION_TRACE_OVERFLOW.build(overflow));
        }
        trace.add(Component.text("```"));
        Component stackTrace = Component.join(JoinConfiguration.newlines(), trace);

        interaction.deferReply(true).queue(hook -> this.plugin.getDiscordService().sendMessageSilent(
                hook::sendMessage,
                ComponentSerializer.serialize(stackTrace, guildLocale)
        ));
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, String label) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            this.plugin.getLogger().warn("Command execution " + label + " has not completed - is another command execution blocking it?");
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                    .map(el -> "  " + el.toString())
                    .collect(Collectors.joining("\n"));
            this.plugin.getLogger().warn("Command execution " + label + " has not completed. Trace: \n" + stackTrace);
        }
    }

    private void executeCommand(CommandInteraction interaction, @Nullable Locale guildLocale, String label) {
        // Look for the main command.
        InteractionCommand command = this.commands.get(label.toLowerCase(Locale.ROOT));

        // Command not found
        if (command == null) {
            this.plugin.getDiscordService().sendMessage(interaction.getMessageChannel(), Message.COMMAND_NOT_RECOGNISED.build(), guildLocale);
            return;
        }
        CommandContext context = new CommandContext(this.plugin, interaction);
        command.onPerform(context);
    }
}
