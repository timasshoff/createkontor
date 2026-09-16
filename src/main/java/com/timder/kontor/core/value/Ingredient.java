package com.timder.kontor.core.value;

public record Ingredient(
        ItemId item,
        double quantity
) {
    public Ingredient {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
