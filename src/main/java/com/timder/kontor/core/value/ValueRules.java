package com.timder.kontor.core.value;

import com.timder.kontor.core.port.RecipeGraph;

import java.util.*;

public final class ValueRules {

    /**
     * How deep the algorithm goes into the recipe tree for each item
     */
    public static final int MAX_DISCOVERY_DEPTH = 12;

    /**
     * How many times the values are getting recomputed before stopping
     */
    public static final int MAX_ITERATIONS = 50;

    /**
     * An item counts as computed once a round changes its cost by less than this value
     */
    public static final double CONVERGENCE_THRESHOLD = 0.001;

    /**
     * Finds every item that COULD matter for the given roots.
     * Follows the recipes ingredients backwards up to {@link #MAX_DISCOVERY_DEPTH}.
     * @param roots Usually every product with a market
     * @param graph The recipe graph to search
     * @return Every finding, roots included
     */
    public static Set<ItemId> discover(Collection<ItemId> roots, RecipeGraph graph) {
        Set<ItemId> scope = new HashSet<>(roots);
        Set<ItemId> frontier = new HashSet<>(roots);

        for (int hop = 0; hop < MAX_DISCOVERY_DEPTH && !frontier.isEmpty(); hop++) {
            Set<ItemId> next = new HashSet<>();
            for (ItemId item : frontier) {
                for (RecipeNode recipeNode : graph.recipesFor(item)) {
                    for (Ingredient ingredient : recipeNode.inputs()) {
                        if (scope.add(ingredient.item())) {
                            next.add(ingredient.item());
                        }
                    }
                }
            }
            frontier = next;
        }

        return scope;
    }

    /**
     * Computes a reference cost for every item that is not already externally known.
     * @param scope A reference cost will be computed for these items
     * @param graph The recipe graph to search
     * @param processCosts The cost per process
     * @param leafValues Raw materials and any other fixed values. Items in this map will never have its recipes evaluated.
     * @param marketPrices Current market price of every item that has a market. Reference cost may differ from market price.
     * @param technicalProgress The technical progress
     * @return Reference costs, the recipe each reference cost came from, depths and not-valued items
     */
    public static ValueResult computeValues(Set<ItemId> scope,
                                            RecipeGraph graph,
                                            ProcessCosts processCosts,
                                            Map<ItemId, Double> leafValues,
                                            Map<ItemId, Double> marketPrices,
                                            double technicalProgress) {
        Map<ItemId, Double> referenceCost = new HashMap<>();
        Map<ItemId, RecipeNode> standardRecipe = new HashMap<>();
        Map<ItemId, Integer> depth = new HashMap<>();

        for (ItemId leaf : leafValues.keySet()) {
            depth.put(leaf, 0);
        }

        for (int round = 0; round < MAX_ITERATIONS; round++) {
            boolean changed = false;

            for (ItemId item : scope) {
                if (leafValues.containsKey(item)) {
                    continue;
                }

                double bestCost = Double.POSITIVE_INFINITY;
                RecipeNode bestRecipe = null;
                int bestDepth = 0;

                for (RecipeNode recipe : graph.recipesFor(item)) {
                    if (recipe.yield() <= 0) {
                        continue;
                    }

                    double ingredientCost = 0.0;
                    int deepestIngredient = -1;
                    boolean usable = true;

                    for (Ingredient ingredient : recipe.inputs()) {
                        double value = ingredientValue(ingredient.item(), referenceCost, marketPrices, leafValues);

                        if (Double.isInfinite(value)) {
                            usable = false;
                            break;
                        }

                        ingredientCost += ingredient.quantity() * value;
                        deepestIngredient = Math.max(deepestIngredient, depth.getOrDefault(ingredient.item(), -1));
                    }

                    if (!usable) {
                        continue;
                    }

                    double cost = CostRules.recipeCost(ingredientCost, processCosts.of(recipe.process()), technicalProgress, recipe.yield());

                    if (cost < bestCost) {
                        bestCost = cost;
                        bestRecipe = recipe;
                        bestDepth = deepestIngredient + 1;
                    }
                }

                if (bestRecipe != null) {
                    Double previous = referenceCost.get(item);
                    if (previous == null || Math.abs(bestCost - previous) / Math.max(previous, 1e-9) > CONVERGENCE_THRESHOLD) {
                        changed = true;
                    }
                    referenceCost.put(item, bestCost);
                    standardRecipe.put(item, bestRecipe);
                    depth.put(item, bestDepth);
                }
            }

            if (!changed) {
                break;
            }
        }

        Set<ItemId> unvalued = new HashSet<>();
        for (ItemId item : scope) {
            if (!leafValues.containsKey(item) && !referenceCost.containsKey(item)) {
                unvalued.add(item);
            }
        }

        return new ValueResult(referenceCost, standardRecipe, depth, unvalued);
    }

    /**
     * The value to use for an item when it appears as an ingredient.
     * Either its external leaf value, otherwise its current market price.
     * If there is no market price, the just-computed reference cost will be used.
     * Positive Infinity if none of these are known.
     * @param item
     * @param referenceCost
     * @param marketPrices
     * @param leafValues
     * @return
     */
    public static double ingredientValue(ItemId item, Map<ItemId, Double> referenceCost, Map<ItemId, Double> marketPrices,
                                         Map<ItemId, Double> leafValues) {
        Double leaf = leafValues.get(item);
        if (leaf != null) {
            return leaf;
        }

        Double market = marketPrices.get(item);
        if (market != null) {
            return market;
        }

        return referenceCost.getOrDefault(item, Double.POSITIVE_INFINITY);
    }

}
