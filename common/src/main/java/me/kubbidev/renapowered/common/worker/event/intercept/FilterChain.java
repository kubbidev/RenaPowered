package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.Event;

/**
 * A FilterChain is an object giving a view into the invocation chain of a filtered event for a resource.
 * <p>
 * Filters use the FilterChain to invoke the next filter in the chain, or if the
 * calling filter is the last filter in the chain, to invoke the resource at the
 * end of the chain.
 *
 * @see Filter
 **/
public interface FilterChain<T extends Event> {

    /**
     * Causes the next filter in the chain to be invoked, or if the calling
     * filter is the last filter in the chain, causes the resource at the end of
     * the chain to be invoked.
     *
     * @param event the event to pass along the chain.
     */
    void doFilter(T event);

    /**
     * Resets chain to be ready to handle next request
     */
    void reset();
}