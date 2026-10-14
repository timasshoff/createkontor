package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.logistics.LogisticsStorageRules;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.LogisticsManagerDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.chart.CompanyCharts;
import com.timder.kontor.game.ui.chart.segment.SegmentBarElement;
import com.timder.kontor.game.ui.chart.segment.SegmentBarSpec;
import com.timder.kontor.game.ui.element.*;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class LogisticsManagerUi {
    public static ModularUI create(EmployeeDeskContext context) {
        LogisticsManagerDeskBlockEntity be = (LogisticsManagerDeskBlockEntity) context.desk();

        ObservableValue<Boolean> networkConnected = new ObservableValue<>(false);
        SimpleBinding<Boolean> networkConnectedBinding = DataBindingBuilder.boolS2C(() -> context.company().logistics().isConnected())
                .onRemoteSyncReceived(networkConnected::set)
                .build();

        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.logistics_manager_desk.network_overview")), networkOverview(context, be, networkConnected, networkConnectedBinding));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.logistics_manager_desk.orders")), orders(context, be, networkConnected, networkConnectedBinding));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement networkOverview(EmployeeDeskContext context, LogisticsManagerDeskBlockEntity be, ObservableValue<Boolean> networkConnected, SimpleBinding<Boolean> networkConnectedBinding) {
        UIElement content = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        content.addSyncValue(networkConnectedBinding.getSyncValue());

        UIElement notConnected = getNotConnectedElement(be);
        UIElement connected = new UIElement().layout(layout -> layout.gapAll(4));

        content.addChildren(connected, notConnected);
        networkConnected.addListener(() -> {
            boolean isConnected = networkConnected.get();
            notConnected.setDisplay(!isConnected);
            connected.setDisplay(isConnected);
        });

        Label totalItemCountLabel = UiLabels.secondary(Component.empty(), Horizontal.CENTER);
        totalItemCountLabel.addSyncValue(
                DataBindingBuilder.componentS2C(() -> context.company().logistics().isConnected() ? Component.translatable("ui.createkontor.logistics_manager_desk.total_items", ComponentFormatting.highlightStandard(String.valueOf(LogisticsNetworkAccess.totalItemCount(context.company().logistics().networkId())))) : Component.empty())
                        .onRemoteSyncReceived(totalItemCountLabel::setText)
                        .build().getSyncValue()
        );

        Label expectedCostLabel = UiLabels.secondary(Component.empty(), Horizontal.CENTER);
        expectedCostLabel.addSyncValue(
                DataBindingBuilder.componentS2C(() -> {
                            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
                            if (!context.company().logistics().isConnected()) {
                                return Component.empty();
                            }
                            long itemsTicks = LogisticsNetworkAccess.totalItemCount(context.company().logistics().networkId()) * Economy.DAY_LENGTH;
                            return Component.translatable("ui.createkontor.logistics_manager_desk.expected_cost", ComponentFormatting.moneyNegative(LogisticsStorageRules.storageCost(itemsTicks, params)));
                        })
                        .onRemoteSyncReceived(expectedCostLabel::setText)
                        .build().getSyncValue()
        );

        connected.addChild(UiContainer.headerWithSubtitle(
                UiLabels.h2(Component.translatable("ui.createkontor.logistics_manager_desk.connected_network"), Horizontal.CENTER),
                UiLabels.seperatedLabelRow(List.of(
                        totalItemCountLabel,
                        expectedCostLabel
                ))
        ));

        ObservableValue<Integer> unloadedLinks = new ObservableValue<>(0);
        SimpleBinding<Integer> unloadedLinksBinding = DataBindingBuilder.intValS2C(() -> context.company().logistics().isConnected() ? LogisticsNetworkAccess.unloadedLinkCount(context.company().logistics().networkId()) : 0)
                .onRemoteSyncReceived(unloadedLinks::set)
                .build();
        connected.addSyncValue(unloadedLinksBinding.getSyncValue());
        unloadedLinks.addListener(() -> {
            if (unloadedLinks.get() > 0) {
                connected.addChild(UiLabels.paragraphError(Component.translatable("ui.createkontor.logistics_manager_desk.unloaded_links", ComponentFormatting.highlightError(String.valueOf(unloadedLinks.get()))), Horizontal.LEFT));
            }
        });
        unloadedLinks.runListenersOnce();

        SegmentBarElement stockBar = SegmentBarElement.from(SegmentBarSpec.builder("").build());
        stockBar.layout(layout -> layout.widthPercent(100));
        stockBar.addSyncValue(DataBindingBuilder.tagS2C(() -> stockToTag(context))
                .onRemoteSyncReceived(tag -> {
                    StockBreakdown breakdown = tagToStock((CompoundTag) tag);
                    stockBar.update(CompanyCharts.stock(breakdown.top(), breakdown.others()));
                })
                .build().getSyncValue());
        connected.addChild(stockBar);

        networkConnected.runListenersOnce();
        return content;
    }

    public static UIElement orders(EmployeeDeskContext context, LogisticsManagerDeskBlockEntity be, ObservableValue<Boolean> networkConnected, SimpleBinding<Boolean> networkConnectedBinding) {
        UIElement content = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        content.addSyncValue(networkConnectedBinding.getSyncValue());

        UIElement notConnected = getNotConnectedElement(be);
        UIElement connected = new UIElement().layout(layout -> layout.gapAll(4).heightPercent(100));

        content.addChildren(connected, notConnected);
        networkConnected.addListener(() -> {
            boolean isConnected = networkConnected.get();
            notConnected.setDisplay(!isConnected);
            connected.setDisplay(isConnected);
        });

        SplitView.Horizontal split = new SplitView.Horizontal();
        split.layout(layout -> layout.widthPercent(100).heightPercent(100));
        split.setMinPercentage(40);
        split.setMaxPercentage(70);
        split.setPercentage(30);

        split.left(orderDeliverySettings(be));
        split.right(orderDeliveryOverview(be));
        connected.addChild(split);

        networkConnected.runListenersOnce();
        return content;
    }

    public static UIElement orderDeliverySettings(LogisticsManagerDeskBlockEntity be) {
        ScrollerView content = (ScrollerView) UiContainer.tabScroller().style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8));

        content.addScrollViewChildren(UiLabels.h2(Component.translatable("ui.createkontor.logistics_manager_desk.order_delivery_settings"), Horizontal.LEFT));
        content.addScrollViewChildren(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.logistics_manager_desk.ordering_explanation"), Horizontal.LEFT));

        ObservableValue<Boolean> orderingEnabled = new ObservableValue<>(false);
        SimpleBinding<Boolean> orderingEnabledBinding = DataBindingBuilder.boolS2C(be::isOrderingEnabled)
                .onRemoteSyncReceived(orderingEnabled::set)
                .build();

        UIElement statusBox = new UIElement().style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8).widthPercent(100));
        statusBox.addChild(UiLabels.paragraphPrimary(Component.translatable("ui.createkontor.logistics_manager_desk.status"), Horizontal.LEFT));
        Label statusLabel = UiLabels.paragraphSecondary(Component.empty(), Horizontal.LEFT);
        statusLabel.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.translatable("ui.createkontor.logistics_manager_desk.status." + be.getOrderingStatus().name().toLowerCase()))
                        .onRemoteSyncReceived(statusLabel::setText)
                        .build().getSyncValue());
        statusBox.addChild(statusLabel);
        content.addScrollViewChildren(statusBox);

        UiSwitches.LabeledSwitch ordering = UiSwitches.labeled(
                Component.translatable("ui.createkontor.logistics_manager_desk.ordering_enabled"),
                be.isOrderingEnabled(),
                isOn -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("Enable", isOn);
                    content.sendMessage("c2s_set_ordering_enabled", tag);
                });
        ordering.addSyncValue(orderingEnabledBinding.getSyncValue());
        orderingEnabled.addListener(() -> ordering.getSwitch().setOn(orderingEnabled.get(), false));
        content.onMessage("c2s_set_ordering_enabled", (tag) -> be.setOrderingEnabled(tag.getBoolean("Enable")));
        content.addScrollViewChildren(ordering);

        UiTextFields.LabeledTextField address = UiTextFields.labeled(
                Component.translatable("ui.createkontor.logistics_manager_desk.address"),
                Component.translatable("ui.createkontor.logistics_manager_desk.address"),
                Component.translatable("ui.createkontor.logistics_manager_desk.address.tooltip"),
                be.getOrderAddress(),
                text -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Address", text);
                    content.sendMessage("c2s_set_order_address", tag);
                });
        orderingEnabled.addListener(() -> address.setDisplay(orderingEnabled.get()));
        address.addSyncValue(DataBindingBuilder.stringS2C(be::getOrderAddress)
                .onRemoteSyncReceived(value -> address.getTextField().setText(value, false))
                .build().getSyncValue());
        content.addScrollViewChildren(address);
        content.onMessage("c2s_set_order_address", tag -> be.setOrderAddress(tag.getString("Address")));

        UiTextFields.LabeledTextField minimumStock = UiTextFields.labeled(
                Component.translatable("ui.createkontor.logistics_manager_desk.minimum_stock"),
                Component.translatable("ui.createkontor.logistics_manager_desk.minimum_stock"),
                Component.translatable("ui.createkontor.logistics_manager_desk.minimum_stock.tooltip"),
                String.valueOf(be.getMinimumStock()),
                text -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("MinimumStock", Integer.parseInt(text));
                    content.sendMessage("c2s_set_minimum_stock", tag);
                });
        minimumStock.getTextField().setNumbersOnlyInt(0, LogisticsManagerDeskBlockEntity.MAX_MINIMUM_STOCK);
        orderingEnabled.addListener(() -> minimumStock.setDisplay(orderingEnabled.get()));
        minimumStock.addSyncValue(DataBindingBuilder.intValS2C(be::getMinimumStock)
                .onRemoteSyncReceived(value -> minimumStock.getTextField().setText(String.valueOf(value), false))
                .build().getSyncValue());
        content.addScrollViewChildren(minimumStock);
        content.onMessage("c2s_set_minimum_stock", tag -> be.setMinimumStock(tag.getInt("MinimumStock")));

        return content;
    }

    public static UIElement orderDeliveryOverview(LogisticsManagerDeskBlockEntity be) {
        ScrollerView content = (ScrollerView) UiContainer.tabScroller().style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8));

        ObservableList<OrderEntry> orderedQuantities = new ObservableList<>();
        SimpleBinding<Tag> orderedQuantitiesBinding = DataBindingBuilder.tagS2C(() -> orderedQuantitiesToTag(be.getOrderedQuantitiesPercent()))
                .onRemoteSyncReceived(tag -> orderedQuantities.set(tagToOrderedQuantities(tag)))
                .build();

        Label title = UiLabels.h2(Component.translatable("ui.createkontor.logistics_manager_desk.order_delivery_overview"), Horizontal.LEFT);
        orderedQuantities.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.logistics_manager_desk.order_delivery_overview_count",
                orderedQuantities.get().size())
        ));
        title.addSyncValue(orderedQuantitiesBinding.getSyncValue());
        content.addScrollViewChildren(title);

        UIElement ordersBox = new UIElement().layout(layout -> layout.gapColumn(4)).addSyncValue(orderedQuantitiesBinding.getSyncValue());
        orderedQuantities.addListener(() -> {
            ordersBox.clearAllChildren();

            if (orderedQuantities.get().isEmpty()) {
                ordersBox.addChild(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.logistics_manager_desk.no_orders"), Horizontal.LEFT));
            }

            for (OrderEntry entry : orderedQuantities.get()) {
                UIElement orderElement = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8).gapAll(4));

                UIElement titleRow = new UIElement()
                        .layout(layout -> layout
                                .flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .justifyContent(AlignContent.FLEX_START)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .marginBottom(4)
                                .widthPercent(100));

                titleRow.addChild(new ItemSlot().setItem(new ItemStack(entry.product(), entry.quantity)));
                titleRow.addChild(UiLabels.paragraphPrimary(Component.empty()
                                .append(Component.literal(String.valueOf(entry.quantity)).withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" "))
                                .append(entry.product().getDescription().copy().withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" (#" + entry.number() + ")").withStyle(ChatFormatting.DARK_GRAY)), Horizontal.LEFT)
                        .textStyle(style -> style.textAlignVertical(Vertical.CENTER))
                        .layout(layout -> layout
                                .widthAuto()
                                .flexBasis(0)
                                .flexGrow(1)));
                orderElement.addChild(titleRow);

                orderElement.addChild(UiLabels.secondary(Component.translatable("ui.createkontor.logistics_manager_desk.order_missing", Math.round(entry.quantity() * (1.0 - entry.percent()))), Horizontal.LEFT));
                orderElement.addChild(UiProgressBars.ofFraction(entry.percent()).style(style -> style.tooltips(Component.translatable("ui.createkontor.logistics_manager_desk.order_progress.tooltip", Math.round(Math.max(0.0, Math.min(1.0, entry.percent())) * 100.0)))));

                if (entry.percent() > 0) {
                    orderElement.addChild(UiButtons.criticalWrapping(Component.translatable("ui.createkontor.logistics_manager_desk.order_reset"))
                            .setOnClick((e) -> {
                                CompoundTag order = new CompoundTag();
                                order.putLong("Number", entry.number());
                                ordersBox.sendMessage("c2s_reset_order", order);
                            }).style(style -> style.tooltips(Component.translatable("ui.createkontor.logistics_manager_desk.order_reset.tooltip"))).layout(layout -> layout.height(14)));
                }

                ordersBox.addChild(orderElement);
            }

            if (!orderedQuantities.get().isEmpty()) {
                ordersBox.addChild(UiButtons.criticalWrapping(Component.translatable("ui.createkontor.logistics_manager_desk.order_reset_all")).setOnClick((e) -> ordersBox.sendMessage("c2s_reset_all_orders")).style(style -> style.tooltips(Component.translatable("ui.createkontor.logistics_manager_desk.order_reset_all.tooltip"))));
            }
        });
        ordersBox.onMessage("c2s_reset_order", (tag) -> be.resetOrderProgress(tag.getLong("Number")));
        ordersBox.onMessage("c2s_reset_all_orders", (tag) -> be.resetOrderProgress());
        content.addScrollViewChildren(ordersBox);

        return content;
    }

    private static UIElement getNotConnectedElement(LogisticsManagerDeskBlockEntity be) {
        return UiContainer.headerWithSubtitle(
                UiLabels.h2(Component.translatable("ui.createkontor.logistics_manager_desk.no_network"), Horizontal.CENTER),
                UiLabels.error(Component.translatable(be.getConnectionStatus().translationKey()).withStyle(ChatFormatting.DARK_RED), Horizontal.CENTER)
        );
    }

    private static Tag stockToTag(EmployeeDeskContext context) {
        CompoundTag tag = new CompoundTag();
        UUID network = context.company().logistics().networkId();
        if (network == null) return tag;

        List<Map.Entry<ItemId, Long>> stock = LogisticsNetworkAccess.stockByItem(network);
        ListTag top = new ListTag();
        long others = 0;
        for (int i = 0; i < stock.size(); i++) {
            if (i < 5) {
                CompoundTag entry = new CompoundTag();
                entry.putString("Item", stock.get(i).getKey().value());
                entry.putLong("Count", stock.get(i).getValue());
                top.add(entry);
            } else {
                others += stock.get(i).getValue();
            }
        }
        tag.put("Top", top);
        tag.putLong("Others", others);
        return tag;
    }

    public record StockBreakdown(List<Map.Entry<ItemId, Long>> top, long others) {}

    public static StockBreakdown tagToStock(CompoundTag data) {
        List<Map.Entry<ItemId, Long>> top = new ArrayList<>();

        ListTag topData = data.getList("Top", Tag.TAG_COMPOUND);
        for (int i = 0; i < topData.size(); i++) {
            CompoundTag entry = topData.getCompound(i);
            top.add(Map.entry(new ItemId(entry.getString("Item")), entry.getLong("Count")));
        }
        return new StockBreakdown(List.copyOf(top), data.getLong("Others"));
    }

    public static ListTag orderedQuantitiesToTag(Map<Order, Double> quantities) {
        ListTag list = new ListTag();
        for (Map.Entry<Order, Double> entry : quantities.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Number", entry.getKey().getNumber());
            entryTag.putString("Product", entry.getKey().getProduct().value());
            entryTag.putInt("Quantity", entry.getKey().getQuantity());
            entryTag.putDouble("Percent", entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    public record OrderEntry(long number, Item product, int quantity, double percent) {}

    public static List<OrderEntry> tagToOrderedQuantities(Tag tag) {
        List<OrderEntry> quantities = new ArrayList<>();
        if (!(tag instanceof ListTag list)) {
            return quantities;
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            quantities.add(new OrderEntry(entryTag.getLong("Number"), BuiltInRegistries.ITEM.get(ResourceLocation.parse(entryTag.getString("Product"))), entryTag.getInt("Quantity"), entryTag.getDouble("Percent")));
        }
        return quantities;
    }
}
