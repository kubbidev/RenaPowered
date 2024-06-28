package me.kubbidev.renapowered.standalone.app.util;

import me.kubbidev.renapowered.standalone.app.RenaApplication;
import me.kubbidev.renapowered.standalone.app.integration.CommandExecutor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * The terminal/console-style interface presented to the user.
 */
public class TerminalInterface extends SimpleTerminalConsole {
    private final RenaApplication application;
    private final CommandExecutor commandExecutor;

    public TerminalInterface(RenaApplication application, CommandExecutor commandExecutor) {
        this.application = application;
        this.commandExecutor = commandExecutor;
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder
                .appName("RenaPowered")
                .completer(this::completeCommand)
        );
    }

    @Override
    protected boolean isRunning() {
        return this.application.runningState().get();
    }

    @Override
    protected void shutdown() {
        this.application.requestShutdown();
    }

    @Override
    protected void runCommand(String command) {
        command = stripSlashRP(command);

        if (command.equalsIgnoreCase("stop") || command.equalsIgnoreCase("end")) {
            this.application.requestShutdown();
            return;
        }

        this.commandExecutor.execute(command);
    }

    private void completeCommand(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String cmdLine = stripSlashRP(line.line());

        for (String suggestion : this.commandExecutor.tabComplete(cmdLine)) {
            candidates.add(new Candidate(suggestion));
        }
    }

    private static String stripSlashRP(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.startsWith("rp ")) {
            command = command.substring(3);
        }
        return command;
    }
}