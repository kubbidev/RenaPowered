package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.Event;

/**
 * A filter is an object that performs filtering tasks Discord events
 */
public interface Filter<T extends Event> {

    /**
     * The <code>doFilter</code> method of the Filter is called by the event
     * manager each time a event passed through the chain.
     * <p>
     *
     * @param event The event to process
     * @param chain Provides access to the next filter in the chain for this
     *              filter to pass the request and response to for further
     *              processing
     */
    void doFilter(T event, FilterChain<T> chain);
}