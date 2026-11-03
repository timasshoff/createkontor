package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.timder.kontor.game.item.NotebookItem;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class KontorItems {

    public static final ItemEntry<NotebookItem> NOTEBOOK = REGISTRATE
            .item("notebook", NotebookItem::new)
            .properties(properties -> properties.stacksTo(1))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("NL")
                    .pattern("NP")
                    .define('N', Items.IRON_NUGGET)
                    .define('L', Items.LEATHER)
                    .define('P', Items.PAPER)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Notebook")
            .onRegisterAfter(Registries.ITEM, KontorItems::tooltip)
            .register();

    public static void tooltip(Item item) {
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }

    static void touch() {}

    private KontorItems() {}
}
