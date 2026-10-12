package com.timder.kontor.game.wiki;

import java.util.*;

public final class WikiRegistry {
    private final Object lock = new Object();
    private final Map<String, WikiEntry> entries = new LinkedHashMap<>();
    private final Map<String, WikiCategory> categories = new HashMap<>();

    /**
     * @param entry The entry to add
     * @throws IllegalArgumentException If the id is taken, or the category id is used with a different category
     */
    public void register(WikiEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        synchronized (lock) {
            if (entries.containsKey(entry.id())) {
                throw new IllegalArgumentException("A wiki entry with the id " + entry.id() + " is already registered.");
            }
            WikiCategory known = categories.get(entry.category().id());
            if (known != null && !known.equals(entry.category())) {
                throw new IllegalArgumentException("The wiki category id " + entry.category().id() + " is already used with a different title (entry " + entry.id() + ").");
            }
            categories.putIfAbsent(entry.category().id(), entry.category());
            entries.put(entry.id(), entry);
        }
    }

    /**
     * @return A snapshot in registration order; later registrations do not change it
     */
    public List<WikiEntry> entries() {
        synchronized (lock) {
            return List.copyOf(entries.values());
        }
    }
}
