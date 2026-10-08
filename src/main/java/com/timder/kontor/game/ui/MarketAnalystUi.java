package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.license.LicenseHoldingRules;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.MarketAnalystActions;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.chart.ChartHost;
import com.timder.kontor.game.ui.chart.MarketCharts;
import com.timder.kontor.game.ui.chart.RawMaterialCharts;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MarketAnalystUi {

    public static ModularUI create(EmployeeDeskContext context, MarketAnalystActions actions) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.market_analyst_desk.markets")), marketsTab(context));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.market_analyst_desk.raw_materials")), rawMaterialsTab(context));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement marketsTab(EmployeeDeskContext context) {
        UIElement content = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));

        ObservableList<MarketCandidate> markets = new ObservableList<>();
        SimpleBinding<Tag> marketsBinding = DataBindingBuilder.tagS2C(() -> marketsToTag(context))
                .onRemoteSyncReceived(tag -> markets.set(tagToMarketCandidates(tag).stream()
                        .sorted(Comparator
                                .comparing((MarketCandidate candidate) -> !candidate.licensed())
                                .thenComparing((MarketCandidate candidate) -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())).asItem().getDescription().getString(), String.CASE_INSENSITIVE_ORDER)
                        ).toList()))
                .build();
        content.addSyncValue(marketsBinding.getSyncValue());

        SearchComponent<MarketCandidate> search = new SearchComponent<>();
        search.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(MarketCandidate candidate) {
                return BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())).asItem().getDescription().getString();
            }

            @Override
            public void onResultSelected(@Nullable MarketCandidate value) {
            }

            @Override
            public void search(String word, IResultHandler<MarketCandidate> searchHandler) {
                String needle = word.trim().toLowerCase(Locale.ROOT);
                for (MarketCandidate candidate : markets.get()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    if (needle.isEmpty() || BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())).asItem().getDescription().getString().toLowerCase(Locale.ROOT).contains(needle)) {
                        searchHandler.acceptResult(candidate);
                    }
                }
            }
        });
        search.setCandidateUIProvider(candidate -> new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4))
                .addChildren(
                        new ItemSlot().setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())), 1)),
                        candidate.licensed()
                                ? UiLabels.paragraphPrimary(Component.translatable("ui.createkontor.market_analyst_desk.licensed_candidate", BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())).asItem().getDescription()), Horizontal.LEFT)
                                : UiLabels.paragraphPrimary(BuiltInRegistries.ITEM.get(ResourceLocation.parse(candidate.market().value())).asItem().getDescription(), Horizontal.LEFT)
                ));
        search.layout(layout -> layout
                .widthPercent(100)
                .height(22));
        search.searchStyle(style -> style
                .maxItemCount(8)
                .scrollerViewHeight(148));
        content.addChild(search);

        Label hintLabel = UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.select_market"), Horizontal.CENTER);
        UIElement hintBox = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .justifyContent(AlignContent.CENTER)
                .alignItems(AlignItems.CENTER));
        hintBox.addChild(hintLabel);
        content.addChild(hintBox);

        ItemId[] selected = {null}; // Server
        search.setOnValueChanged(candidate -> {
            hintBox.setDisplay(candidate == null);
            CompoundTag tag = new CompoundTag();
            tag.putString("Market", candidate == null ? "" : candidate.market().value());
            content.sendMessage("c2s_select_market", tag);
        });
        content.onMessage("c2s_select_market", tag -> {
            String raw = tag.getString("Market");
            ItemId id = raw.isBlank() ? null : new ItemId(raw);
            selected[0] = id != null && context.economy().isMarket(id) ? id : null;
        });

        UIElement marketBox = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .gapAll(4)
                .marginTop(4)
                .minHeight(0));

        Label currentPrice = (Label) UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.current_price", "-", "-"), Horizontal.LEFT)
                .style(style -> style.tooltips(Component.translatable("ui.createkontor.market_analyst_desk.current_price.tooltip")));
        Label currentCompetitors = (Label) UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.current_competitors", "-"), Horizontal.LEFT)
                .style(style -> style.tooltips(Component.translatable("ui.createkontor.market_analyst_desk.current_competitors.tooltip")));
        Label currentDemand = (Label) UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.current_demand", "-"), Horizontal.LEFT)
                .style(style -> style.tooltips(Component.translatable("ui.createkontor.market_analyst_desk.current_demand.tooltip")));

        UIElement labelRow = UiLabels.seperatedLabelRow(List.of(
                currentPrice,
                currentCompetitors,
                currentDemand
        )).layout(layout -> layout.justifyContent(AlignContent.FLEX_START));
        labelRow.setDisplay(false);
        marketBox.addChild(labelRow);

        ChartHost priceChartHost = new ChartHost();
        priceChartHost.layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .minHeight(0));
        priceChartHost.setDisplay(false);

        ChartHost demandOverflowChartHost = new ChartHost();
        demandOverflowChartHost.layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .minHeight(0));
        demandOverflowChartHost.setDisplay(false);

        TabView chartTabs = (TabView) UiContainer.largeTabView().layout(layout -> layout
                .widthPercent(100)
                .flexGrow(1)
                .flexBasis(0));
        chartTabs.addTab(UiContainer.tab(Component.translatable("ui.createkontor.market_analyst_desk.price_tab")), priceChartHost);
        chartTabs.addTab(UiContainer.tab(Component.translatable("ui.createkontor.market_analyst_desk.demand_overflow_tab")), demandOverflowChartHost);
        chartTabs.setDisplay(false);

        SimpleBinding<Tag> historyBinding = DataBindingBuilder.tagS2C(() -> marketHistoryToTag(context.economy(), selected[0]))
                .onRemoteSyncReceived(tag -> {
                    CompoundTag data = (CompoundTag) tag;
                    boolean hasMarket = data.contains("Market");
                    chartTabs.setDisplay(hasMarket);
                    labelRow.setDisplay(hasMarket);
                    if (!hasMarket) return;

                    List<MarketHistoryEntry> history = tagToHistory(data);
                    if (history.isEmpty()) {
                        chartTabs.setDisplay(false);
                        labelRow.setDisplay(false);
                        return;
                    }

                    currentPrice.setText(Component.translatable("ui.createkontor.market_analyst_desk.current_price", ComponentFormatting.moneyColored(Money.fromDollar(history.getLast().displayedPrice())), ComponentFormatting.percentColored(MarketAnalystUi.marketPriceChange(history))));
                    currentCompetitors.setText(Component.translatable("ui.createkontor.market_analyst_desk.current_competitors", Component.literal(String.valueOf((int) Math.round(history.getLast().competitors()))).withStyle(ChatFormatting.RED)));
                    currentDemand.setText(Component.translatable("ui.createkontor.market_analyst_desk.current_demand", Component.literal(String.valueOf((int) Math.round(history.getLast().demand()))).withStyle(ChatFormatting.GOLD)));
                    priceChartHost.show(MarketCharts.priceCompetitors(history));
                    demandOverflowChartHost.show(MarketCharts.demandOverflow(history));
                })
                .build();
        marketBox.addSyncValue(historyBinding.getSyncValue());
        marketBox.addChild(chartTabs);

        content.addChild(marketBox);
        return content;
    }

    public static UIElement rawMaterialsTab(EmployeeDeskContext context) {
        UIElement content = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));

        ObservableList<ItemId> rawMaterials = new ObservableList<>();
        SimpleBinding<Tag> rawMaterialsBinding = DataBindingBuilder.tagS2C(() -> rawMaterialsToTag(context))
                .onRemoteSyncReceived(tag -> rawMaterials.set(tagToRawMaterials(tag).stream()
                        .sorted(Comparator.comparing(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDescription().getString(), String.CASE_INSENSITIVE_ORDER)
                        ).toList()))
                .build();
        content.addSyncValue(rawMaterialsBinding.getSyncValue());

        SearchComponent<ItemId> search = new SearchComponent<>();
        search.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(ItemId id) {
                return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDescription().getString();
            }

            @Override
            public void onResultSelected(@Nullable ItemId value) {
            }

            @Override
            public void search(String word, IResultHandler<ItemId> searchHandler) {
                String needle = word.trim().toLowerCase(Locale.ROOT);
                for (ItemId id : rawMaterials.get()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    if (needle.isEmpty() || BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDescription().getString().toLowerCase(Locale.ROOT).contains(needle)) {
                        searchHandler.acceptResult(id);
                    }
                }
            }
        });
        search.setCandidateUIProvider(id -> new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4))
                .addChildren(
                        new ItemSlot().setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())), 1)),
                        UiLabels.paragraphPrimary(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDescription(), Horizontal.LEFT)
                ));
        search.layout(layout -> layout
                .widthPercent(100)
                .height(22));
        search.searchStyle(style -> style
                .maxItemCount(8)
                .scrollerViewHeight(148));
        content.addChild(search);

        Label hintLabel = UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.select_raw_market"), Horizontal.CENTER);
        UIElement hintBox = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .justifyContent(AlignContent.CENTER)
                .alignItems(AlignItems.CENTER));
        hintBox.addChild(hintLabel);
        content.addChild(hintBox);

        ItemId[] selected = {null}; // Server
        search.setOnValueChanged(id -> {
            hintBox.setDisplay(id == null);
            CompoundTag tag = new CompoundTag();
            tag.putString("RawMaterial", id == null ? "" : id.value());
            content.sendMessage("c2s_select_raw_material", tag);
        });
        content.onMessage("c2s_select_raw_material", tag -> {
            String raw = tag.getString("RawMaterial");
            ItemId id = raw.isBlank() ? null : new ItemId(raw);
            selected[0] = id != null && context.economy().isRawMaterial(id) ? id : null;
        });

        UIElement rawMaterialBox = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .gapAll(4)
                .marginTop(4)
                .minHeight(0));

        Label currentPrice = (Label) UiLabels.secondary(Component.translatable("ui.createkontor.market_analyst_desk.current_price", "-", "-"), Horizontal.LEFT)
                .style(style -> style.tooltips(Component.translatable("ui.createkontor.market_analyst_desk.current_price.tooltip")));
        currentPrice.setDisplay(false);
        rawMaterialBox.addChild(currentPrice);

        ChartHost priceChartHost = new ChartHost();
        priceChartHost.layout(layout -> layout
                .widthPercent(100)
                .flexBasis(0)
                .flexGrow(1)
                .minHeight(0));
        priceChartHost.setDisplay(false);
        rawMaterialBox.addChild(priceChartHost);

        SimpleBinding<Tag> historyBinding = DataBindingBuilder.tagS2C(() -> rawMaterialHistoryToTag(context.economy(), selected[0]))
                .onRemoteSyncReceived(tag -> {
                    CompoundTag data = (CompoundTag) tag;
                    boolean hasRawMaterial = data.contains("RawMaterial");
                    priceChartHost.setDisplay(hasRawMaterial);
                    currentPrice.setDisplay(hasRawMaterial);
                    if (!hasRawMaterial) return;

                    List<RawMaterialHistoryEntry> history = tagToRawMaterialHistory(data);
                    if (history.isEmpty()) {
                        priceChartHost.setDisplay(false);
                        currentPrice.setDisplay(false);
                        return;
                    }

                    currentPrice.setText(Component.translatable("ui.createkontor.market_analyst_desk.current_price", ComponentFormatting.moneyColored(Money.fromDollar(history.getLast().price())), ComponentFormatting.percentColored(rawMaterialPriceChange(history))));
                    priceChartHost.show(RawMaterialCharts.price(history));
                })
                .build();
        rawMaterialBox.addSyncValue(historyBinding.getSyncValue());

        content.addChild(rawMaterialBox);
        return content;
    }

    public record MarketCandidate(ItemId market, boolean licensed) {}

    private static Tag marketsToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (ItemId id : context.economy().marketIds()) {
            CompoundTag e = new CompoundTag();
            e.putString("Market", id.value());
            e.putBoolean("Licensed", LicenseHoldingRules.mayTrade(context.company(), params.licenses(), id));
            list.add(e);
        }
        return list;
    }

    private static List<MarketCandidate> tagToMarketCandidates(Tag tag) {
        ListTag list = (ListTag) tag;
        List<MarketCandidate> candidates = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            candidates.add(new MarketCandidate(new ItemId(e.getString("Market")), e.getBoolean("Licensed")));
        }
        return candidates;
    }

    private static Tag marketHistoryToTag(Economy economy, @Nullable ItemId id) {
        CompoundTag tag = new CompoundTag();
        if (id == null) return tag;

        List<MarketHistoryEntry> history = economy.marketHistory(id);
        int wanted = 360 * MarketRules.TRADING_TICKS_PER_DAY;
        ListTag list = new ListTag();
        for (MarketHistoryEntry e : history.subList(Math.max(0, history.size() - wanted), history.size())) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Tick", e.tick());
            entry.putLong("Day", e.day());
            entry.putDouble("PriceLevel", e.priceLevel());
            entry.putDouble("Deviation", e.deviation());
            entry.putDouble("Price", e.displayedPrice());
            entry.putDouble("Competitors", e.competitors());
            entry.putDouble("Delivered", e.deliveredThisTick());
            entry.putDouble("Demand", e.demand());
            entry.putDouble("Overflow", e.overflow());
            list.add(entry);
        }
        tag.putString("Market", id.value());
        tag.putDouble("ReferenceCost", economy.marketSnapshot(id).referenceCost());
        tag.put("History", list);
        return tag;
    }

    private static List<MarketHistoryEntry> tagToHistory(Tag raw) {
        CompoundTag tag = (CompoundTag) raw;
        ItemId id = new ItemId(tag.getString("Market"));
        List<MarketHistoryEntry> history = new ArrayList<>();
        for (Tag t : tag.getList("History", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            history.add(new MarketHistoryEntry(
                    id,
                    e.getLong("Tick"),
                    e.getLong("Day"),
                    e.getDouble("PriceLevel"),
                    e.getDouble("Deviation"),
                    e.getDouble("Price"),
                    e.getDouble("Competitors"),
                    e.getDouble("Delivered"),
                    e.getDouble("Demand"),
                    e.contains("Overflow", Tag.TAG_DOUBLE) ? e.getDouble("Overflow") : Double.NaN)
            );
        }
        return history;
    }

    private static Tag rawMaterialsToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        for (ItemId id : context.economy().rawMaterialIds()) {
            list.add(StringTag.valueOf(id.value()));
        }
        return list;
    }

    private static List<ItemId> tagToRawMaterials(Tag tag) {
        ListTag list = (ListTag) tag;
        List<ItemId> ids = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ids.add(new ItemId(list.getString(i)));
        }
        return ids;
    }

    private static Tag rawMaterialHistoryToTag(Economy economy, @Nullable ItemId id) {
        CompoundTag tag = new CompoundTag();
        if (id == null) return tag;

        List<RawMaterialHistoryEntry> history = economy.rawMaterialHistory(id);
        ListTag list = new ListTag();
        for (RawMaterialHistoryEntry e : history.subList(Math.max(0, history.size() - 360), history.size())) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Day", e.day());
            entry.putDouble("Price", e.price());
            list.add(entry);
        }
        tag.putString("RawMaterial", id.value());
        tag.put("History", list);
        return tag;
    }

    private static List<RawMaterialHistoryEntry> tagToRawMaterialHistory(Tag raw) {
        CompoundTag tag = (CompoundTag) raw;
        ItemId id = new ItemId(tag.getString("RawMaterial"));
        List<RawMaterialHistoryEntry> history = new ArrayList<>();
        for (Tag t : tag.getList("History", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            history.add(new RawMaterialHistoryEntry(
                    id,
                    e.getLong("Day"),
                    e.getDouble("Price"))
            );
        }
        return history;
    }

    public static double marketPriceChange(List<MarketHistoryEntry> history) {
        int size = history.size();
        int previousIndex = size - 1 - MarketRules.TRADING_TICKS_PER_DAY;
        if (previousIndex < 0) {
            return 0.0;
        }

        double previous = history.get(previousIndex).displayedPrice();
        double current = history.get(size - 1).displayedPrice();
        if (!(previous > 0) || !Double.isFinite(current)) {
            return 0.0;
        }
        return current / previous - 1.0;
    }

    public static double rawMaterialPriceChange(List<RawMaterialHistoryEntry> history) {
        int size = history.size();
        int previousIndex = size - 1 - MarketRules.TRADING_TICKS_PER_DAY;
        if (previousIndex < 0) {
            return 0.0;
        }

        double previous = history.get(previousIndex).price();
        double current = history.get(size - 1).price();
        if (!(previous > 0) || !Double.isFinite(current)) {
            return 0.0;
        }
        return current / previous - 1.0;
    }
}
