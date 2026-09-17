package com.timder.kontor.core.value;


/**
 * A possible output of a recipe.
 * @param item The output item
 * @param yield The expected amount produced per recipe-run
 */
public record RecipeOutput(
        ItemId item,
        double yield
) {

    public RecipeOutput {
        if (item == null) throw new IllegalArgumentException("item must not be null.");
        if (yield <= 0) throw new IllegalArgumentException("yield must be positive.");
    }

}
