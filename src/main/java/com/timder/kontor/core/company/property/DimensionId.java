package com.timder.kontor.core.company.property;

public record DimensionId(String value) {
    public DimensionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
