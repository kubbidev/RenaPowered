package me.kubbidev.renapowered.common.worker.event.intercept;

import net.dv8tion.jda.api.events.Event;

import java.util.List;

public abstract class BaseEventFilterFactory<T extends Event> implements EventFilterFactory<T> {

    private final ThreadLocal<FilterChainImpl<T>> chains = new ThreadLocal<>();
    private final List<Filter<T>> filterList;

    public BaseEventFilterFactory(List<Filter<T>> filterList) {
        this.filterList = filterList;
    }

    @Override
    public FilterChain<T> createChain(T event) {
        if (event == null) {
            return null;
        }
        if (this.filterList.isEmpty()) {
            return null;
        }
        FilterChainImpl<T> chain = this.chains.get();
        if (chain == null) {
            chain = new FilterChainImpl<>(this.filterList);
            this.chains.set(chain);
        }
        chain.reset();
        return chain;
    }
}