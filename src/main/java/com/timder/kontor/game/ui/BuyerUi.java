package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.timder.kontor.client.ui.KontorToasts;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.purchase.PurchaseKind;
import com.timder.kontor.core.company.purchase.PurchaseOffer;
import com.timder.kontor.core.company.purchase.PurchaseOffers;
import com.timder.kontor.core.company.purchase.PurchaseQuote;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.buyer.BuyerDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.*;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BuyerUi {
    public static ModularUI create(EmployeeDeskContext context, List<List<ClipboardEntry>> materialList) {
        BuyerDeskBlockEntity be = (BuyerDeskBlockEntity) context.desk();
        ObservableValue<Map<ItemId, Integer>> quote = new ObservableValue<>(Map.of());

        var tabView = UiContainer.largeTabView();
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.buyer_desk.catalog")),
                catalogTab(context, be, quote));
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.buyer_desk.auto_stock")),
                autoStock(context, be));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));

        if (materialList != null && !materialList.isEmpty()) {
            for (List<ClipboardEntry> subList : materialList) {
                for (ClipboardEntry entry : subList) {
                    if (entry.itemAmount < 1 || entry.icon.isEmpty()) {
                        continue;
                    }
                    ItemId id = new ItemId(
                            BuiltInRegistries.ITEM.getKey(entry.icon.getItem()).toString());
                    addToQuote(
                            quote,
                            id,
                            entry.itemAmount); // Items that do not have offers will be caught in buildQuote()
                }
            }
            if (context.player().level() instanceof ClientLevel) {
                KontorToasts.show(
                        ToastPayload.ToastKind.INFO,
                        Component.translatable("message.createkontor.buyer_desk.material_list_added"));
            }
        }

        return new ModularUI(UiStyles.ui(root), context.player());
    }

    public static UIElement catalogTab(
            EmployeeDeskContext context, BuyerDeskBlockEntity be, ObservableValue<Map<ItemId, Integer>> quote) {
        ObservableList<PurchaseOffer> offers = new ObservableList<>();
        SimpleBinding<Tag> offersBinding = DataBindingBuilder.tagS2C(() -> offersToTag(context))
                .onRemoteSyncReceived(tag -> offers.set(tagToOffers(tag).stream()
                        .sorted(Comparator.comparing(
                                offer -> BuiltInRegistries.ITEM
                                        .get(ResourceLocation.parse(offer.item().value()))
                                        .asItem()
                                        .getDescription()
                                        .getString(),
                                String.CASE_INSENSITIVE_ORDER))
                        .toList()))
                .build();

        SplitView.Horizontal split = new SplitView.Horizontal();
        split.layout(layout -> layout.widthPercent(100).heightPercent(100));
        split.setMinPercentage(50);
        split.setMaxPercentage(80);
        split.setPercentage(75);

        split.left(offers(offers, offersBinding, quote));
        split.right(quote(context, be, offers, offersBinding, quote));

        return split;
    }

    public static UIElement offers(
            ObservableList<PurchaseOffer> offers,
            SimpleBinding<Tag> offersBinding,
            ObservableValue<Map<ItemId, Integer>> quote) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .style(UiStyles.PANEL)
                .layout(layout -> layout.paddingAll(8));
        content.addSyncValue(offersBinding.getSyncValue());

        Label title = (Label) UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.catalog"), Horizontal.LEFT)
                .textStyle(style -> style.adaptiveWidth(false));
        offers.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.buyer_desk.catalog_count", offers.get().size())));
        content.addChild(title);

        content.addChild(UiContainer.searchableGrid(
                offers,
                (PurchaseOffer offer) -> {
                    UIElement element = new UIElement().style(UiStyles.CARD).layout(layout -> layout.paddingAll(8));

                    if (offer.kind() == PurchaseKind.RAW_MATERIAL) {
                        element.addChild(UiLabels.paragraphTertiary(
                                Component.translatable("ui.createkontor.buyer_desk.catalog.raw_material"),
                                Horizontal.LEFT));
                    } else {
                        element.addChild(UiLabels.paragraphTertiary(
                                Component.translatable("ui.createkontor.buyer_desk.catalog.product"), Horizontal.LEFT));
                    }

                    UIElement titleRow = new UIElement()
                            .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                    .flexWrap(FlexWrap.WRAP)
                                    .justifyContent(AlignContent.FLEX_START)
                                    .alignItems(AlignItems.CENTER)
                                    .gapAll(4)
                                    .marginBottom(4)
                                    .widthPercent(100));
                    titleRow.addChild(new ItemSlot()
                            .setItem(new ItemStack(
                                    BuiltInRegistries.ITEM.get(
                                            ResourceLocation.parse(offer.item().value())),
                                    1)));
                    titleRow.addChild(UiLabels.paragraphPrimary(
                                    Component.empty()
                                            .append(BuiltInRegistries.ITEM
                                                    .get(ResourceLocation.parse(
                                                            offer.item().value()))
                                                    .asItem()
                                                    .getDescription())
                                            .withStyle(ChatFormatting.GOLD),
                                    Horizontal.LEFT)
                            .textStyle(style -> style.textAlignVertical(Vertical.CENTER))
                            .layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1)));
                    element.addChild(titleRow);

                    element.addChild(UiLabels.paragraphPrimary(
                            Component.translatable(
                                    "ui.createkontor.buyer_desk.catalog.price",
                                    ComponentFormatting.moneyColored(offer.unitPrice())),
                            Horizontal.LEFT));

                    int[] quantity = {1};
                    UIElement buyRow = new UIElement()
                            .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                    .alignItems(AlignItems.CENTER)
                                    .gapAll(4)
                                    .widthPercent(100)
                                    .marginTopAuto());

                    TextField quantityField = new TextField();
                    quantityField.setNumbersOnlyInt(1, BuyerDeskBlockEntity.MAX_GOOD_SIZE);
                    quantityField.setText("1", false);
                    quantityField.setTextResponder(text -> {
                        try {
                            quantity[0] = Math.max(
                                    1, Math.min(Integer.parseInt(text.trim()), BuyerDeskBlockEntity.MAX_GOOD_SIZE));
                        } catch (NumberFormatException e) {
                            quantity[0] = 1;
                        }
                    });
                    quantityField.addEventListener(
                            UIEvents.BLUR, event -> quantityField.setText(String.valueOf(quantity[0]), false));
                    quantityField.layout(layout -> layout.width(40));

                    Button add = (Button) UiButtons.secondary(
                                    Component.translatable("ui.createkontor.buyer_desk.catalog.add"))
                            .setOnClick(e -> addToQuote(
                                    quote,
                                    offer.item(),
                                    Math.max(1, Math.min(quantity[0], BuyerDeskBlockEntity.MAX_GOOD_SIZE))))
                            .layout(layout ->
                                    layout.widthAuto().flexBasis(0).flexGrow(1).height(14));

                    buyRow.addChildren(quantityField, add);
                    element.addChild(buyRow);

                    return element;
                },
                (PurchaseOffer offer) -> List.of(BuiltInRegistries.ITEM
                        .get(ResourceLocation.parse(offer.item().value()))
                        .asItem()
                        .getDescription()
                        .getString()),
                Component.translatable("ui.createkontor.buyer_desk.catalog.search"),
                Component.translatable("ui.createkontor.buyer_desk.catalog.no_result"),
                4,
                128));

        return content;
    }

    public static UIElement quote(
            EmployeeDeskContext context,
            BuyerDeskBlockEntity be,
            ObservableList<PurchaseOffer> offers,
            SimpleBinding<Tag> offersBinding,
            ObservableValue<Map<ItemId, Integer>> quote) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100)
                        .heightPercent(100)
                        .paddingAll(8)
                        .gapAll(4))
                .style(UiStyles.PANEL);

        int[] purchases = {0}; // Server sided
        int[] seen = {0}; // Client sided

        content.onMessage("c2s_buy", tag -> {
            be.setAddress(tag.getString("Address"));
            Optional<S2CActionResult> result = be.buy(tagToCart(tag), tag.getLong("TotalCents"));
            ToastPayload.send(context.player(), result);
            if (result.isPresent() && result.get().kind() == ToastPayload.ToastKind.SUCCESS) {
                purchases[0]++;
            }
        });
        content.onMessage("c2s_address", tag -> be.setAddress(tag.getString("Address")));

        content.addSyncValue( // Clears quote on successful purchase
                DataBindingBuilder.tagS2C(() -> IntTag.valueOf(purchases[0]))
                        .onRemoteSyncReceived(tag -> {
                            int count = tag instanceof IntTag number ? number.getAsInt() : 0;
                            if (count != seen[0]) {
                                seen[0] = count;
                                quote.set(Map.of());
                            }
                        })
                        .build()
                        .getSyncValue());

        Label title = (Label) UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.quote"), Horizontal.LEFT)
                .textStyle(style -> style.adaptiveWidth(false));
        content.addChild(title);
        content.addSyncValue(offersBinding.getSyncValue());

        ScrollerView linesScroller = UiContainer.tabScroller();
        linesScroller.layout(
                layout -> layout.heightAuto().flexBasis(0).flexGrow(1).minHeight(0));
        linesScroller.viewContainer.layout(
                layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(10));

        float[] appliedWidth = {0};
        linesScroller.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
            float available = linesScroller.getContentWidth() - 10;
            if (available <= 0 || Math.abs(available - appliedWidth[0]) < 0.01f) return;
            appliedWidth[0] = available;
            linesScroller.viewContainer.layout(layout -> layout.width(available));
        });

        UIElement linesBox = new UIElement()
                .layout(layout ->
                        layout.widthPercent(100).flexBasis(0).flexGrow(1).minHeight(0))
                .addChildren(linesScroller);
        content.addChild(linesBox);

        Label total = UiLabels.paragraphPrimary(Component.empty(), Horizontal.LEFT);
        content.addChild(total);

        quote.addListener(() -> buildQuoteElement(linesScroller, total, quote, offers.get()));
        offers.addListener(() -> buildQuoteElement(linesScroller, total, quote, offers.get()));
        buildQuoteElement(linesScroller, total, quote, offers.get());

        String[] address = {be.getAddress()};
        TextField addressField = new TextField();
        addressField.textFieldStyle(
                style -> style.placeholder(Component.translatable("ui.createkontor.buyer_desk.quote.address")));
        addressField.style(
                style -> style.tooltips(Component.translatable("ui.createkontor.buyer_desk.quote.address.tooltip")));
        addressField.layout(layout -> layout.widthPercent(100));
        addressField.setText(address[0], false);
        addressField.setTextResponder(text -> address[0] = text.trim());
        addressField.addEventListener(UIEvents.BLUR, event -> {
            CompoundTag addressTag = new CompoundTag();
            addressTag.putString("Address", address[0]);
            content.sendMessage("c2s_address", addressTag);
        });
        content.addChild(addressField);

        Button buy = UiButtons.secondary(Component.translatable("ui.createkontor.buyer_desk.quote.buy"));
        buy.setActive(false);
        quote.addListener(() -> buy.setActive(!quote.get().isEmpty()));
        buy.layout(layout -> layout.widthPercent(100));
        buy.setOnClick(e -> buildQuote(quote.get(), offers.get())
                .ifPresent(current -> content.sendMessage("c2s_buy", quoteToTag(current, address[0]))));
        content.addChild(buy);

        return content;
    }

    public static UIElement autoStock(EmployeeDeskContext context, BuyerDeskBlockEntity be) {
        ObservableValue<Boolean> networkConnected = new ObservableValue<>(false);
        SimpleBinding<Boolean> networkConnectedBinding = DataBindingBuilder.boolS2C(
                        () -> context.company().logistics().isConnected())
                .onRemoteSyncReceived(networkConnected::set)
                .build();

        UIElement content =
                new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        content.addSyncValue(networkConnectedBinding.getSyncValue());

        UIElement notConnected = UiContainer.headerWithSubtitle(
                UiLabels.h2(
                        Component.translatable("ui.createkontor.logistics_manager_desk.no_network"), Horizontal.CENTER),
                UiLabels.error(
                        Component.translatable("ui.createkontor.buyer_desk.no_network")
                                .withStyle(ChatFormatting.DARK_RED),
                        Horizontal.CENTER));
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

        split.left(autoStockSettings(be));
        split.right(autoStockTargets(context, be));
        connected.addChild(split);

        networkConnected.runListenersOnce();

        return content;
    }

    public static UIElement autoStockSettings(BuyerDeskBlockEntity be) {
        ScrollerView content =
                (ScrollerView) UiContainer.tabScroller().style(UiStyles.PANEL).layout(layout -> layout.paddingAll(8));

        content.addScrollViewChildren(
                UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.auto_stock.settings"), Horizontal.LEFT));
        content.addScrollViewChildren(UiLabels.paragraphSecondary(
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.explanation"), Horizontal.LEFT));

        ObservableValue<Boolean> refillEnabled = new ObservableValue<>(false);
        SimpleBinding<Boolean> refillEnabledBinding = DataBindingBuilder.boolS2C(be::isRefillEnabled)
                .onRemoteSyncReceived(refillEnabled::set)
                .build();

        UiSwitches.LabeledSwitch refillEnabledSwitch = UiSwitches.labeled(
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.enabled"), be.isRefillEnabled(), isOn -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("Enable", isOn);
                    content.sendMessage("c2s_set_restock_enabled", tag);
                });
        refillEnabledSwitch.addSyncValue(refillEnabledBinding.getSyncValue());
        refillEnabled.addListener(() -> refillEnabledSwitch.getSwitch().setOn(refillEnabled.get(), false));
        content.onMessage("c2s_set_restock_enabled", (tag) -> be.setRefillEnabled(tag.getBoolean("Enable")));
        content.addScrollViewChildren(refillEnabledSwitch);

        UiTextFields.LabeledTextField address = UiTextFields.labeled(
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.refill_address"),
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.refill_address"),
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.refill_address.tooltip"),
                be.getRefillAddress(),
                text -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("RefillAddress", text);
                    content.sendMessage("c2s_set_refill_address", tag);
                });
        address.addSyncValue(DataBindingBuilder.stringS2C(be::getRefillAddress)
                .onRemoteSyncReceived(value -> address.getTextField().setText(value, false))
                .build()
                .getSyncValue());
        content.addScrollViewChildren(address);
        content.onMessage("c2s_set_refill_address", tag -> be.setRefillAddress(tag.getString("RefillAddress")));

        ObservableValue<Boolean> postboxConnected = new ObservableValue<>(false);
        SimpleBinding<Boolean> postboxConnectedBinding = DataBindingBuilder.boolS2C(
                        () -> !be.connectedPorts().isEmpty())
                .onRemoteSyncReceived(postboxConnected::set)
                .build();

        Label postboxMissingLabel = UiLabels.paragraphError(
                Component.translatable("ui.createkontor.buyer_desk.auto_stock.no_postbox"), Horizontal.LEFT);
        postboxMissingLabel.addSyncValue(postboxConnectedBinding.getSyncValue());
        postboxConnected.addListener(() -> postboxMissingLabel.setDisplay(!postboxConnected.get()));
        content.addScrollViewChildren(postboxMissingLabel);
        postboxConnected.runListenersOnce();

        return content;
    }

    public static UIElement autoStockTargets(EmployeeDeskContext context, BuyerDeskBlockEntity be) {
        UIElement content = new UIElement()
                .style(UiStyles.PANEL)
                .layout(layout -> layout.heightPercent(100).paddingAll(8));

        content.addChild(
                UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.auto_stock.targets"), Horizontal.LEFT));

        ObservableList<ItemId> purchasable = new ObservableList<>();
        SimpleBinding<Tag> purchasableBinding = DataBindingBuilder.tagS2C(() -> purchasableToTag(context))
                .onRemoteSyncReceived(tag -> purchasable.set(tagToItemIds(tag).stream()
                        .sorted(Comparator.comparing(
                                id -> BuiltInRegistries.ITEM
                                        .get(ResourceLocation.parse(id.value()))
                                        .getDescription()
                                        .getString(),
                                String.CASE_INSENSITIVE_ORDER))
                        .toList()))
                .build();
        content.addSyncValue(purchasableBinding.getSyncValue());

        SearchComponent<ItemId> search = new SearchComponent<>();
        search.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(ItemId id) {
                return BuiltInRegistries.ITEM
                        .get(ResourceLocation.parse(id.value()))
                        .getDescription()
                        .getString();
            }

            @Override
            public void onResultSelected(@Nullable ItemId value) {}

            @Override
            public void search(String word, IResultHandler<ItemId> searchHandler) {
                String needle = word.trim().toLowerCase(Locale.ROOT);
                for (ItemId id : purchasable.get()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    if (needle.isEmpty()
                            || BuiltInRegistries.ITEM
                                    .get(ResourceLocation.parse(id.value()))
                                    .getDescription()
                                    .getString()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(needle)) {
                        searchHandler.acceptResult(id);
                    }
                }
            }
        });

        search.setCandidateUIProvider(id -> new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4))
                .addChildren(
                        new ItemSlot()
                                .setItem(new ItemStack(
                                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())), 1)),
                        UiLabels.paragraphPrimary(
                                BuiltInRegistries.ITEM
                                        .get(ResourceLocation.parse(id.value()))
                                        .getDescription(),
                                Horizontal.LEFT)));
        search.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).height(22));
        search.searchStyle(style -> style.maxItemCount(8).scrollerViewHeight(148));

        Button add = UiButtons.secondary(Component.translatable("ui.createkontor.buyer_desk.auto_stock.add"));
        add.setActive(false);
        add.layout(layout -> layout.height(22));

        ItemId[] selected = {null};
        search.setOnValueChanged(id -> {
            selected[0] = id;
            add.setActive(id != null);
        });
        add.setOnClick(e -> {
            if (selected[0] == null) return;
            CompoundTag tag = new CompoundTag();
            tag.putString("Item", selected[0].value());
            content.sendMessage("c2s_add_refill_target", tag);
            search.setValue(null);
        });

        UIElement searchRow = new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100));
        searchRow.addChildren(search, add);
        content.addChild(searchRow);

        content.onMessage("c2s_add_refill_target", tag -> {
            String raw = tag.getString("Item");
            ResourceLocation location = raw.isBlank() ? null : ResourceLocation.tryParse(raw);
            Economy economy = context.economy();
            if (location == null || economy == null || !BuiltInRegistries.ITEM.containsKey(location)) return;
            ItemId id = new ItemId(raw);
            if (!economy.isMarket(id) && !economy.isRawMaterial(id)) return;

            Component name = BuiltInRegistries.ITEM
                    .get(ResourceLocation.parse(id.value()))
                    .getDescription();
            if (be.getRefillTargets().containsKey(id)) {
                ToastPayload.sendInfo(
                        context.player(),
                        Component.translatable("message.createkontor.buyer_desk.auto_stock.already_added", name));
            } else if (!be.setRefillTarget(id, 0)) {
                ToastPayload.sendError(
                        context.player(),
                        Component.translatable(
                                "message.createkontor.buyer_desk.auto_stock.limit_reached",
                                BuyerDeskBlockEntity.MAX_REFILL_ENTRIES));
            } else {
                ToastPayload.sendSuccess(
                        context.player(),
                        Component.translatable("message.createkontor.buyer_desk.auto_stock.added", name));
            }
        });

        ScrollerView refillTargets = (ScrollerView) UiContainer.tabScroller()
                .layout(layout -> layout.heightAuto()
                        .flexBasis(0)
                        .flexGrow(1)
                        .minHeight(0)
                        .marginTop(4));
        ObservableList<RefillTargetEntry> targets = new ObservableList<>();
        SimpleBinding<Tag> targetsBinding = DataBindingBuilder.tagS2C(
                        () -> refillTargetsToTag(context.company(), context.economy(), be))
                .onRemoteSyncReceived(tag -> targets.set(tagToRefillTargets(tag).stream()
                        .sorted(Comparator.comparing(
                                (RefillTargetEntry entry) -> BuiltInRegistries.ITEM
                                        .get(ResourceLocation.parse(entry.item().value()))
                                        .asItem()
                                        .getDescription()
                                        .getString(),
                                String.CASE_INSENSITIVE_ORDER))
                        .toList()))
                .build();
        refillTargets.addSyncValue(targetsBinding.getSyncValue());

        Map<ItemId, Consumer<RefillTargetEntry>> rowUpdaters = new HashMap<>();
        List<ItemId> shownOrder = new ArrayList<>();

        targets.addListener(() -> {
            List<ItemId> order =
                    targets.get().stream().map(RefillTargetEntry::item).toList();
            if (order.equals(shownOrder)) {
                for (RefillTargetEntry entry : targets.get()) {
                    rowUpdaters.get(entry.item()).accept(entry);
                }
                return;
            }
            shownOrder.clear();
            shownOrder.addAll(order);
            rowUpdaters.clear();
            refillTargets.clearAllScrollViewChildren();

            for (RefillTargetEntry entry : targets.get()) {
                UIElement element = new UIElement()
                        .style(UiStyles.CARD)
                        .layout(layout -> layout.paddingAll(8).gapAll(4));

                UIElement titleRow = new UIElement()
                        .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .justifyContent(AlignContent.FLEX_START)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .marginBottom(4)
                                .widthPercent(100));
                titleRow.addChild(new ItemSlot()
                        .setItem(new ItemStack(
                                BuiltInRegistries.ITEM.get(
                                        ResourceLocation.tryParse(entry.item().value())),
                                1)));

                Label itemLabel = UiLabels.paragraphPrimary(Component.empty(), Horizontal.LEFT);
                itemLabel.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
                titleRow.addChild(itemLabel);
                element.addChild(titleRow);

                UIElement valueRow = new UIElement()
                        .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .widthPercent(100));

                UIElement infoGroup = new UIElement()
                        .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .flexShrink(1)
                                .minWidth(0));

                Label stock = UiLabels.secondary(Component.literal("0"), Horizontal.RIGHT);
                stock.layout(layout -> layout.width(32));

                Label slash = UiLabels.secondary(Component.literal("/"), Horizontal.LEFT);

                int[] committed = {entry.minStock()};
                int[] current = {entry.minStock()};
                TextField minField = new TextField();
                minField.setNumbersOnlyInt(0, BuyerDeskBlockEntity.MAX_GOOD_SIZE);
                minField.setText(String.valueOf(entry.minStock()), false);
                minField.layout(layout -> layout.width(40));
                minField.setTextResponder(text -> {
                    try {
                        current[0] = Math.max(
                                0, Math.min(Integer.parseInt(text.trim()), BuyerDeskBlockEntity.MAX_GOOD_SIZE));
                    } catch (NumberFormatException e) {
                        current[0] = 0;
                    }
                });
                minField.addEventListener(UIEvents.BLUR, event -> {
                    if (current[0] != committed[0]) {
                        committed[0] = current[0];
                        CompoundTag tag = new CompoundTag();
                        tag.putString("Item", entry.item().value());
                        tag.putInt("MinStock", current[0]);
                        content.sendMessage("c2s_set_refill_min", tag);
                    }
                });

                Label promised = UiLabels.secondary(Component.empty(), Horizontal.LEFT);

                infoGroup.addChildren(stock, slash, minField, promised);

                Button reset = UiButtons.critical(
                        Component.translatable("ui.createkontor.buyer_desk.auto_stock.reset_promised"));
                reset.layout(layout -> layout.height(14));
                reset.setOnClick(e -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Item", entry.item().value());
                    content.sendMessage("c2s_reset_promises", tag);
                });

                Button remove =
                        UiButtons.critical(Component.translatable("ui.createkontor.buyer_desk.auto_stock.remove"));
                remove.layout(layout -> layout.height(14));
                remove.setOnClick(e -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Item", entry.item().value());
                    content.sendMessage("c2s_remove_refill_target", tag);
                });

                UIElement buttonGroup = new UIElement()
                        .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .justifyContent(AlignContent.FLEX_END)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .widthAuto()
                                .flexGrow(1)
                                .flexShrink(1)
                                .minWidth(0));
                buttonGroup.addChildren(reset, remove);

                valueRow.addChildren(infoGroup, buttonGroup);
                element.addChild(valueRow);

                Consumer<RefillTargetEntry> update = e -> {
                    int color;
                    Component tooltip;
                    if (e.minStock() == 0) {
                        color = 0xAAAAAA;
                        tooltip = Component.translatable("ui.createkontor.buyer_desk.auto_stock.status.disabled");
                    } else if (e.currentStock() >= e.minStock()) {
                        color = 0x55FF55;
                        tooltip = Component.translatable("ui.createkontor.buyer_desk.auto_stock.status.enough");
                    } else if (e.currentStock() + e.promised() >= e.minStock()) {
                        color = 0xFFFF55;
                        tooltip =
                                Component.translatable("ui.createkontor.buyer_desk.auto_stock.status.enough_promised");
                    } else {
                        color = 0xFF5555;
                        tooltip = Component.translatable("ui.createkontor.buyer_desk.auto_stock.status.not_enough");
                    }
                    final int finalColor = color;
                    final Component finalTooltip = tooltip;

                    itemLabel.setText(Component.empty()
                            .append(BuiltInRegistries.ITEM
                                    .get(ResourceLocation.tryParse(e.item().value()))
                                    .getDescription())
                            .append(Component.translatable(
                                    "ui.createkontor.buyer_desk.auto_stock.unit_price",
                                    ComponentFormatting.moneyColored(e.unitPrice()))));
                    itemLabel.textStyle(
                            style -> style.textAlignVertical(Vertical.CENTER).textColor(finalColor));
                    itemLabel.style(style -> style.tooltips(finalTooltip));

                    stock.setText(Component.literal(String.valueOf(e.currentStock())));
                    promised.setText(
                            Component.translatable("ui.createkontor.buyer_desk.auto_stock.promised", e.promised()));

                    if (e.minStock() != committed[0]) {
                        committed[0] = e.minStock();
                        current[0] = e.minStock();
                        minField.setText(String.valueOf(e.minStock()), false);
                    }
                };
                update.accept(entry);
                rowUpdaters.put(entry.item(), update);

                refillTargets.addScrollViewChildren(element);
            }
        });
        content.onMessage("c2s_set_refill_min", tag -> {
            String item = tag.getString("Item");
            if (!item.isBlank()) {
                be.setRefillTarget(new ItemId(item), tag.getInt("MinStock"));
            }
        });
        content.onMessage("c2s_remove_refill_target", tag -> be.removeRefillTarget(new ItemId(tag.getString("Item"))));
        content.onMessage("c2s_reset_promises", tag -> {
            if (context.company().logistics().networkId() != null) {
                LogisticsNetworkAccess.clearPromises(
                        context.company().logistics().networkId(), new ItemId(tag.getString("Item")));
            }
        });
        targets.runListenersOnce();

        content.addChild(refillTargets);

        return content;
    }

    private static Tag offersToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        if (context.economy() == null) {
            return list;
        }
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (PurchaseOffer offer : PurchaseOffers.list(context.economy(), params, false)) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Item", offer.item().value());
            entry.putString("Kind", offer.kind().name());
            entry.putDouble("MarketPrice", offer.marketPrice());
            entry.putLong("UnitPriceCents", offer.unitPrice().cents());
            list.add(entry);
        }
        return list;
    }

    private static List<PurchaseOffer> tagToOffers(Tag tag) {
        List<PurchaseOffer> offers = new ArrayList<>();
        if (!(tag instanceof ListTag list)) {
            return offers;
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            offers.add(new PurchaseOffer(
                    new ItemId(entry.getString("Item")),
                    PurchaseKind.valueOf(entry.getString("Kind")),
                    entry.getDouble("MarketPrice"),
                    Money.ofCents(entry.getLong("UnitPriceCents"))));
        }
        return offers;
    }

    private static CompoundTag quoteToTag(PurchaseQuote quote, String address) {
        ListTag list = new ListTag();
        for (PurchaseQuote.Line line : quote.lines()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Item", line.item().value());
            entry.putInt("Quantity", line.quantity());
            list.add(entry);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Lines", list);
        tag.putLong("TotalCents", quote.total().cents());
        tag.putString("Address", address);
        return tag;
    }

    private static Map<ItemId, Integer> tagToCart(CompoundTag tag) {
        Map<ItemId, Integer> cart = new LinkedHashMap<>();
        ListTag list = tag.getList("Lines", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            cart.merge(new ItemId(entry.getString("Item")), entry.getInt("Quantity"), Integer::sum);
        }
        return cart;
    }

    private static Tag purchasableToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        Economy economy = context.economy();
        if (economy == null) {
            return list;
        }
        Set<ItemId> ids = new LinkedHashSet<>(economy.marketIds());
        ids.addAll(economy.rawMaterialIds());
        for (ItemId id : ids) {
            list.add(StringTag.valueOf(id.value()));
        }
        return list;
    }

    private static List<ItemId> tagToItemIds(Tag tag) {
        List<ItemId> ids = new ArrayList<>();
        if (!(tag instanceof ListTag list)) {
            return ids;
        }
        for (int i = 0; i < list.size(); i++) {
            String raw = list.getString(i);
            if (!raw.isBlank()) {
                ids.add(new ItemId(raw));
            }
        }
        return ids;
    }

    private static Tag refillTargetsToTag(Company company, Economy economy, BuyerDeskBlockEntity be) {
        ListTag list = new ListTag();
        if (company.logistics() == null || !company.logistics().isConnected()) {
            return list;
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Map<ItemId, Integer> stock = LogisticsNetworkAccess.stockOf(
                company.logistics().networkId(), be.getRefillTargets().keySet());
        for (Map.Entry<ItemId, Integer> target : be.getRefillTargets().entrySet()) {
            if (!economy.isMarket(target.getKey()) && !economy.isRawMaterial(target.getKey())) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("Item", target.getKey().value());
            entry.putInt("MinStock", target.getValue());
            entry.putInt("CurrentStock", stock.getOrDefault(target.getKey(), 0));
            entry.putInt(
                    "Promised",
                    LogisticsNetworkAccess.promisedOf(company.logistics().networkId(), target.getKey()));
            entry.putLong(
                    "UnitPriceCents",
                    PurchaseOffers.offerFor(economy, params, target.getKey(), true)
                            .unitPrice()
                            .cents());
            list.add(entry);
        }
        return list;
    }

    public record RefillTargetEntry(ItemId item, int minStock, int currentStock, int promised, Money unitPrice) {}

    private static List<RefillTargetEntry> tagToRefillTargets(Tag tag) {
        List<RefillTargetEntry> targets = new ArrayList<>();
        if (!(tag instanceof ListTag list)) {
            return targets;
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String raw = entry.getString("Item");
            if (raw.isBlank()) {
                continue;
            }
            targets.add(new RefillTargetEntry(
                    new ItemId(raw),
                    entry.getInt("MinStock"),
                    entry.getInt("CurrentStock"),
                    entry.getInt("Promised"),
                    Money.ofCents(entry.getLong("UnitPriceCents"))));
        }
        return targets;
    }

    private static void addToQuote(ObservableValue<Map<ItemId, Integer>> quote, ItemId item, int quantity) {
        Map<ItemId, Integer> next = new LinkedHashMap<>(quote.get());
        next.merge(item, quantity, Integer::sum);
        quote.set(next);
    }

    private static Optional<PurchaseQuote> buildQuote(Map<ItemId, Integer> cart, List<PurchaseOffer> offers) {
        Map<ItemId, PurchaseOffer> byItem = new HashMap<>();
        for (PurchaseOffer offer : offers) {
            byItem.put(offer.item(), offer);
        }
        List<PurchaseQuote.Line> lines = new ArrayList<>();
        for (Map.Entry<ItemId, Integer> entry : cart.entrySet()) {
            PurchaseOffer offer = byItem.get(entry.getKey());
            if (offer != null) {
                lines.add(offer.lineFor(entry.getValue()));
            }
        }
        return lines.isEmpty() ? Optional.empty() : Optional.of(new PurchaseQuote(lines));
    }

    private static void buildQuoteElement(
            ScrollerView parent, Label total, ObservableValue<Map<ItemId, Integer>> quote, List<PurchaseOffer> offers) {
        parent.clearAllScrollViewChildren();
        Optional<PurchaseQuote> purchaseQuote = buildQuote(quote.get(), offers);
        if (purchaseQuote.isEmpty()) {
            parent.addScrollViewChildren(UiLabels.paragraphSecondary(
                    Component.translatable("ui.createkontor.buyer_desk.quote.empty"), Horizontal.LEFT));
            total.setText(Component.empty());
            return;
        }

        for (PurchaseQuote.Line line : purchaseQuote.get().lines()) {
            Item item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.parse(line.item().value()));
            UIElement element = new UIElement();

            element.addChild(UiLabels.labelValueNoWrap(
                    Component.translatable(
                            "ui.createkontor.buyer_desk.quote.line",
                            Component.literal(String.valueOf(line.quantity())).withStyle(ChatFormatting.GRAY),
                            item.getDescription()),
                    ComponentFormatting.moneyColored(line.lineTotal())));
            element.addChild(UiButtons.critical(Component.translatable("ui.createkontor.buyer_desk.quote.remove"))
                    .setOnClick((e) -> {
                        Map<ItemId, Integer> next = new LinkedHashMap<>(quote.get());
                        next.remove(line.item());
                        quote.set(next);
                    })
                    .layout(layout -> layout.height(14)));

            parent.addScrollViewChildren(element);
        }
        total.setText(Component.translatable(
                "ui.createkontor.buyer_desk.quote.total",
                ComponentFormatting.moneyColored(purchaseQuote.get().total())));
    }
}
