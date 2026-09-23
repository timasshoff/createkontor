package com.timder.kontor.core.economy;

import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.request.ReputationParams;
import com.timder.kontor.core.company.request.ReputationRules;
import com.timder.kontor.core.economy.event.CompetitorEnteredEvent;
import com.timder.kontor.core.economy.event.CompetitorExitedEvent;
import com.timder.kontor.core.economy.event.EconomyEvent;
import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.core.macro.MacroRules;
import com.timder.kontor.core.macro.MacroState;
import com.timder.kontor.core.macro.Phase;
import com.timder.kontor.core.market.*;
import com.timder.kontor.core.port.RecipeGraph;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.raw.*;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.ValueResult;
import com.timder.kontor.core.value.ValueRules;

import java.util.*;

public final class Economy {

    /**
     * The length of one trading tick
     */
    public static final long TRADING_TICK_LENGTH = 2000;

    /**
     * The length of one trading day
     */
    public static final long DAY_LENGTH = TRADING_TICK_LENGTH * MarketRules.TRADING_TICKS_PER_DAY;

    /** How many days of history to keep per market/raw material. Oldest entries drop first. */
    public static final int HISTORY_LENGTH_DAYS = 360;
    public static final int HISTORY_LENGTH_TICKS = HISTORY_LENGTH_DAYS * MarketRules.TRADING_TICKS_PER_DAY;

    private final Map<ItemId, Deque<MarketHistoryEntry>> marketHistory = new LinkedHashMap<>();
    private final Map<ItemId, Deque<RawMaterialHistoryEntry>> rawMaterialHistory = new LinkedHashMap<>();
    private final Deque<MacroHistoryEntry> macroHistory;

    private final RecipeGraph graph;
    private final EconomyParams params;
    private final Rng rng;

    private final List<RawMaterialDefinition> rawMaterialDefinitions;
    private final List<MarketDefinition> marketDefinitions;
    private final Map<ItemId, Double> valueOverrides;

    private final ReputationParams reputationParams;
    private final Map<ItemId, Set<CompanyId>> fulfilledToday = new LinkedHashMap<>();

    private final Map<ItemId, RawMaterialState> rawMaterialStates = new LinkedHashMap<>();
    private final Map<ItemId, MarketState> marketStates = new LinkedHashMap<>();
    private final Map<ItemId, MarketParticipants> marketParticipants = new LinkedHashMap<>();
    private final Map<ItemId, MarketParams> marketParamsMap = new LinkedHashMap<>();
    private final Map<ItemId, Double> currentDemand = new LinkedHashMap<>();
    private final Map<ItemId, DayResult> lastDayResults = new LinkedHashMap<>();
    private final Map<ItemId, Integer> manufacturingDepths = new LinkedHashMap<>();

    /**
     * Every item reachable from the configured markets.
     * Only discovered once.
     */
    private final Set<ItemId> discoveredScope;

    private final MacroState macro;
    private long ticksElapsed = 0;

    /**
     * A persistable snapshot of everything the economy needs.
     */
    public record SaveState(
            long ticksElapsed,
            MacroState.SaveState macro,
            Map<ItemId, RawMaterialState.SaveState> rawMaterialStates,
            Map<ItemId, MarketState.SaveState> marketStates,
            Map<ItemId, MarketParticipants.SaveState> marketParticipants,
            Map<ItemId, MarketParams> marketParams,
            Map<ItemId, Integer> manufacturingDepths,
            Map<ItemId, Set<CompanyId>> fulfilledToday,
            Map<ItemId, Double> currentDemand,
            Map<ItemId, DayResult> lastDayResults,
            Map<ItemId, List<MarketHistoryEntry>> marketHistory,
            Map<ItemId, List<RawMaterialHistoryEntry>> rawMaterialHistory,
            List<MacroHistoryEntry> macroHistory
    ) {
        public SaveState {
            rawMaterialStates = Map.copyOf(rawMaterialStates);
            marketStates = Map.copyOf(marketStates);
            marketParticipants = Map.copyOf(marketParticipants);
            manufacturingDepths = Map.copyOf(manufacturingDepths);
            marketParams = Map.copyOf(marketParams);
            fulfilledToday = immutableCopy(fulfilledToday);
            currentDemand = Map.copyOf(currentDemand);
            lastDayResults = Map.copyOf(lastDayResults);
            marketHistory = Map.copyOf(marketHistory);
            rawMaterialHistory = Map.copyOf(rawMaterialHistory);
        }

        private static Map<ItemId, Set<CompanyId>> immutableCopy(Map<ItemId, Set<CompanyId>> source) {
            Map<ItemId, Set<CompanyId>> copy = new LinkedHashMap<>();
            for (Map.Entry<ItemId, Set<CompanyId>> entry : source.entrySet()) {
                copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
            }
            return Map.copyOf(copy);
        }
    }

