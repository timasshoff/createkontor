package com.timder.kontor.core.value;

import java.util.Map;
import java.util.Set;

/**
 * What the value engine found.
 * Is a theoretical production cost computed from the recipe tree.
 *
 * @param referenceCost The cheapest cost per unit found for each computed good. Raw materials are not included
 * @param standardRecipe The recipes that produced the {@link #referenceCost}
 * @param depth How many steps lie between a good and the raw materials
 * @param unvalued Items that have not received a final cost
 */
public record ValueResult(
    Map<ItemId, Double> referenceCost,
    Map<ItemId, RecipeNode> standardRecipe,
    Map<ItemId, Integer> depth,
    Set<ItemId> unvalued
) {

    public ValueResult {
        referenceCost = Map.copyOf(referenceCost);
        standardRecipe = Map.copyOf(standardRecipe);
        depth = Map.copyOf(depth);
        unvalued = Set.copyOf(unvalued);
    }

}
