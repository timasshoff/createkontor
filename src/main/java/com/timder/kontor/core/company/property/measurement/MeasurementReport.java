package com.timder.kontor.core.company.property.measurement;

import com.timder.kontor.core.company.property.BiomeFrequencies;
import com.timder.kontor.core.company.property.BiomeId;
import java.util.*;

public record MeasurementReport(List<Row> hit, List<BiomeId> neverHit) {

    public record Row(BiomeId biome, long samples, double share) {}

    /**
     * @param frequencies The measurement of a dimension
     * @param possibleBiomes All possible biomes
     * @return The report
     */
    public static MeasurementReport of(BiomeFrequencies frequencies, Collection<BiomeId> possibleBiomes) {
        Objects.requireNonNull(frequencies, "frequencies must not be null.");
        Objects.requireNonNull(possibleBiomes, "possibleBiomes must not be null.");

        List<Map.Entry<BiomeId, Long>> entries =
                new ArrayList<>(frequencies.samples().entrySet());
        entries.sort(Map.Entry.<BiomeId, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(entry -> entry.getKey().value()));
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<BiomeId, Long> entry : entries) {
            rows.add(new Row(entry.getKey(), entry.getValue(), frequencies.share(entry.getKey())));
        }

        Set<BiomeId> hitBiomes = frequencies.samples().keySet();
        List<BiomeId> missing = new ArrayList<>();
        for (BiomeId biome : Set.copyOf(possibleBiomes)) {
            if (!hitBiomes.contains(biome)) {
                missing.add(biome);
            }
        }
        missing.sort(Comparator.comparing(BiomeId::value));
        return new MeasurementReport(rows, missing);
    }
}