    /**
     * Captures the current economy as a save state for persistence.
     * @return The save state
     */
    public SaveState getSaveState() {
        Map<ItemId, RawMaterialState.SaveState> rawSaveStates = new LinkedHashMap<>();
        for (Map.Entry<ItemId, RawMaterialState> entry : rawMaterialStates.entrySet()) {
            rawSaveStates.put(entry.getKey(), entry.getValue().getSaveState());
        }

        Map<ItemId, MarketState.SaveState> marketSaveStates = new LinkedHashMap<>();
        for (Map.Entry<ItemId, MarketState> entry : marketStates.entrySet()) {
            marketSaveStates.put(entry.getKey(), entry.getValue().getSaveState());
        }

        Map<ItemId, MarketParticipants.SaveState> marketParticipantsSaveStates = new LinkedHashMap<>();
        for (Map.Entry<ItemId, MarketParticipants> entry : marketParticipants.entrySet()) {
            marketParticipantsSaveStates.put(entry.getKey(), entry.getValue().getSaveState());
        }

        Map<ItemId, List<MarketHistoryEntry>> marketHistorySaveStates = new LinkedHashMap<>();
        for (Map.Entry<ItemId, Deque<MarketHistoryEntry>> entry : marketHistory.entrySet()) {
            marketHistorySaveStates.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        Map<ItemId, List<RawMaterialHistoryEntry>> rawMaterialHistorySaveStates = new LinkedHashMap<>();
        for (Map.Entry<ItemId, Deque<RawMaterialHistoryEntry>> entry : rawMaterialHistory.entrySet()) {
            rawMaterialHistorySaveStates.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        List<MacroHistoryEntry> macroHistorySave = new LinkedList<>(macroHistory);

        return new SaveState(
                ticksElapsed,
                macro.getSaveState(),
                rawSaveStates,
                marketSaveStates,
                marketParticipantsSaveStates,
                marketParamsMap,
                Map.copyOf(manufacturingDepths),
                fulfilledToday,
                currentDemand,
                lastDayResults,
                marketHistorySaveStates,
                rawMaterialHistorySaveStates,
                macroHistorySave);
    }

    private record Attractiveness(Map<CompanyId, Double> byCompany, double total) {}

    /**
     * Restores the economy from a previously saves save-state.
     * Changes in the market or raw material definitions will lead to a fresh start of that definition.
     * @param rawMaterialDefinitions Every raw material that is supposed to be simulated
     * @param marketDefinitions Every market that is supposed to be simulated
     * @param valueOverrides Fixed values that override any (computed) recipe. May be empty.
     * @param params Global settings for the economy
     * @param graph The recipe graph
     * @param rng A source of randomness
     * @return
     */
    public static Economy restore(List<RawMaterialDefinition> rawMaterialDefinitions,
                                  List<MarketDefinition> marketDefinitions,
                                  Map<ItemId, Double> valueOverrides,
                                  EconomyParams params,
                                  ReputationParams reputationParams,
                                  RecipeGraph graph,
                                  Rng rng,
                                  SaveState saveState) {
        return new Economy(rawMaterialDefinitions, marketDefinitions, valueOverrides, params, reputationParams, graph, rng, saveState);
    }

    private Economy(List<RawMaterialDefinition> rawMaterialDefinitions,
                    List<MarketDefinition> marketDefinitions,
                    Map<ItemId, Double> valueOverrides,
                    EconomyParams params,
                    ReputationParams reputationParams,
                    RecipeGraph graph,
                    Rng rng,
                    SaveState saveState) {
        this.rawMaterialDefinitions = List.copyOf(rawMaterialDefinitions);
        this.marketDefinitions = List.copyOf(marketDefinitions);
        this.valueOverrides = Map.copyOf(valueOverrides);
        this.params = Objects.requireNonNull(params, "params must not be null.");
        this.graph = Objects.requireNonNull(graph, "graph must not be null.");
        this.rng = Objects.requireNonNull(rng, "rng must not be null.");
        this.reputationParams = Objects.requireNonNull(reputationParams, "reputationParams must not be null.");

        for (RawMaterialDefinition def : this.rawMaterialDefinitions) {
            RawMaterialState.SaveState saved = saveState.rawMaterialStates().get(def.id());
            rawMaterialStates.put(def.id(), saved != null
                    ? RawMaterialState.restore(saved)
                    : RawMaterialState.fresh(def.params()));
            List<RawMaterialHistoryEntry> savedHistory = saveState.rawMaterialHistory().getOrDefault(def.id(), List.of());
            rawMaterialHistory.put(def.id(), new ArrayDeque<>(savedHistory));
        }

        for (MarketDefinition def : this.marketDefinitions) {
            MarketState.SaveState saved = saveState.marketStates().get(def.id());
            marketStates.put(def.id(), saved != null
                    ? MarketState.restore(saved)
                    : MarketState.fresh(def.params()));

            MarketParticipants.SaveState savedParticipants = saveState.marketParticipants().get(def.id());
            marketParticipants.put(def.id(), savedParticipants != null
                    ? MarketParticipants.restore(savedParticipants)
                    : MarketParticipants.empty());

            marketParamsMap.put(def.id(), saveState.marketParams().getOrDefault(def.id(), def.params()));
            List<MarketHistoryEntry> savedHistory = saveState.marketHistory().getOrDefault(def.id(), List.of());
            marketHistory.put(def.id(), new ArrayDeque<>(savedHistory));
        }

        this.macroHistory = new ArrayDeque<>(saveState.macroHistory);

        currentDemand.putAll(saveState.currentDemand());
        lastDayResults.putAll(saveState.lastDayResults());
        manufacturingDepths.putAll(saveState.manufacturingDepths());

        for (Map.Entry<ItemId, Set<CompanyId>> entry : saveState.fulfilledToday().entrySet()) {
            fulfilledToday.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }

        List<ItemId> roots = this.marketDefinitions.stream().map(MarketDefinition::id).toList();
        this.discoveredScope = ValueRules.discover(roots, graph);

        this.macro = MacroState.restore(saveState.macro());
        this.ticksElapsed = saveState.ticksElapsed();
    }

    /**
     * Builds a fresh economy and opens the first day.
     * @param rawMaterialDefinitions Every raw material that is supposed to be simulated
     * @param marketDefinitions Every market that is supposed to be simulated
     * @param valueOverrides Fixed values that override any (computed) recipe. May be empty.
     * @param params Global settings for the economy
     * @param graph The recipe graph
     * @param rng A source of randomness
     */
    public Economy(List<RawMaterialDefinition> rawMaterialDefinitions,
                   List<MarketDefinition> marketDefinitions,
                   Map<ItemId, Double> valueOverrides,
                   EconomyParams params,
                   ReputationParams reputationParams,
                   RecipeGraph graph,
                   Rng rng) {
        this.rawMaterialDefinitions = List.copyOf(rawMaterialDefinitions);
        this.marketDefinitions = List.copyOf(marketDefinitions);
        this.valueOverrides = Map.copyOf(valueOverrides);
        this.params = Objects.requireNonNull(params, "params must not be null.");
        this.graph = Objects.requireNonNull(graph, "graph must not be null.");
        this.rng = Objects.requireNonNull(rng, "rng must not be null.");
        this.reputationParams = Objects.requireNonNull(reputationParams, "reputationParams must not be null.");

        for (RawMaterialDefinition def : this.rawMaterialDefinitions) {
            rawMaterialStates.put(def.id(), RawMaterialState.fresh(def.params()));
            rawMaterialHistory.put(def.id(), new ArrayDeque<>());
        }

        for (MarketDefinition def : this.marketDefinitions) {
            marketStates.put(def.id(), MarketState.fresh(def.params()));
            marketParticipants.put(def.id(), MarketParticipants.empty());
            marketParamsMap.put(def.id(), def.params());
            marketHistory.put(def.id(), new ArrayDeque<>());
        }

        macroHistory = new ArrayDeque<>();

        List<ItemId> roots = this.marketDefinitions.stream().map(MarketDefinition::id).toList();
        this.discoveredScope = ValueRules.discover(roots, graph);

        this.macro = MacroState.fresh(params.macro(), rng);
        openNewDay();
    }

    /**
     * Advanced as many whole trading ticks as possible until reaching the target tick.
     * @param targetTick The game tick to advance to
     * @return A list of every event that happened along the way. Can be empty.
     */
    public List<EconomyEvent> advanceTo(long targetTick) {
        List<EconomyEvent> events = new ArrayList<>();

        while (ticksElapsed + TRADING_TICK_LENGTH <= targetTick) {
            ticksElapsed += TRADING_TICK_LENGTH;
            runTradingTick();

            if (ticksElapsed % DAY_LENGTH == 0) {
                events.addAll(closeDay());
                openNewDay();
            }
        }

        return events;
    }

    public List<EconomyEvent> advanceTicks(long deltaTicks) {
        return advanceTo(ticksElapsed + deltaTicks);
    }

    /**
     * Forwards the economy by ticksElapsed + delta ticks without
     * changing the ticksElapsed counter.
     * @param deltaTicks The ticks to advance
     * @return The economy events
     */
    public List<EconomyEvent> advanceTicksQuietly(long deltaTicks) {
        long before = ticksElapsed;
        List<EconomyEvent> events = advanceTo(ticksElapsed + deltaTicks);
        ticksElapsed = before;
        return events;
    }

    private void runTradingTick() {
        for (MarketDefinition def : marketDefinitions) {
            MarketState state = marketStates.get(def.id());
            MarketParams params = marketParamsMap.get(def.id());
            double demandPerTick = currentDemand.get(def.id()) / MarketRules.TRADING_TICKS_PER_DAY;

            double actual = state.getDeliveredThisTick();
            double expected = expectedTickDelivery(def.id(), state, params);

            MarketRules.advanceTradingTick(state, actual, expected, demandPerTick);

            recordMarketHistory(def.id(), state, actual);
        }
    }

    /**
     * The expected delivery from registered competitors in this trading tick.
     * @param market The market
     * @param state The current state of the market
     * @param marketParams The parameters of the market
     * @return The expected delivery for this one trading tick
     */
    private double expectedTickDelivery(ItemId market, MarketState state, MarketParams marketParams) {
        Attractiveness attractiveness = attractivenessOf(market, state, marketParams);
        if (attractiveness.byCompany().isEmpty()) {
            return 0.0;
        }

        double demand = currentDemand.get(market);
        double expectedDaily = 0.0;
        for (Map.Entry<CompanyId, Double> entry : attractiveness.byCompany().entrySet()) {
            double own = entry.getValue();
            double other = attractiveness.total() - own;
            double share = MarketRules.shareFrom(own, other, state, marketParams);
            expectedDaily += share * demand;
        }

        return expectedDaily / MarketRules.TRADING_TICKS_PER_DAY;
    }

    public double expectedDailyQuantity(ItemId market, CompanyId company) {
        MarketState state = stateOf(market);
        MarketParams marketParams = marketParamsMap.get(market);
        Attractiveness attractiveness = attractivenessOf(market, state, marketParams);

        Double own = attractiveness.byCompany().get(company);
        if (own == null) {
            throw new IllegalArgumentException(company + " is not a registered participant of market " + market + ".");
        }
        double other = attractiveness.total() - own;
        double share = MarketRules.shareFrom(own, other, state, marketParams);
        return share * currentDemand.get(market);
    }

    private Attractiveness attractivenessOf(ItemId market, MarketState state, MarketParams marketParams) {
        Map<CompanyId, Double> byCompany = new LinkedHashMap<>();
        double total = 0.0;
        for (MarketParticipant participant : participantsOf(market).all()) {
            double a = MarketRules.attractiveness(participant.listPrice(), participant.reputationInStars(), state, marketParams);
            byCompany.put(participant.companyId(), a);
            total += a;
        }
        return new Attractiveness(byCompany, total);
    }

    /**
     * The company's share of yesterdays market overflow.
     * @param market The market
     * @param company The company (must be registered on this market)
     * @return The overflow share
     */
    public double overflowShare(ItemId market, CompanyId company) {
        DayResult lastResult = lastDayResults.get(market);
        if (lastResult == null || lastResult.overflow() <= 0) {
            return 0.0;
        }

        MarketState state = stateOf(market);
        MarketParams marketParams = marketParamsMap.get(market);
        Attractiveness attractiveness = attractivenessOf(market, state, marketParams);

        Double own = attractiveness.byCompany().get(company);
        if (own == null) {
            throw new IllegalArgumentException(company + " is not a registered participant of market " + market + ".");
        }
        if (attractiveness.total() <= 0) {
            return 0.0;
        }
        return lastResult.overflow() * (own / attractiveness.total());
    }

    private List<EconomyEvent> closeDay() {
        List<EconomyEvent> events = new ArrayList<>();
        long day = macro.getDay();

        for (RawMaterialDefinition def : rawMaterialDefinitions) {
            recordRawMaterialHistory(def.id(), day);
        }

        for (MarketDefinition def : marketDefinitions) {
            MarketState state = marketStates.get(def.id());
            MarketParams marketParams = marketParamsMap.get(def.id());

            DayResult result = MarketRules.advanceDay(state, marketParams, currentDemand.get(def.id()));
            lastDayResults.put(def.id(), result);

            if (result.competitorClosed()) {
                events.add(new CompetitorExitedEvent(def.id()));
            } else if (result.competitorOpened()) {
                events.add(new CompetitorEnteredEvent(def.id()));
            }

            applyReputationDrift(def.id());
        }

        recordMacroHistory();

        return events;
    }

    private void openNewDay() {
        MacroRules.advanceDay(macro, params.macro(), rng);

        for (RawMaterialDefinition def : rawMaterialDefinitions) {
            RawMaterialRules.advanceDay(rawMaterialStates.get(def.id()), def.params(), params.priceProcess(), rng);
        }

        double technicalProgress = MacroRules.technicalProgress(macro, params.progress());

        Map<ItemId, Double> leafValues = new HashMap<>();
        for (RawMaterialDefinition def : rawMaterialDefinitions) {
            leafValues.put(def.id(), rawMaterialStates.get(def.id()).getPrice());
        }
        leafValues.putAll(valueOverrides);

        Map<ItemId, Double> marketPrices = new HashMap<>();
        for (MarketDefinition def : marketDefinitions) {
            marketPrices.put(def.id(), marketStates.get(def.id()).getPriceLevel());
        }

        ValueResult result = ValueRules.computeValues(discoveredScope, graph, params.processCosts(), leafValues, marketPrices, technicalProgress);
        double trend = MacroRules.trend(macro, params.macro());

        for (MarketDefinition def : marketDefinitions) {
            Double newCost = result.referenceCost().get(def.id());
            MarketParams previous = marketParamsMap.get(def.id());

            double referenceCost = newCost != null ? newCost : previous.referenceCost();
            double plantSize = def.params().plantSize() * trend; // Grows plant size with trend & therefor with demand
            MarketParams updated = new MarketParams(referenceCost, plantSize, previous.targetUtilisation(), previous.group());
            marketParamsMap.put(def.id(), updated);

            Integer depth = result.depth().get(def.id());
            if (depth != null) {
                manufacturingDepths.put(def.id(), depth);
            }

            double demand = MacroRules.demand(def.baseDemand(), macro, params.macro(), updated.cycleSensitivity());
            currentDemand.put(def.id(), demand);
        }
    }

    private void applyReputationDrift(ItemId market) {
        Set<CompanyId> fulfilled = fulfilledToday.getOrDefault(market, Set.of());
        for (MarketParticipant participant : participantsOf(market).all()) {
            boolean hasFulfilled = fulfilled.contains(participant.companyId());
            double newReputation = ReputationRules.drift(participant.reputation(), hasFulfilled, reputationParams);
            if (newReputation != participant.reputation()) {
                updateReputation(market, participant.companyId(), newReputation);
            }
        }
        fulfilledToday.remove(market);
    }

    private void recordRawMaterialHistory(ItemId id, long day) {
        Deque<RawMaterialHistoryEntry> history = rawMaterialHistory.get(id);
        history.addLast(new RawMaterialHistoryEntry(id, day, rawMaterialStates.get(id).getPrice()));
        if (history.size() > HISTORY_LENGTH_DAYS) {
            history.removeFirst();
        }
    }

    private void recordMarketHistory(ItemId id, MarketState state, double delivered) {
        Deque<MarketHistoryEntry> history = marketHistory.get(id);
        history.addLast(new MarketHistoryEntry(
                id,
                ticksElapsed,
                macro.getDay(),
                state.getPriceLevel(),
                state.getDeviation(),
                state.getDisplayedPrice(),
                state.getCompetitors(),
                delivered
        ));
        if (history.size() > HISTORY_LENGTH_TICKS) {
            history.removeFirst();
        }
    }

    private void recordMacroHistory() {
        macroHistory.addLast(new MacroHistoryEntry(macro.getDay(), macro.getIndex(), macro.getPolicyRate()));
        if (macroHistory.size() > HISTORY_LENGTH_DAYS) {
            macroHistory.removeFirst();
        }
    }

    private MarketState stateOf(ItemId market) {
        MarketState state = marketStates.get(market);
        if (state == null) {
            throw new IllegalArgumentException("No such market: " + market);
        }
        return state;
    }

    /**
     * Records a successful delivery to the market.
     * @param market The market that received a delivery
     * @param quantity The amount of product delivered
     */
    public void recordDelivery(ItemId market, double quantity) {
        stateOf(market).recordDelivery(quantity);
    }

    /**
     * Records a delivery attributed to one of the market's registered participants.
     * @param market The market that received the delivery
     * @param company The delivering company, must be registered on this market
     * @param quantity The amount of product delivered
     */
    public void recordDelivery(ItemId market, CompanyId company, double quantity) {
        if (!participantsOf(market).isRegistered(company)) {
            throw new IllegalArgumentException(company + " is not a registered participant of market " + market + ".");
        }
        stateOf(market).recordDelivery(quantity);
    }

    /**
     * Marks that a company fulfilled at least one order for this product today (on time or late).
     * @param market The product
     * @param company The company
     */
    public void recordFulfillment(ItemId market, CompanyId company) {
        Objects.requireNonNull(market, "market must not be null.");
        Objects.requireNonNull(company, "company must not be null.");
        fulfilledToday.computeIfAbsent(market, m -> new LinkedHashSet<>()).add(company);
    }

    /**
     * Records a purchase of a raw material.
     * @param material The raw material
     * @param quantity The quantity purchased
     */
    public void recordPurchase(ItemId material, double quantity) {
        RawMaterialState state = rawMaterialStates.get(material);
        if (state == null) {
            throw new IllegalArgumentException("No such raw material: " + material);
        }

        state.recordPurchase(quantity);
    }

    private MarketParticipants participantsOf(ItemId market) {
        MarketParticipants participants = marketParticipants.get(market);
        if (participants == null) {
            throw new IllegalArgumentException("No such market: " + market);
        }
        return participants;
    }

    /**
     * Registers a company as a new participant of a market.
     * @param market The market
     * @param company The company
     * @param listPrice The starting list price
     * @param reputation The starting reputation
     */
    public void registerParticipant(ItemId market, CompanyId company, double listPrice, double reputation) {
        participantsOf(market).register(company, listPrice, reputation);
    }

    /**
     * Changes the list price of a company already registered on a market
     * @param market The market
     * @param company The company
     * @param listPrice The new listing price
     */
    public void updateListPrice(ItemId market, CompanyId company, double listPrice) {
        participantsOf(market).updateListPrice(company, listPrice);
    }

    /**
     * Changes the reputation of a company in one market
     * @param market The market
     * @param company The company
     * @param reputation The new reputation (0 to 100)
     */
    public void updateReputation(ItemId market, CompanyId company, double reputation) {
        participantsOf(market).updateReputation(company, reputation);
    }

    /**
     * Removes a participant from the market
     * @param market The market
     * @param company The company to remove
     */
    public void withdrawParticipant(ItemId market, CompanyId company) {
        participantsOf(market).withdraw(company);
    }

    /**
     * Whether a company currently takes part in a market
     * @param market The market
     * @param company The company
     * @return True, if the company is registered in the market
     */
    public boolean isParticipant(ItemId market, CompanyId company) {
        return participantsOf(market).isRegistered(company);
    }

    /**
     * Every company currently taking part in a market.
     * @param market The market
     * @return An immutable view, in registration order. Empty if nobody is registered.
     */
    public Collection<MarketParticipant> participants(ItemId market) {
        return participantsOf(market).all();
    }

    public int manufacturingDepth(ItemId market) {
        return manufacturingDepths.getOrDefault(market, 0);
    }

    public MarketSnapshot marketSnapshot(ItemId market) {
        MarketState state = stateOf(market);
        MarketParams marketParams = marketParamsMap.get(market);
        DayResult result = lastDayResults.get(market);

        return new MarketSnapshot(market,
                state.getPriceLevel(),
                state.getDeviation(),
                state.getDisplayedPrice(),
                state.getCompetitors(),
                state.getVisibleCompanies(),
                marketParams.referenceCost(),
                currentDemand.getOrDefault(market, Double.NaN),
                result != null ? result.utilisation() : Double.NaN,
                result != null ? result.overflow() : Double.NaN);
    }

    public RawMaterialSnapshot rawMaterialSnapshot(ItemId material) {
        RawMaterialState state = rawMaterialStates.get(material);
        if (state == null) {
            throw new IllegalArgumentException("No such raw material: " + material);
        }

        return new RawMaterialSnapshot(material, state.getPrice());
    }

    public List<MarketHistoryEntry> marketHistory(ItemId market) {
        Deque<MarketHistoryEntry> history = marketHistory.get(market);
        if (history == null) {
            throw new IllegalArgumentException("No such market: " + market);
        }
        return List.copyOf(history);
    }

    public List<RawMaterialHistoryEntry> rawMaterialHistory(ItemId material) {
        Deque<RawMaterialHistoryEntry> history = rawMaterialHistory.get(material);
        if (history == null) {
            throw new IllegalArgumentException("No such raw material: " + material);
        }
        return List.copyOf(history);
    }

    public List<MacroHistoryEntry> macroHistory() {
        return List.copyOf(macroHistory);
    }

    /**
     * @param day Only entries strictly after this day
     * @return The macro history entries recorded after the given day, in order
     */
    public List<MacroHistoryEntry> macroHistorySince(long day) {
        return macroHistory.stream().filter(entry -> entry.day() > day).toList();
    }

    public List<ItemId> marketIds() {
        return marketDefinitions.stream().map(MarketDefinition::id).toList();
    }

    public List<ItemId> rawMaterialIds() {
        return rawMaterialDefinitions.stream().map(RawMaterialDefinition::id).toList();
    }

    public long ticksElapsed() {
        return ticksElapsed;
    }

    public long currentDay() {
        return ticksElapsed / DAY_LENGTH;
    }

    public Phase currentPhase() {
        return MacroRules.phase(macro);
    }

    public double policyRate() {
        return macro.getPolicyRate();
    }

    @Override
    public String toString() {
        return "Economy{" +
                "graph=" + graph +
                ", params=" + params +
                ", rng=" + rng +
                ", rawMaterialDefinitions=" + rawMaterialDefinitions +
                ", marketDefinitions=" + marketDefinitions +
                ", valueOverrides=" + valueOverrides +
                ", rawMaterialStates=" + rawMaterialStates +
                ", marketStates=" + marketStates +
                ", marketParamsMap=" + marketParamsMap +
                ", currentDemand=" + currentDemand +
                ", lastDayResults=" + lastDayResults +
                ", discoveredScope=" + discoveredScope +
                ", macro=" + macro +
                ", ticksElapsed=" + ticksElapsed +
                '}';
    }
}
