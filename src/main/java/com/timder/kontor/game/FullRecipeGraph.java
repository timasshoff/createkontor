package com.timder.kontor.game;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.port.RecipeGraph;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.RecipeInput;
import com.timder.kontor.core.value.RecipeNode;
import com.timder.kontor.core.value.RecipeOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class FullRecipeGraph implements RecipeGraph {

    /**
     * A map that stores all recipes.
     * Maps a ressource location (id) to all recipes that output this item.
     */
    private final Map<String, List<RecipeNode>> graph = new HashMap<>();

    public static FullRecipeGraph createRecipeGraph(MinecraftServer server) {
        return new  FullRecipeGraph(server.getRecipeManager().getRecipes());
    }

    public FullRecipeGraph(Collection<RecipeHolder<?>> recipeHolders) {
        var registryAccess = ServerLifecycleHooks.getCurrentServer().registryAccess();

        for (RecipeHolder<?> recipeHolder : recipeHolders) {
            Recipe<?> recipe = recipeHolder.value();
            String context = recipeHolder.id().toString();

            List<RecipeOutput> outputs = outputsOf(recipe, registryAccess);
            if (outputs.isEmpty()) {
                CreateKontor.LOGGER.warn("Recipe {} has no usable output, skipped.", context);
                continue;
            }

            RecipeNode node = new RecipeNode(outputs, inputsOf(recipe, context), processIdOf(recipe));

            for (RecipeOutput output : outputs) {
                graph.computeIfAbsent(output.item().value(), key -> new ArrayList<>()).add(node);
            }

            CreateKontor.LOGGER.info("Recipe loaded: {}", recipeHolder.id());
        }

        CreateKontor.LOGGER.info("Loaded a total of {} recipes.", graph.size());
    }

    @Override
    public List<RecipeNode> recipesFor(ItemId output) {
        return graph.getOrDefault(output.value(), List.of());
    }

    private static String processIdOf(Recipe<?> recipe) {
        return BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString();
    }

    private static List<RecipeOutput> outputsOf(Recipe<?> recipe, RegistryAccess registryAccess) {
        if (recipe instanceof ProcessingRecipe<?, ?> processingRecipe) {
            return independentOutputs(processingRecipe.getRollableResults());
        }

        if (recipe instanceof SequencedAssemblyRecipe sequencedRecipe) {
            return exclusiveOutputs(sequencedRecipe.resultPool);
        }

        ItemStack result = recipe.getResultItem(registryAccess);
        if (result.isEmpty()) {
            return List.of();
        }

        return List.of(new RecipeOutput(itemIdOf(result.getItem()), result.getCount()));
    }

    private static List<RecipeOutput> independentOutputs(List<ProcessingOutput> rollable) {
        List<RecipeOutput> outputs = new ArrayList<>();

        for (ProcessingOutput entry : rollable) {
            ItemStack stack = entry.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            double expectedYield = stack.getCount() * Math.min(entry.getChance(), 1.0f);
            if (expectedYield > 0) {
                outputs.add(new RecipeOutput(itemIdOf(stack.getItem()), expectedYield));
            }
        }

        return outputs;
    }

    private static List<RecipeOutput> exclusiveOutputs(List<ProcessingOutput> pool) {
        float totalWeight = 0f;
        for (ProcessingOutput entry : pool) {
            totalWeight += entry.getChance();
        }

        List<RecipeOutput> outputs = new ArrayList<>();
        if (totalWeight <= 0) {
            return outputs;
        }

        for (ProcessingOutput entry : pool) {
            ItemStack stack = entry.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            double expectedYield = stack.getCount() * (entry.getChance() / totalWeight);
            if (expectedYield > 0) {
                outputs.add(new RecipeOutput(itemIdOf(stack.getItem()), expectedYield));
            }
        }

        return outputs;
    }

    private static List<RecipeInput> inputsOf(Recipe<?> recipe, String context) {
        if (recipe instanceof SequencedAssemblyRecipe sequencedRecipe) {
            return sequencedAssemblyInputs(sequencedRecipe, context);
        }

        List<com.timder.kontor.core.value.RecipeInput> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            convertIngredient(ingredient, 1.0, context).ifPresent(inputs::add);
        }
        return inputs;
    }

    private static List<RecipeInput> sequencedAssemblyInputs(SequencedAssemblyRecipe sequencedRecipe, String context) {
        List<com.timder.kontor.core.value.RecipeInput> inputs = new ArrayList<>();

        convertIngredient(sequencedRecipe.getIngredient(), 1.0, context).ifPresent(inputs::add);

        double loops = sequencedRecipe.getLoops();

        for (SequencedRecipe<?> step : sequencedRecipe.getSequence()) {
            List<Ingredient> stepIngredients = new ArrayList<>();
            step.getAsAssemblyRecipe().addAssemblyIngredients(stepIngredients);

            for (Ingredient ingredient : stepIngredients) {
                convertIngredient(ingredient, loops, context).ifPresent(inputs::add);
            }
        }

        return inputs;
    }

    private static Optional<RecipeInput> convertIngredient(Ingredient ingredient, double quantity, String context) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            CreateKontor.LOGGER.warn("Found ingredient with no items in {}.", context);
            return Optional.empty();
        }

        List<ItemId> alternatives = new ArrayList<>();
        Set<Item> seen = new HashSet<>();
        for (ItemStack stack : items) {
            if (seen.add(stack.getItem())) {
                alternatives.add(itemIdOf(stack.getItem()));
            }
        }

        return Optional.of(new com.timder.kontor.core.value.RecipeInput(alternatives, quantity));
    }

    private static ItemId itemIdOf(Item item) {
        return new ItemId(BuiltInRegistries.ITEM.getKey(item).toString());
    }

}
