package com.timder.kontor.data.provider;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RawMaterialDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public RawMaterialDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/raw_materials");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        /*
        Seeds & Food-related
         */
        futures.add(save(cache, "wheat", "minecraft:wheat", 1.0, 0.02, 6000.0));
        futures.add(save(cache, "cocoa_beans", "minecraft:cocoa_beans", 3.5, 0.037, 3500.0));
        futures.add(save(cache, "kelp", "minecraft:kelp", 0.3, 0.02, 8000.0));
        futures.add(save(cache, "sugar_cane", "minecraft:sugar_cane", 0.5, 0.02, 8000.0));
        futures.add(save(cache, "melon_slice", "minecraft:melon_slice", 0.4, 0.02, 8000.0));
        futures.add(save(cache, "potato", "minecraft:potato", 0.8, 0.02, 6000.0));
        futures.add(save(cache, "carrot", "minecraft:carrot", 0.8, 0.02, 6000.0));
        futures.add(save(cache, "beetroot", "minecraft:beetroot", 0.8, 0.02, 6000.0));
        futures.add(save(cache, "sweet_berries", "minecraft:sweet_berries", 0.8, 0.02, 6000.0));
        futures.add(save(cache, "apple", "minecraft:apple", 1.0, 0.02, 5000.0));
        futures.add(save(cache, "beef", "minecraft:beef", 2.25, 0.02, 5000.0));
        futures.add(save(cache, "porkchop", "minecraft:porkchop", 2.0, 0.02, 5000.0));
        futures.add(save(cache, "chicken", "minecraft:chicken", 2.0, 0.02, 5000.0));
        futures.add(save(cache, "mutton", "minecraft:mutton", 2.0, 0.02, 5000.0));
        futures.add(save(cache, "rabbit", "minecraft:rabbit", 2.5, 0.02, 3500.0));
        futures.add(save(cache, "cod", "minecraft:cod", 1.75, 0.02, 5000.0));
        futures.add(save(cache, "salmon", "minecraft:salmon", 1.75, 0.02, 5000.0));

        /*
        Gems & Ores
         */
        futures.add(save(cache, "coal", "minecraft:coal", 2.0, 0.02, 7000.0));
        futures.add(save(cache, "raw_copper", "minecraft:raw_copper", 4.5, 0.025, 3500.0));
        futures.add(save(cache, "raw_iron", "minecraft:raw_iron", 6.2, 0.025, 3000.0));
        futures.add(save(cache, "raw_zinc", "create:raw_zinc", 5.5, 0.03, 2800.0));
        futures.add(save(cache, "redstone", "minecraft:redstone", 3.0, 0.03, 4000.0));
        futures.add(save(cache, "amethyst_shard", "minecraft:amethyst_shard", 3.5, 0.05, 2000.0));
        futures.add(save(cache, "quartz", "minecraft:quartz", 3.5, 0.03, 3500.0));
        futures.add(save(cache, "raw_gold", "minecraft:raw_gold", 9.5, 0.035, 1800));
        futures.add(save(cache, "lapis_lazuli", "minecraft:lapis_lazuli", 7.0, 0.035, 2200));
        futures.add(save(cache, "emerald", "minecraft:emerald", 100.0, 0.045, 400));
        futures.add(save(cache, "diamond", "minecraft:diamond", 120.0, 0.045, 200));
        futures.add(save(cache, "ancient_debris", "minecraft:ancient_debris", 180.0, 0.06, 100));

        /*
        Wood
         */
        futures.add(save(cache, "oak_log", "minecraft:oak_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_oak_log", "minecraft:stripped_oak_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "spruce_log", "minecraft:spruce_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_spruce_log", "minecraft:stripped_spruce_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "birch_log", "minecraft:birch_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_birch_log", "minecraft:stripped_birch_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "jungle_log", "minecraft:jungle_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_jungle_log", "minecraft:stripped_jungle_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "acacia_log", "minecraft:acacia_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_acacia_log", "minecraft:stripped_acacia_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "dark_oak_log", "minecraft:dark_oak_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_dark_oak_log", "minecraft:stripped_dark_oak_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "mangrove_log", "minecraft:mangrove_log", 2.2, 0.02, 4000.0));
        futures.add(save(cache, "stripped_mangrove_log", "minecraft:stripped_mangrove_log", 2.5, 0.02, 3500.0));
        futures.add(save(cache, "cherry_log", "minecraft:cherry_log", 1.7, 0.01, 7000.0));
        futures.add(save(cache, "stripped_cherry_log", "minecraft:stripped_cherry_log", 2.0, 0.01, 6000.0));
        futures.add(save(cache, "crimson_stem", "minecraft:crimson_stem", 3.7, 0.035, 3000.0));
        futures.add(save(cache, "stripped_crimson_stem", "minecraft:stripped_crimson_stem", 4.0, 0.035, 2500.0));
        futures.add(save(cache, "warped_stem", "minecraft:warped_stem", 3.7, 0.035, 3000.0));
        futures.add(save(cache, "stripped_warped_stem", "minecraft:stripped_warped_stem", 4.0, 0.035, 2500.0));

        /*
        Stone
         */

        futures.add(save(cache, "cobblestone", "minecraft:cobblestone", 1.25, 0.01, 9000.0));
        futures.add(save(cache, "cobbled_deepslate", "minecraft:cobbled_deepslate", 1.4, 0.01, 8000.0));
        futures.add(save(cache, "calcite", "minecraft:calcite", 2.0, 0.015, 4000.0));
        futures.add(save(cache, "dripstone_block", "minecraft:dripstone_block", 2.0, 0.015, 4000.0));
        futures.add(save(cache, "andesite", "minecraft:andesite", 1.75, 0.02, 5000.0));
        futures.add(save(cache, "tuff", "minecraft:tuff", 1.75, 0.02, 5000.0));
        futures.add(save(cache, "obsidian", "minecraft:obsidian", 15.0, 0.035, 1500.0));
        futures.add(save(cache, "limestone", "create:limestone", 2.4, 0.02, 3500.0));
        futures.add(save(cache, "scoria", "create:scoria", 2.5, 0.02, 3200.0));
        futures.add(save(cache, "scorchia", "create:scorchia", 2.5, 0.02, 3200.0));
        futures.add(save(cache, "asurine", "create:asurine", 2.6, 0.02, 3200.0));
        futures.add(save(cache, "crimsite", "create:crimsite", 2.6, 0.02, 3200.0));
        futures.add(save(cache, "ochrum", "create:ochrum", 2.6, 0.02, 3200.0));
        futures.add(save(cache, "veridium", "create:veridium", 2.6, 0.02, 3200.0));
        futures.add(save(cache, "netherrack", "minecraft:netherrack", 0.8, 0.01, 9000.0));
        futures.add(save(cache, "basalt", "minecraft:basalt", 1.5, 0.01, 6000.0));
        futures.add(save(cache, "end_stone", "minecraft:end_stone", 3.0, 0.02, 4000.0));

        /*
        Misc
         */

        futures.add(save(cache, "dirt", "minecraft:dirt", 1.0, 0.01, 9000.0));
        futures.add(save(cache, "sand", "minecraft:sand", 1.0, 0.01, 9000.0));
        futures.add(save(cache, "soul_sand", "minecraft:soul_sand", 1.5, 0.015, 6000.0));
        futures.add(save(cache, "gravel", "minecraft:gravel", 1.1, 0.01, 9000.0));
        futures.add(save(cache, "clay_ball", "minecraft:clay_ball", 0.9, 0.015, 6000.0));
        futures.add(save(cache, "flint", "minecraft:flint", 1.3, 0.02, 5000.0));
        futures.add(save(cache, "moss_block", "minecraft:moss_block", 1.5, 0.02, 4000.0));
        futures.add(save(cache, "vine", "minecraft:vine", 0.5, 0.02, 6000.0));
        futures.add(save(cache, "feather", "minecraft:feather", 1.5, 0.02, 4000.0));
        futures.add(save(cache, "egg", "minecraft:egg", 1.5, 0.02, 4000.0));
        futures.add(save(cache, "ink_sac", "minecraft:ink_sac", 10.0, 0.04, 1500.0));
        futures.add(save(cache, "cactus", "minecraft:cactus", 5.0, 0.04, 4000.0));
        futures.add(save(cache, "bone", "minecraft:bone", 5.0, 0.025, 1500.0));
        futures.add(save(cache, "leather", "minecraft:leather", 5.0, 0.025, 3000.0));
        futures.add(save(cache, "string", "minecraft:string", 3.0, 0.025, 3000.0));
        futures.add(save(cache, "gunpowder", "minecraft:gunpowder", 5.0, 0.03, 2000.0));
        futures.add(save(cache, "slime_ball", "minecraft:slime_ball", 6.0, 0.03, 1400.0));
        futures.add(save(cache, "magma_cream", "minecraft:magma_cream", 8.0, 0.035, 1000.0));
        futures.add(save(cache, "blaze_rod", "minecraft:blaze_rod", 25.0, 0.045, 600.0));
        futures.add(save(cache, "ender_pearl", "minecraft:ender_pearl", 30.0, 0.045, 800.0));
        futures.add(save(cache, "shulker_shell", "minecraft:shulker_shell", 55.0, 0.05, 200.0));
        futures.add(save(cache, "honeycomb", "minecraft:honeycomb", 4.0, 0.02, 2500.0));
        futures.add(save(cache, "glowstone_dust", "minecraft:glowstone_dust", 3.5, 0.03, 3000.0));
        futures.add(save(cache, "prismarine_shard", "minecraft:prismarine_shard", 5.0, 0.03, 2000.0));
        futures.add(save(cache, "prismarine_crystals", "minecraft:prismarine_crystals", 8.0, 0.035, 1500.0));

        /*
        Flowers & Similar
         */

        futures.add(save(cache, "dandelion", "minecraft:dandelion", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "poppy", "minecraft:poppy", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "blue_orchid", "minecraft:blue_orchid", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "allium", "minecraft:allium", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "azure_bluet", "minecraft:azure_bluet", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "red_tulip", "minecraft:red_tulip", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "orange_tulip", "minecraft:orange_tulip", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "white_tulip", "minecraft:white_tulip", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "oxeye_daisy", "minecraft:oxeye_daisy", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "cornflower", "minecraft:cornflower", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "lily_of_the_valley", "minecraft:lily_of_the_valley", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "sunflower", "minecraft:sunflower", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "lilac", "minecraft:lilac", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "rose_bush", "minecraft:rose_bush", 7.5, 0.037, 3500.0));
        futures.add(save(cache, "wither_rose", "minecraft:wither_rose", 18.0, 0.04, 1500.0));
        futures.add(save(cache, "azalea", "minecraft:azalea", 3.0, 0.03, 3500.0));
        futures.add(save(cache, "nether_wart", "minecraft:nether_wart", 3.0, 0.03, 3500.0));
        futures.add(save(cache, "chorus_fruit", "minecraft:chorus_fruit", 4.0, 0.035, 2000.0));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(
            CachedOutput cache,
            String fileName,
            String item,
            double baseValue,
            double dailyVolatility,
            double referenceVolume) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("base_value", baseValue);
        json.addProperty("daily_volatility", dailyVolatility);
        json.addProperty("reference_volume", referenceVolume);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Raw Materials";
    }
}
