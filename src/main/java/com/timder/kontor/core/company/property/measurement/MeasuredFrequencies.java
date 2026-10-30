package com.timder.kontor.core.company.property.measurement;

import com.timder.kontor.core.company.property.BiomeFrequencies;
import java.util.Objects;

/**
 * Result of a measurement of one dimension.
 * @param grid The grid that was sampled
 * @param frequencies Hits per biome
 */
public record MeasuredFrequencies(MeasurementGrid grid, BiomeFrequencies frequencies) {

    public MeasuredFrequencies {
        Objects.requireNonNull(grid, "grid must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");
        if (frequencies.total() != grid.pointCount()) {
            throw new IllegalArgumentException("The grid has " + grid.pointCount()
                    + " points, but the frequencies count " + frequencies.total() + " samples.");
        }
    }
}
