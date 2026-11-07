/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public final class EventHook<E> {

    private static final Priority[] PRIORITIES = Priority.values();

    private final String name;
    @SuppressWarnings("unchecked")
    private final List<Consumer<? super E>>[] listeners = new List[PRIORITIES.length];

    public EventHook(@Nonnull final String name) {
        this.name = Objects.requireNonNull(name);
        for (int i = 0; i < this.listeners.length; i++)
            this.listeners[i] = new CopyOnWriteArrayList<>();
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public Subscription register(@Nonnull final Consumer<? super E> listener) {
        return register(Priority.NORMAL, listener);
    }

    @Nonnull
    public Subscription register(@Nonnull final Priority priority, @Nonnull final Consumer<? super E> listener) {
        Objects.requireNonNull(listener);
        final List<Consumer<? super E>> list = this.listeners[priority.ordinal()];
        list.add(listener);
        return () -> list.remove(listener);
    }

    public boolean hasListeners() {
        for (final List<Consumer<? super E>> list : this.listeners)
            if (!list.isEmpty())
                return true;
        return false;
    }

    public boolean hasListeners(@Nonnull final Priority priority) {
        return !this.listeners[priority.ordinal()].isEmpty();
    }

    public void post(@Nonnull final Priority priority, @Nonnull final E event) {
        for (final Consumer<? super E> listener : this.listeners[priority.ordinal()]) {
            listener.accept(event);
            if (event instanceof Cancellable c && c.isCanceled())
                return;
        }
    }

    @Nonnull
    public E post(@Nonnull final E event) {
        for (final Priority priority : PRIORITIES) {
            post(priority, event);
            if (event instanceof Cancellable c && c.isCanceled())
                break;
        }
        return event;
    }

    @Override
    public String toString() {
        return "EventHook[" + this.name + "]";
    }
}
