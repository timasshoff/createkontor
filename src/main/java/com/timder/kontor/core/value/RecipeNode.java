package com.timder.kontor.core.value;

import java.util.List;

/**
 * A recipe flattened to the output, the amount of output, the input and which process it uses.
 * Does not include byproducts yet.
 * @param output What this recipe outputs
 * @param yield How much this recipe outputs
 * @param inputs Which inputs this recipe requires
 * @param process Identifier for the processing step this recipe uses
 */
public record RecipeNode(
        ItemId output,
        double yield,
        List<Ingredient> inputs,
        String process
) {

    public RecipeNode {
        if (output == null) throw new IllegalArgumentException("output must not be null.");
        if (yield <= 0) throw new IllegalArgumentException("yield must be positive.");
        if (inputs == null) throw new IllegalArgumentException("inputs must not be null.");
        if (process == null || process.isBlank()) throw new IllegalArgumentException("process must not be blank");

        inputs = List.copyOf(inputs);
    }

}
