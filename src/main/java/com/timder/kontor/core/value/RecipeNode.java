package com.timder.kontor.core.value;

import java.util.List;

/**
 * A recipe flattened to the output, the amount of output, the input and which process it uses.
 * Does not include byproducts yet.
 * @param outputs What this recipe produces, and how much of each per run
 * @param inputs Which inputs this recipe requires
 * @param process Identifier for the processing step this recipe uses
 */
public record RecipeNode(
        List<RecipeOutput> outputs,
        List<RecipeInput> inputs,
        String process
) {

    public RecipeNode {
        if (outputs == null) throw new IllegalArgumentException("outputs must not be null.");
        if (outputs.isEmpty()) throw new IllegalArgumentException("outputs must not be empty.");
        if (inputs == null) throw new IllegalArgumentException("inputs must not be null.");
        if (process == null || process.isBlank()) throw new IllegalArgumentException("process must not be blank");

        inputs = List.copyOf(inputs);
    }

    public RecipeNode(ItemId output, double yield, List<RecipeInput> inputs, String process) {
        this(List.of(new RecipeOutput(output, yield)), inputs, process);
    }

    @Override
    public String toString() {
        return "RecipeNode{" +
                "outputs=" + outputs +
                ", inputs=" + inputs +
                ", process='" + process + '\'' +
                '}';
    }
}
