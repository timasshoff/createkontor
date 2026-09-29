package com.timder.kontor.util;

import java.util.ArrayList;
import java.util.List;

public final class ObservableValue<T> {
    private T value;
    private final List<Runnable> listeners = new ArrayList<>();

    public ObservableValue(T initial) {
        this.value = initial;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }
}
