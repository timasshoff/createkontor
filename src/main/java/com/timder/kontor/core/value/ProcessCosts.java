package com.timder.kontor.core.value;

import java.util.Map;

/**
 * Maps a process name to what one run of this process costs (before technical progress).
 * @param costs The known processes and their cost per run
 * @param defaultCost The default cost used for processes that are not recognised
 */
public record ProcessCosts(
        Map<String, Double> costs,
        double defaultCost
) {

    public ProcessCosts {
        if (costs == null) throw new IllegalArgumentException("costs must not be null.");
        if (defaultCost < 0) throw new IllegalArgumentException("defaultCost must not be negative");

        costs = Map.copyOf(costs);
    }

    /**
     * The cost of one run of the named process (before technical progress)
     * @param process The process name
     * @return The cost (or the default cost if the process is not found)
     */
    public double of(String process) {
        return costs.getOrDefault(process, defaultCost);
    }

    /*
     * The following code is AI generated. Might be changed in the future.
     */

    public static ProcessCosts standard() {
        return new ProcessCosts(Map.ofEntries(
                Map.entry("crafting", 0.10),
                Map.entry("smelting", 0.40),
                Map.entry("blasting", 0.40),
                Map.entry("smoking", 0.30),
                Map.entry("campfire_cooking", 0.30),
                Map.entry("stonecutting", 0.05),
                Map.entry("pressing", 0.80),
                Map.entry("crushing", 0.80),
                Map.entry("milling", 0.40),
                Map.entry("mixing", 0.60),
                Map.entry("mixing_heated", 1.00),
                Map.entry("mixing_superheated", 1.60),
                Map.entry("cutting", 0.30),
                Map.entry("deploying", 0.40),
                Map.entry("mechanical_crafting", 1.00),
                Map.entry("splashing", 0.30),
                Map.entry("haunting", 0.30),
                Map.entry("filling", 0.20),
                Map.entry("emptying", 0.20),
                Map.entry("sandpaper_polishing", 0.20)
        ), 0.50);
    }
}
