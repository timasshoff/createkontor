package com.timder.kontor.core.value;

import java.util.List;

/**
 * The input to a recipe.
 * @param alternatives A set of items that make up the ingredient. They can all be used as alternatives.
 * @param quantity The amount that makes up this ingredient.
 */
public record RecipeInput(
        List<ItemId> alternatives,
        double quantity
) {
    public RecipeInput {
        if (alternatives == null) {
            throw new IllegalArgumentException("alternatives must not be null");
        }

        if (alternatives.isEmpty()) {
            throw new IllegalArgumentException("alternatives must not be empty");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
