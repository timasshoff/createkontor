package com.timder.kontor.core.port;

import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.RecipeNode;

import java.util.List;

public interface RecipeGraph {
    List<RecipeNode> recipesFor(ItemId output);
}
