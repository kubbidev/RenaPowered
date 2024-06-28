package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FilterChainImpl<T extends Event> implements FilterChain<T> {

    private static final int INCREMENT = 10;

    @SuppressWarnings("unchecked")
    private Filter<T>[] filters = (Filter<T>[]) new Filter[0];

    /**
     * The int which is used to maintain the current position
     * in the filter chain.
     */
    private int position = 0;


    /**
     * The int which gives the current number of filters in the chain.
     */
    private int size = 0;

    public FilterChainImpl(@NotNull Collection<Filter<T>> filters) {
        filters.forEach(this::addFilter);
    }

    @Override
    public void doFilter(T e) {
        if (this.position < this.size) {
            Filter<T> filter = this.filters[this.position++];
            if (filter != null) {
                filter.doFilter(e, this);
            }
        }
    }

    @Override
    public void reset() {
        this.position = 0;
    }

    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param newFilter The Filter for the event to be executed
     */
    @SuppressWarnings("unchecked")
    void addFilter(Filter<T> newFilter) {
        // prevent the same filter being added multiple times
        for (Filter<T> filter : this.filters) {
            if (filter == newFilter) {
                return;
            }
        }

        if (this.size == this.filters.length) {
            Filter<T>[] newFilters = (Filter<T>[]) new Filter[this.size + INCREMENT];
            System.arraycopy(this.filters, 0, newFilters, 0, this.size);
            this.filters = newFilters;
        }
        this.filters[this.size++] = newFilter;
    }
}