package com.timder.kontor.game;

import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.economy.EconomyParams;
import com.timder.kontor.core.macro.MacroState;
import com.timder.kontor.core.market.*;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
import com.timder.kontor.core.raw.RawMaterialState;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.ProcessCosts;
import com.timder.kontor.data.KontorData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EconomySavedData extends SavedData {

    private static final String NAME = "kontor_economy";
    private static final long WARMUP_TICKS = 60L * Economy.DAY_LENGTH; // = 60 days of economy warmup

    private final Economy economy;

    private EconomySavedData(Economy economy) {
        this.economy = economy;
    }

    public Economy getEconomy() {
        return economy;
    }

    public static EconomySavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        SavedData.Factory<EconomySavedData> factory = new SavedData.Factory<>(
                () -> create(server), // No file found -> create new economy
                (tag, registries) -> load(tag, server) // File found -> load existing economy
        );
        return overworld.getDataStorage().computeIfAbsent(factory, NAME);
    }

    /**
     * Creates a fresh economy. Use when no economy exists yet.
     */
    private static EconomySavedData create(MinecraftServer server) {
        Economy economy = buildFreshEconomy(server);
        economy.advanceTicksQuietly(WARMUP_TICKS);
        return new EconomySavedData(economy);
    }

    private static EconomySavedData load(CompoundTag tag, MinecraftServer server) {
        Economy.SaveState saveState = readSaveState(tag);
        Rng rng = SeededRng.forName(server.overworld().getSeed(), "economy");
        Economy economy = Economy.restore(
                KontorData.getRawMaterials(),
                KontorData.getMarketDefinitions(),
                Map.of(),
                new EconomyParams(
                        EconomyConfig.toMacroParams(),
                        EconomyConfig.toProgressParams(),
                        EconomyConfig.toPriceProcessParams(),
                        new ProcessCosts(KontorData.getProcessCosts(), EconomyConfig.DEFAULT_PROCESS_COST.get())
                ),
                FullRecipeGraph.createRecipeGraph(server),
                rng,
                saveState);
        return new EconomySavedData(economy);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        writeSaveState(compoundTag, economy.getSaveState());
        return compoundTag;
    }

    private static Economy buildFreshEconomy(MinecraftServer server) {
        Rng rng = SeededRng.forName(server.overworld().getSeed(), "economy");
        return new Economy(
                KontorData.getRawMaterials(),
                KontorData.getMarketDefinitions(),
                Map.of(),
                new EconomyParams(
                        EconomyConfig.toMacroParams(),
                        EconomyConfig.toProgressParams(),
                        EconomyConfig.toPriceProcessParams(),
                        new ProcessCosts(KontorData.getProcessCosts(), EconomyConfig.DEFAULT_PROCESS_COST.get())
                ),
                FullRecipeGraph.createRecipeGraph(server),
                rng);
    }

    /*
    NBT <-> Economy
     */

    private static void writeSaveState(CompoundTag tag, Economy.SaveState saveState) {
        tag.putLong("TicksElapsed", saveState.ticksElapsed());
        tag.put("Macro", writeMacro(saveState.macro()));

        ListTag rawMaterials = new ListTag();
        for (Map.Entry<ItemId, RawMaterialState.SaveState> entry : saveState.rawMaterialStates().entrySet()) {
            ItemId id = entry.getKey();
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Id", id.value());
            entryTag.putDouble("Price", entry.getValue().price());
            entryTag.putDouble("PurchasedToday", entry.getValue().purchasedToday());
            entryTag.put("History", writeRawMaterialHistory(saveState.rawMaterialHistory().getOrDefault(id, List.of())));
            rawMaterials.add(entryTag);
        }
        tag.put("RawMaterials", rawMaterials);

        ListTag markets = new ListTag();
        for (Map.Entry<ItemId, MarketState.SaveState> entry : saveState.marketStates().entrySet()) {
            ItemId id = entry.getKey();
            MarketState.SaveState s = entry.getValue();
            MarketParams params = saveState.marketParams().get(id);

            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Id", id.value());
            entryTag.putDouble("PriceLevel", s.priceLevel());
            entryTag.putDouble("Companies", s.companies());
            entryTag.putDouble("CompetitorReputation", s.competitorReputation());
            entryTag.putDouble("Deviation", s.deviation());
            entryTag.putDouble("DeliveredToday", s.deliveredToday());
            entryTag.putDouble("DeliveredThisTick", s.deliveredThisTick());
            entryTag.putDouble("ReferenceCost", params.referenceCost());
            entryTag.putDouble("PlantSize", params.plantSize());
            entryTag.putDouble("Demand", saveState.currentDemand().getOrDefault(id, 0.0));
            entryTag.putDouble("LastTickDelivered", saveState.lastTickDelivered().getOrDefault(id, 0.0));
            entryTag.put("History", writeMarketHistory(saveState.marketHistory().getOrDefault(id, List.of())));
            markets.add(entryTag);
        }
        tag.put("Markets", markets);
    }

    private static Economy.SaveState readSaveState(CompoundTag tag) {
        MacroState.SaveState macro = readMacro(tag.getCompound("Macro"));

        Map<ItemId, MarketDefinition> definitionsById = new LinkedHashMap<>();
        for (MarketDefinition def : KontorData.getMarketDefinitions()) {
            definitionsById.put(def.id(), def);
        }

        Map<ItemId, RawMaterialState.SaveState> rawMaterialStates = new LinkedHashMap<>();
        Map<ItemId, List<RawMaterialHistoryEntry>> rawMaterialHistory = new LinkedHashMap<>();
        for (Tag t : tag.getList("RawMaterials", Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) t;
            ItemId id = new ItemId(entryTag.getString("Id"));
            rawMaterialStates.put(id, new RawMaterialState.SaveState(
                    entryTag.getDouble("Price"), entryTag.getDouble("PurchasedToday")));
            rawMaterialHistory.put(id, readRawMaterialHistory(id, entryTag.getList("History", Tag.TAG_COMPOUND)));
        }

        Map<ItemId, MarketState.SaveState> marketStates = new LinkedHashMap<>();
        Map<ItemId, MarketParams> marketParams = new LinkedHashMap<>();
        Map<ItemId, Double> currentDemand = new LinkedHashMap<>();
        Map<ItemId, Double> lastTickDelivered = new LinkedHashMap<>();
        Map<ItemId, List<MarketHistoryEntry>> marketHistory = new LinkedHashMap<>();

        for (Tag t : tag.getList("Markets", Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) t;
            ItemId id = new ItemId(entryTag.getString("Id"));

            marketStates.put(id, new MarketState.SaveState(
                    entryTag.getDouble("PriceLevel"),
                    entryTag.getDouble("Companies"),
                    entryTag.getDouble("CompetitorReputation"),
                    entryTag.getDouble("Deviation"),
                    entryTag.getDouble("DeliveredToday"),
                    entryTag.getDouble("DeliveredThisTick")));

            MarketDefinition def = definitionsById.get(id);
            marketParams.put(id, new MarketParams(
                    entryTag.getDouble("ReferenceCost"),
                    entryTag.getDouble("PlantSize"),
                    def != null ? def.params().targetUtilisation() : 0.80,
                    def != null ? def.params().group() : GroupDef.metal()));

            currentDemand.put(id, entryTag.getDouble("Demand"));
            lastTickDelivered.put(id, entryTag.getDouble("LastTickDelivered"));
            marketHistory.put(id, readMarketHistory(id, entryTag.getList("History", Tag.TAG_COMPOUND)));
        }

        return new Economy.SaveState(tag.getLong("TicksElapsed"), macro, rawMaterialStates, marketStates,
                marketParams, currentDemand, Map.of(), lastTickDelivered, marketHistory, rawMaterialHistory);
    }

    private static CompoundTag writeMacro(MacroState.SaveState saveState) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Day", saveState.day());
        tag.putDouble("Index", saveState.index());
        tag.putDouble("PreviousIndex", saveState.previousIndex());
        tag.putLong("CycleStart", saveState.cycleStart());
        tag.putInt("CycleLength", saveState.cycleLength());
        tag.putDouble("CycleAmplitude", saveState.cycleAmplitude());
        tag.putDouble("Noise", saveState.noise());
        return tag;
    }

    private static MacroState.SaveState readMacro(CompoundTag tag) {
        return new MacroState.SaveState(
                tag.getLong("Day"), tag.getDouble("Index"), tag.getDouble("PreviousIndex"),
                tag.getLong("CycleStart"), tag.getInt("CycleLength"),
                tag.getDouble("CycleAmplitude"), tag.getDouble("Noise"));
    }

    private static List<RawMaterialHistoryEntry> readRawMaterialHistory(ItemId id, ListTag historyTag) {
        List<RawMaterialHistoryEntry> history = new ArrayList<>();
        for (Tag t : historyTag) {
            CompoundTag entryTag = (CompoundTag) t;
            history.add(new RawMaterialHistoryEntry(id, entryTag.getLong("Day"), entryTag.getDouble("Price")));
        }
        return history;
    }

    private static ListTag writeRawMaterialHistory(List<RawMaterialHistoryEntry> history) {
        ListTag historyTag = new ListTag();
        for (RawMaterialHistoryEntry entry : history) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Day", entry.day());
            entryTag.putDouble("Price", entry.price());
            historyTag.add(entryTag);
        }
        return historyTag;
    }

    private static List<MarketHistoryEntry> readMarketHistory(ItemId id, ListTag historyTag) {
        List<MarketHistoryEntry> history = new ArrayList<>();
        for (Tag t : historyTag) {
            CompoundTag entryTag = (CompoundTag) t;
            history.add(new MarketHistoryEntry(id,
                    entryTag.getLong("Tick"), entryTag.getLong("Day"),
                    entryTag.getDouble("PriceLevel"), entryTag.getDouble("Deviation"),
                    entryTag.getDouble("DisplayedPrice"), entryTag.getDouble("Companies"),
                    entryTag.getDouble("DeliveredThisTick")));
        }
        return history;
    }

    private static ListTag writeMarketHistory(List<MarketHistoryEntry> history) {
        ListTag historyTag = new ListTag();
        for (MarketHistoryEntry entry : history) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Tick", entry.tick());
            entryTag.putLong("Day", entry.day());
            entryTag.putDouble("PriceLevel", entry.priceLevel());
            entryTag.putDouble("Deviation", entry.deviation());
            entryTag.putDouble("DisplayedPrice", entry.displayedPrice());
            entryTag.putDouble("Companies", entry.companies());
            entryTag.putDouble("DeliveredThisTick", entry.deliveredThisTick());
            historyTag.add(entryTag);
        }
        return historyTag;
    }
}
