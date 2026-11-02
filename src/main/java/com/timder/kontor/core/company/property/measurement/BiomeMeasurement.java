package com.timder.kontor.core.company.property.measurement;

import com.timder.kontor.core.company.property.BiomeFrequencies;
import com.timder.kontor.core.company.property.BiomeId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A running biome measurement of one dimension
 */
public final class BiomeMeasurement {

    private final MeasurementGrid grid;
    private final Map<BiomeId, Long> counts = new LinkedHashMap<>();
    private long recorded = 0;

    public BiomeMeasurement(MeasurementGrid grid) {
        this.grid = Objects.requireNonNull(grid, "grid must not be null.");
    }

    public MeasurementGrid grid() {
        return grid;
    }

    public boolean isDone() {
        return recorded >= grid.pointCount();
    }

    public long recorded() {
        return recorded;
    }

    public long remaining() {
        return grid.pointCount() - recorded;
    }

    public double progress() {
        return (double) recorded / (double) grid.pointCount();
    }

    public int nextX() {
        requireNotDone();
        return grid.xAt(recorded);
    }

    public int nextZ() {
        requireNotDone();
        return grid.zAt(recorded);
    }

    public void record(BiomeId biome) {
        Objects.requireNonNull(biome, "biome must not be null.");
        requireNotDone();
        counts.merge(biome, 1L, Long::sum);
        recorded++;
    }

    public MeasuredFrequencies result() {
        if (!isDone()) {
            throw new IllegalStateException("The measurement is not done: " + remaining() + " points are missing.");
        }
        return new MeasuredFrequencies(grid, new BiomeFrequencies(counts));
    }

    private void requireNotDone() {
        if (isDone()) {
            throw new IllegalStateException("The measurement is done.");
        }
    }
}
