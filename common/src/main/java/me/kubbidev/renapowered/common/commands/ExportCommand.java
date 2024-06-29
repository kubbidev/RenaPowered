package me.kubbidev.renapowered.common.commands;

import me.kubbidev.renapowered.common.backup.Exporter;
import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.abstraction.SingleCommand;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.Predicates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExportCommand extends SingleCommand {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
            .withZone(ZoneId.systemDefault());

    private final AtomicBoolean running = new AtomicBoolean(false);

    public ExportCommand() {
        super(CommandSpec.EXPORT, "Export", CommandPermission.EXPORT, Predicates.alwaysFalse());
    }

    @Override
    public void execute(RenaPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        if (this.running.get()) {
            Message.EXPORT_ALREADY_RUNNING.send(sender);
            return;
        }

        Path dataDirectory = plugin.getBootstrap().getDataDirectory();
        Path path;
        if (args.isEmpty()) {
            path = dataDirectory.resolve("renapowered-" + DATE_FORMAT.format(Instant.now()) + ".json.gz");
        } else {
            path = dataDirectory.resolve(args.get(0) + ".json.gz");
        }

        if (!path.getParent().equals(dataDirectory)) {
            Message.FILE_NOT_WITHIN_DIRECTORY.send(sender, path.toString());
            return;
        }

        if (Files.exists(path)) {
            Message.EXPORT_FILE_ALREADY_EXISTS.send(sender, path.toString());
            return;
        }

        try {
            Files.createFile(path);
        } catch (IOException e) {
            Message.EXPORT_FILE_FAILURE.send(sender);
            plugin.getLogger().warn("Error whilst writing to the file", e);
            return;
        }

        if (!Files.isWritable(path)) {
            Message.EXPORT_FILE_NOT_WRITABLE.send(sender, path.toString());
            return;
        }

        if (!this.running.compareAndSet(false, true)) {
            Message.EXPORT_ALREADY_RUNNING.send(sender);
            return;
        }

        Exporter exporter = new Exporter.SaveFile(plugin, sender, path);

        // run the exporter in its own thread.
        plugin.getBootstrap().getScheduler().executeAsync(() -> {
            try {
                exporter.run();
            } finally {
                this.running.set(false);
            }
        });
    }
}