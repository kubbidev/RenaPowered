package me.kubbidev.renapowered.common.worker.event;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.ranking.RankingFilter;
import me.kubbidev.renapowered.common.worker.event.intercept.EventFilterFactory;
import me.kubbidev.renapowered.common.worker.event.intercept.Filter;
import me.kubbidev.renapowered.common.worker.event.intercept.FilterChain;
import me.kubbidev.renapowered.common.worker.event.intercept.MessageFilterFactory;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager implements IEventManager {
    private final RenaPlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("renapowered-event-executor")
            .build()
    );
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * A map of factory for the creation and caching of filters and creation of filter chains.
     */
    private final Map<Class<?>, EventFilterFactory<?>> filterFactoryMap = new ConcurrentHashMap<>();

    public EventManager(RenaPlugin plugin) {
        this.plugin = plugin;

        // register filter factories
        registerFilterFactories(new MessageFilterFactory(ImmutableList.<Filter<MessageReceivedEvent>>builder()
                // add filters here..
                .add(new RankingFilter(plugin))
                .build()));
    }

    public void registerFilterFactories(Iterable<EventFilterFactory<?>> factories) {
        for (EventFilterFactory<?> e : factories) {
            registerFilterFactories(e);
        }
    }

    public void registerFilterFactories(EventFilterFactory<?> factory) {
        this.filterFactoryMap.putIfAbsent(factory.getType(), factory);
    }

    @Override
    public void register(@NotNull Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        registerListeners(Collections.singletonList((EventListener) listener));
    }

    @Override
    public void unregister(@NotNull Object listener) {
        synchronized (this.listeners) {
            this.listeners.remove((EventListener) listener);
        }
    }

    @Override
    public void handle(@NotNull GenericEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                loopListeners(event);
            } catch (Exception e) {
                this.plugin.getLogger().severe("Event manager caused an uncaught exception", e);
            }
        }, this.executor);
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    private void registerListeners(Collection<? extends EventListener> listeners) {
        synchronized (this.listeners) {
            Set<EventListener> listenerSet = new HashSet<>(this.listeners);
            listenerSet.addAll(listeners);

            this.listeners.clear();
            this.listeners.addAll(listenerSet);
            this.listeners.sort(this::compareListeners);
        }
    }

    private int compareListeners(EventListener first, EventListener second) {
        return getPriority(first) - getPriority(second);
    }

    private int getPriority(EventListener eventListener) {
        EventHandler annotation = eventListener.getClass().getAnnotation(EventHandler.class);
        if (annotation == null) {
            throw new NoSuchElementException("The event listener: " + eventListener.getClass().getSimpleName()
                    + " is not annotated with the EventHandler annotation");
        }
        return annotation.priority();
    }

    private void loopListeners(GenericEvent event) {
        if (event instanceof MessageReceivedEvent && ((MessageReceivedEvent) event).isFromGuild()) {
            dispatchChain((MessageReceivedEvent) event);
        }

        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable throwable) {
                this.plugin.getLogger().severe("[" + listener.getClass().getSimpleName() +
                        "] had an uncaught exception for handling " + event, throwable);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void dispatchChain(T event) {
        EventFilterFactory<T> factory = (EventFilterFactory<T>) filterFactoryMap.get(MessageReceivedEvent.class);
        if (factory == null) {
            return;
        }
        FilterChain<T> chain = factory.createChain(event);
        if (chain == null) {
            return;
        }

        try {
            chain.doFilter(event);
        } catch (Exception e) {
            this.plugin.getLogger().severe("Could not process filter chain", e);
        }
    }
}
