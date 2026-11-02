package com.timder.kontor.core.company.property;

import java.util.Objects;

public record PropertyHolding(ChunkKey chunk, BiomeId biome, HoldingKind kind, long acquiredDay, boolean cancelled) {

    public PropertyHolding {
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(biome, "biome must not be null.");
        Objects.requireNonNull(kind, "kind must not be null.");
        if (acquiredDay < 0) throw new IllegalArgumentException("acquiredDay must not be negative.");
        if (cancelled && kind != HoldingKind.RENTED) {
            throw new IllegalArgumentException("Only a rented chunk can be cancelled.");
        }
    }

    public static PropertyHolding bought(ChunkKey chunk, BiomeId biome, long day) {
        return new PropertyHolding(chunk, biome, HoldingKind.OWNED, day, false);
    }

    public static PropertyHolding rented(ChunkKey chunk, BiomeId biome, long day) {
        return new PropertyHolding(chunk, biome, HoldingKind.RENTED, day, false);
    }

    public boolean isOwned() {
        return kind == HoldingKind.OWNED;
    }

    public boolean isRented() {
        return kind == HoldingKind.RENTED;
    }

    public PropertyHolding cancel() {
        if (!isRented()) throw new IllegalStateException("Only a rented chunk can be cancelled.");
        return new PropertyHolding(chunk, biome, kind, acquiredDay, true);
    }

    public PropertyHolding reactivate() {
        return new PropertyHolding(chunk, biome, kind, acquiredDay, false);
    }

    public PropertyHolding boughtOn(long day) {
        return bought(chunk, biome, day);
    }
}
