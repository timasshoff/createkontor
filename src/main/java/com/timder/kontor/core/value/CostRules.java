package com.timder.kontor.core.value;

public final class CostRules {

    /**
     * The cost of producing one unit of output through one processing step.
     * Respects technical progress.
     * @param totalIngredientCost Sum of every ingredients cost * quantity
     * @param processingCost Cost of the processing step itself
     * @param technicalProgress The technical progress of the economy
     * @param yield How many unity this recipe will output
     * @return The cost per unit of output
     */
    public static double recipeCost(double totalIngredientCost, double processingCost, double technicalProgress, double yield) {
        if (yield <= 0) throw new IllegalArgumentException("yield must be positive");

        return (totalIngredientCost + processingCost * technicalProgress) / yield;
    }

    /**
     * The cost of producing one unit of output through a single processing step with a
     * single ingredient. A convenience for the common case, see {@link #recipeCost}.
     *
     * @param ingredientValue Price of one unit of the ingredient
     * @param ingredientQuantity How many units of the ingredient one run of the process consumes
     * @param processingCost Cost of the processing step itself
     * @param technicalProgress The technical progress of the economy
     * @param yield How many unity this recipe will output
     * @return The cost per unit of output
     */
    public static double stepCost(double ingredientValue, double ingredientQuantity, double processingCost, double technicalProgress, double yield) {
        return recipeCost(ingredientQuantity * ingredientValue, processingCost, technicalProgress, yield);
    }

    /**
     * The cost of one unit of a SPECIFIC output from a run that produces several outputs
     * together.
     *
     * @param totalIngredientCost Sum of every ingredient's cost * quantity for one run
     * @param processingCost Cost of the processing step itself, for one run
     * @param technicalProgress The technical progress of the economy
     * @param targetValue The best known value of the output being priced
     * @param totalWeightedOutputValue Sum, over every output of this run, of (that output's expected yield * its own best known value)
     * @return The cost per unit of the target output
     */
    public static double jointRecipeCost(double totalIngredientCost, double processingCost, double technicalProgress, double targetValue, double totalWeightedOutputValue) {
        if (totalWeightedOutputValue <= 0) throw new IllegalArgumentException("totalWeightedOutputValue must be positive");

        double jointCost = totalIngredientCost + processingCost * technicalProgress;
        return jointCost * targetValue / totalWeightedOutputValue;
    }

}
