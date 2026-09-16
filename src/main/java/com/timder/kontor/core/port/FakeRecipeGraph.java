package com.timder.kontor.core.port;

import com.lowdragmc.lowdraglib2.gui.ui.elements.GraphView;
import com.timder.kontor.core.value.Ingredient;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.RecipeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A recipe graph built by hand for testing purposes.
 */
public final class FakeRecipeGraph implements RecipeGraph {

    private final Map<ItemId, List<RecipeNode>> recipes = new HashMap<>();

    public FakeRecipeGraph add(RecipeNode recipe) {
        recipes.computeIfAbsent(recipe.output(), key -> new ArrayList<>()).add(recipe);
        return this;
    }

    // Convenience for one input one output
    public FakeRecipeGraph add(String output, double yield, String ingredient, double quantity, String process) {
        return add(new RecipeNode(new ItemId(output), yield,
                List.of(new Ingredient(new ItemId(ingredient), quantity)), process));
    }

    @Override
    public List<RecipeNode> recipesFor(ItemId output) {
        return recipes.getOrDefault(output, List.of());
    }
}
