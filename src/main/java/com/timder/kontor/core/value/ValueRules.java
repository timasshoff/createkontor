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
                    for (RecipeInput recipeInput : recipeNode.inputs()) {
                        for (ItemId alternative : recipeInput.alternatives()) {
                            if (scope.add(alternative)) {
                                next.add(alternative);
                            }
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
     * @param scope A reference cost will be computed for these alternatives
     * @param graph The recipe graph to search
     * @param processCosts The cost per process
     * @param leafValues Raw materials and any other fixed values. Items in this map will never have its recipes evaluated.
     * @param marketPrices Current market price of every item that has a market. Reference cost may differ from market price.
     * @param technicalProgress The technical progress
     * @return Reference costs, the recipe each reference cost came from, depths and not-valued alternatives
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
                    double ingredientCost = 0.0;
                    int deepestIngredient = -1;
                    boolean usable = true;

                    for (RecipeInput recipeInput : recipe.inputs()) {
                        Cheapest cheapest = cheapestAlternative(recipeInput.alternatives(), referenceCost, marketPrices, leafValues);

                        if (Double.isInfinite(cheapest.value())) {
                            usable = false;
                            break;
                        }

                        ingredientCost += recipeInput.quantity() * cheapest.value();
                        deepestIngredient = Math.max(deepestIngredient, depth.getOrDefault(cheapest.item(), -1));
                    }

                    if (!usable) {
                        continue;
                    }

                    // Gemeinsame Kosten des Laufs auf alle Outputs verteilen, gewichtet nach
                    // (erwartete Menge * bekannter Wert) - siehe CostRules.jointRecipeCost.
                    double targetValue = -1;
                    double totalWeightedOutputValue = 0.0;

                    for (RecipeOutput recipeOutput : recipe.outputs()) {
                        double value = ingredientValue(recipeOutput.item(), referenceCost, marketPrices, leafValues);

                        if (Double.isInfinite(value)) {
                            usable = false;
                            break;
                        }

                        totalWeightedOutputValue += recipeOutput.yield() * value;
                        if (recipeOutput.item().equals(item)) {
                            targetValue = value;
                        }
                    }

                    if (!usable || targetValue < 0 || totalWeightedOutputValue <= 0) {
                        continue;
                    }

                    double cost = CostRules.jointRecipeCost(ingredientCost, processCosts.of(recipe.process()), technicalProgress,
                            targetValue, totalWeightedOutputValue);

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
     * The value to use for a single item when it appears as an ingredient. Either its
     * external leaf value, otherwise its current market price. If there is no market price,
     * the just-computed reference cost will be used. Positive Infinity if none of these are known.
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

    /**
     * The value to use for a set of alternatives when they appear as an ingredient
     */
    public static double ingredientValue(List<ItemId> alternatives, Map<ItemId, Double> referenceCost, Map<ItemId, Double> marketPrices,
                                         Map<ItemId, Double> leafValues) {
        return cheapestAlternative(alternatives, referenceCost, marketPrices, leafValues).value();
    }

    /**
     * Which alternative of a {@link RecipeInput} would actually be used, and at what value
     */
    private static Cheapest cheapestAlternative(List<ItemId> alternatives, Map<ItemId, Double> referenceCost,
                                                Map<ItemId, Double> marketPrices, Map<ItemId, Double> leafValues) {
        ItemId cheapestItem = alternatives.get(0);
        double cheapestValue = ingredientValue(cheapestItem, referenceCost, marketPrices, leafValues);

        for (int i = 1; i < alternatives.size(); i++) {
            ItemId candidate = alternatives.get(i);
            double candidateValue = ingredientValue(candidate, referenceCost, marketPrices, leafValues);
            if (candidateValue < cheapestValue) {
                cheapestValue = candidateValue;
                cheapestItem = candidate;
            }
        }

        return new Cheapest(cheapestItem, cheapestValue);
    }

    private record Cheapest(ItemId item, double value) {}
}
