package com.timder.kontor.core.company.property;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * How often each biome of one dimension was hit when measuring
 * @param samples The number of samples per biome
 */
public record BiomeFrequencies(Map<BiomeId, Long> samples) {

    public static final BiomeFrequencies EMPTY = new BiomeFrequencies(Map.of());

    public BiomeFrequencies {
        Objects.requireNonNull(samples, "samples must not be null.");
        Map<BiomeId, Long> kept = new LinkedHashMap<>();
        long total = 0;
        for (Map.Entry<BiomeId, Long> entry : samples.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "biome must not be null.");
            Objects.requireNonNull(entry.getValue(), "The sample count of " + entry.getKey() + " must not be null.");
            long count = entry.getValue();
            if (count < 0) {
                throw new IllegalArgumentException("The sample count of " + entry.getKey() + " must not be negative.");
            }
            if (count > 0) {
                try {
                    total = Math.addExact(total, count);
                } catch (ArithmeticException e) {
                    throw new IllegalArgumentException("The sum of all sample counts must fit into a long.");
                }
                kept.put(entry.getKey(), count);
            }
        }
        samples = Map.copyOf(kept);
    }

    /**
     * @param biome The biome
     * @return The number of samples that hit this biome
     */
    public long count(BiomeId biome) {
        Objects.requireNonNull(biome, "biome must not be null.");
        return samples.getOrDefault(biome, 0L);
    }

    /**
     * @return The total number of samples
     */
    public long total() {
        long total = 0;
        for (long count : samples.values()) {
            total += count;
        }
        return total;
    }

    public boolean isEmpty() {
        return samples.isEmpty();
    }

    public double share(BiomeId biome) {
        long total = total();
        if (total == 0) {
            return 0.0;
        }
        return (double) count(biome) / (double) total;
    }
}
