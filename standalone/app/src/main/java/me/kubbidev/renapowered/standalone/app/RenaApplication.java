package me.kubbidev.renapowered.standalone.app;

import lombok.Getter;
import lombok.Setter;
import me.kubbidev.renapowered.standalone.app.integration.CommandExecutor;
import me.kubbidev.renapowered.standalone.app.integration.ShutdownCallback;
import me.kubbidev.renapowered.standalone.app.util.TerminalInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The RenaPowered standalone application.
 */
public class RenaApplication implements AutoCloseable {

    /**
     * A logger instance
     */
    public static final Logger LOGGER = LogManager.getLogger(RenaApplication.class);

    /**
     * A callback to shutdown the application via the loader bootstrap.
     */
    private final ShutdownCallback shutdownCallback;

    // called before start()
    /**
     * A command executor interface to run RenaPowered commands
     */
    @Getter
    @Setter
    private CommandExecutor commandExecutor;

    /**
     * If the application is running
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    public RenaApplication(ShutdownCallback shutdownCallback) {
        this.shutdownCallback = shutdownCallback;
    }

    public void start() {
        TerminalInterface terminal = new TerminalInterface(this, this.commandExecutor);
        terminal.start(); // blocking
    }

    public void requestShutdown() {
        this.shutdownCallback.shutdown();
    }

    public AtomicBoolean runningState() {
        return this.running;
    }

    @Override
    public void close() {
        this.running.set(false);
    }

    public String getVersion() {
        return "@version@";
    }
}
