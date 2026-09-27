package com.timder.kontor.util;

import java.util.ArrayList;
import java.util.List;

public final class ObservableList<T> {
    private List<T> values = List.of();
    private final List<Runnable> listeners = new ArrayList<>();

    public List<T> get() {
        return values;
    }

    public void set(List<T> values) {
        this.values = List.copyOf(values);
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }
}
