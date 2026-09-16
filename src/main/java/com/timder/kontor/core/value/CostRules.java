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

}
