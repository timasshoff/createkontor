package com.timder.kontor.core.value;

public record ItemId(String value) {

    public ItemId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
