package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.license.AcquireResult;
import com.timder.kontor.core.company.license.LicenseKeyCodec;
import com.timder.kontor.core.company.purchase.PurchaseKind;
import com.timder.kontor.core.company.purchase.PurchaseOffer;
import com.timder.kontor.core.company.purchase.PurchaseOffers;
import com.timder.kontor.core.company.purchase.PurchaseQuote;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.buyer.BuyerDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.LicenseNames;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;


public final class BuyerUi {
    public static ModularUI create(EmployeeDeskContext context) {
        BuyerDeskBlockEntity be = (BuyerDeskBlockEntity) context.desk();

        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.buyer_desk.catalog")), catalogTab(context, be));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement catalogTab(EmployeeDeskContext context, BuyerDeskBlockEntity be) {
        ObservableValue<Map<ItemId, Integer>> quote = new ObservableValue<>(Map.of());
        ObservableList<PurchaseOffer> offers = new ObservableList<>();
        SimpleBinding<Tag> offersBinding = DataBindingBuilder.tagS2C(() -> offersToTag(context))
                .onRemoteSyncReceived(tag -> offers.set(tagToOffers(tag)
                        .stream()
                        .sorted(Comparator.comparing(offer -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(offer.item().value())).asItem().getDescription().getString(), String.CASE_INSENSITIVE_ORDER))
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

    public static UIElement offers(ObservableList<PurchaseOffer> offers, SimpleBinding<Tag> offersBinding, ObservableValue<Map<ItemId, Integer>> quote) {
        UIElement content = new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100)).style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8));
        content.addSyncValue(offersBinding.getSyncValue());

        Label title = (Label) UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.catalog"), Horizontal.LEFT).textStyle(style -> style.adaptiveWidth(false));
        offers.addListener(() -> title.setText(Component.translatable("ui.createkontor.buyer_desk.catalog_count", offers.get().size())));
        content.addChild(title);

        content.addChild(UiContainer.searchableGrid(
                offers,
                (PurchaseOffer offer) -> {
                    UIElement element = new UIElement()
                            .style(style -> style.background(Sprites.BORDER_DARK))
                            .layout(layout -> layout.paddingAll(8));

                    if (offer.kind() == PurchaseKind.RAW_MATERIAL) {
                        element.addChild(UiLabels.paragraphTertiary(Component.translatable("ui.createkontor.buyer_desk.catalog.raw_material"), Horizontal.LEFT));
                    } else {
                        element.addChild(UiLabels.paragraphTertiary(Component.translatable("ui.createkontor.buyer_desk.catalog.product"), Horizontal.LEFT));
                    }

                    UIElement titleRow = new UIElement()
                            .layout(layout -> layout
                                    .flexDirection(FlexDirection.ROW)
                                    .flexWrap(FlexWrap.WRAP)
                                    .justifyContent(AlignContent.FLEX_START)
                                    .alignItems(AlignItems.CENTER)
                                    .gapAll(4)
                                    .marginBottom(4)
                                    .widthPercent(100));
                    titleRow.addChild(new ItemSlot().setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(offer.item().value())), 1)));
                    titleRow.addChild(UiLabels.paragraphPrimary(Component.empty().append(BuiltInRegistries.ITEM.get(ResourceLocation.parse(offer.item().value())).asItem().getDescription()).withStyle(ChatFormatting.GOLD), Horizontal.LEFT)
                            .textStyle(style -> style.textAlignVertical(Vertical.CENTER))
                            .layout(layout -> layout
                                    .widthAuto()
                                    .flexBasis(0)
                                    .flexGrow(1)));
                    element.addChild(titleRow);

                    element.addChild(UiLabels.paragraphPrimary(Component.translatable(
                            "ui.createkontor.buyer_desk.catalog.price",
                            ComponentFormatting.moneyColored(offer.unitPrice())
                    ), Horizontal.LEFT));

                    int[] quantity = {1};
                    UIElement buyRow = new UIElement()
                            .layout(layout -> layout
                                    .flexDirection(FlexDirection.ROW)
                                    .alignItems(AlignItems.CENTER)
                                    .gapAll(4)
                                    .widthPercent(100)
                                    .marginTopAuto());

                    TextField quantityField = new TextField();
                    quantityField.setNumbersOnlyInt(1, BuyerDeskBlockEntity.MAX_GOOD_SIZE);
                    quantityField.setText("1", false);
                    quantityField.setTextResponder(text -> {
                        try {
                            quantity[0] = Math.max(1, Math.min(Integer.parseInt(text.trim()), BuyerDeskBlockEntity.MAX_GOOD_SIZE));
                        } catch (NumberFormatException e) {
                            quantity[0] = 1;
                        }
                    });
                    quantityField.addEventListener(UIEvents.BLUR, event -> quantityField.setText(String.valueOf(quantity[0]), false));
                    quantityField.layout(layout -> layout.width(40));

                    Button add = (Button) UiButtons.primary(Component.translatable("ui.createkontor.buyer_desk.catalog.add"))
                            .setOnClick(e -> addToCart(quote, offer.item(), Math.max(1, Math.min(quantity[0], BuyerDeskBlockEntity.MAX_GOOD_SIZE))))
                            .layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).height(14));

                    buyRow.addChildren(quantityField, add);
                    element.addChild(buyRow);

                    return element;
                },
                (PurchaseOffer offer) -> List.of(
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(offer.item().value())).asItem().getDescription().getString()
                ),
                Component.translatable("ui.createkontor.buyer_desk.catalog.search"),
                Component.translatable("ui.createkontor.buyer_desk.catalog.no_result"),
                4,
                128));

        return content;
    }

    public static UIElement quote(EmployeeDeskContext context, BuyerDeskBlockEntity be, ObservableList<PurchaseOffer> offers, SimpleBinding<Tag> offersBinding, ObservableValue<Map<ItemId, Integer>> quote) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).heightPercent(100).paddingAll(8).gapAll(4))
                .style(style -> style.background(Sprites.BORDER_DARK));

        int[] purchases = {0}; // Server sided
        int[] seen = {0}; // Client sided

        content.onMessage("c2s_buy", tag -> {
            Optional<S2CActionResult> result = be.buy(tagToCart(tag), tag.getLong("TotalCents"), tag.getString("Address"));
            ToastPayload.send(context.player(), result);
            if (result.isPresent() && result.get().kind() == ToastPayload.ToastKind.SUCCESS) {
                purchases[0]++;
            }
        });

        content.addSyncValue( // Clears quote on successful purchase
                DataBindingBuilder.tagS2C(() -> IntTag.valueOf(purchases[0]))
                        .onRemoteSyncReceived(tag -> {
                            int count = tag instanceof IntTag number ? number.getAsInt() : 0;
                            if (count != seen[0]) {
                                seen[0] = count;
                                quote.set(Map.of());
                            }
                        })
                        .build().getSyncValue()
        );

        Label title = (Label) UiLabels.h2(Component.translatable("ui.createkontor.buyer_desk.quote"), Horizontal.LEFT).textStyle(style -> style.adaptiveWidth(false));
        content.addChild(title);
        content.addSyncValue(offersBinding.getSyncValue());

        ScrollerView linesScroller = UiContainer.tabScroller();
        linesScroller.layout(layout -> layout.heightAuto().flexBasis(0).flexGrow(1).minHeight(0));
        linesScroller.viewContainer.layout(layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(10));

        float[] appliedWidth = {0};
        linesScroller.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
            float available = linesScroller.getContentWidth() - 10;
            if (available <= 0 || Math.abs(available - appliedWidth[0]) < 0.01f) return;
            appliedWidth[0] = available;
            linesScroller.viewContainer.layout(layout -> layout.width(available));
        });

        UIElement linesBox = new UIElement()
                .layout(layout -> layout.widthPercent(100).flexBasis(0).flexGrow(1).minHeight(0))
                .addChildren(linesScroller);
        content.addChild(linesBox);

        Label total = UiLabels.paragraphPrimary(Component.empty(), Horizontal.LEFT);
        content.addChild(total);

        quote.addListener(() -> buildQuoteElement(linesScroller, total, quote, offers.get()));
        offers.addListener(() -> buildQuoteElement(linesScroller, total, quote, offers.get()));
        buildQuoteElement(linesScroller, total, quote, offers.get());

        String[] address = {""};
        TextField addressField = new TextField();
        addressField.textFieldStyle(style -> style.placeholder(Component.translatable("ui.createkontor.buyer_desk.quote.address")));
        addressField.style(style -> style.tooltips(Component.translatable("ui.createkontor.buyer_desk.quote.address.tooltip")));
        addressField.layout(layout -> layout.widthPercent(100));
        addressField.setTextResponder(text -> address[0] = text.trim());
        content.addChild(addressField);

        Button buy = UiButtons.primary(Component.translatable("ui.createkontor.buyer_desk.quote.buy"));
        buy.setActive(false);
        quote.addListener(() -> buy.setActive(!quote.get().isEmpty()));
        buy.layout(layout -> layout.widthPercent(100));
        buy.setOnClick(e -> buildQuote(quote.get(), offers.get()).ifPresent(current -> content.sendMessage("c2s_buy", quoteToTag(current, address[0]))));
        content.addChild(buy);

        return content;
    }

    private static Tag offersToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        if (context.economy() == null) {
            return list;
        }
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (PurchaseOffer offer : PurchaseOffers.list(context.economy(), params)) {
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

    private static void addToCart(ObservableValue<Map<ItemId, Integer>> cart, ItemId item, int quantity) {
        Map<ItemId, Integer> next = new LinkedHashMap<>(cart.get());
        next.merge(item, quantity, Integer::sum);
        cart.set(next);
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

    private static void buildQuoteElement(ScrollerView parent, Label total, ObservableValue<Map<ItemId, Integer>> quote, List<PurchaseOffer> offers) {
        parent.clearAllScrollViewChildren();
        Optional<PurchaseQuote> purchaseQuote = buildQuote(quote.get(), offers);
        if (purchaseQuote.isEmpty()) {
            parent.addScrollViewChildren(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.buyer_desk.quote.empty"), Horizontal.LEFT));
            total.setText(Component.empty());
            return;
        }

        for (PurchaseQuote.Line line : purchaseQuote.get().lines()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(line.item().value()));
            UIElement element = new UIElement();

            element.addChild(UiLabels.labelValueNoWrap(
                    Component.translatable("ui.createkontor.buyer_desk.quote.line", Component.literal(String.valueOf(line.quantity())).withStyle(ChatFormatting.GRAY), item.getDescription()),
                    ComponentFormatting.moneyColored(line.lineTotal())
            ));
            element.addChild(
                    UiButtons.critical(Component.translatable("ui.createkontor.buyer_desk.quote.remove"))
                            .setOnClick((e) -> {
                                Map<ItemId, Integer> next = new LinkedHashMap<>(quote.get());
                                next.remove(line.item());
                                quote.set(next);
                            })
                            .layout(layout -> layout.height(14))
            );

            parent.addScrollViewChildren(element);
        }
        total.setText(Component.translatable("ui.createkontor.buyer_desk.quote.total", ComponentFormatting.moneyColored(purchaseQuote.get().total())));
    }
}
