package me.kubbidev.renapowered.standalone.app.integration;

/**
 * Shutdown callback for the whole standalone app.
 * (in practice this is always implemented by the StandaloneLoader class)
 */
public interface ShutdownCallback {

    void shutdown();
}