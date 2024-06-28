package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.Event;

/**
 * Factory for the creation and caching of Filters and creation
 * of Filter Chains.
 */
public interface EventFilterFactory<T extends Event> {

    /**
     * Construct a FilterChain implementation that will wrap the execution of
     * the specified servlet instance.
     *
     * @param event The event we are processing
     * @return The configured FilterChain instance or null if none is to be
     * executed.
     */
    FilterChain<T> createChain(T event);

    /**
     * Returns a type of event to process in this chain
     *
     * @return Type of event to process in this chain
     */
    Class<T> getType();
}