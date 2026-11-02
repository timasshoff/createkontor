package com.timder.kontor.core.company.property;

import java.util.Objects;

public record ChunkKey(DimensionId dimension, int x, int z) {
    public ChunkKey {
        Objects.requireNonNull(dimension, "dimension must not be null.");
    }

    @Override
    public String toString() {
        return dimension + "@" + x + "," + z;
    }
}
