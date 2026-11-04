package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderBook;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.network.NotebookPayload;
import com.timder.kontor.game.ui.element.UiPaper;
import com.timder.kontor.game.ui.element.UiStyles;
import com.timder.kontor.util.ComponentFormatting;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public final class NotebookUi {

    private static final int ORDERS_FIRST_PAGE = 3;
    private static final int ORDERS_PER_PAGE = 5;

    public static ModularUI create(NotebookPayload payload) {
        List<Order> orders = OrderBook.restore(CompanySavedData.readOrderBook(payload.orderBook())).allOrders().stream()
                .sorted(Comparator.comparingLong(Order::remainingDeadlineTicks)
                        .thenComparingLong(Order::remainingGracePeriodTicks)
                        .thenComparingLong(Order::getNumber))
                .toList();

        int pageCount = pageCount(orders.size());
        int[] page = {0};

        UIElement root = new UIElement()
                .layout(layout -> layout.width(200)
                        .height(237)
                        .paddingLeft(28)
                        .paddingTop(13)
                        .paddingRight(13)
                        .paddingBottom(13)
                        .gapAll(4))
                .style(UiStyles.NOTEBOOK);

        UIElement content = new UIElement()
                .layout(layout ->
                        layout.flexDirection(FlexDirection.COLUMN).flexGrow(1).gapAll(4));

        Button prev = UiPaper.button(Component.literal("<"));
        prev.layout(layout -> layout.width(24));
        Button next = UiPaper.button(Component.literal(">"));
        next.layout(layout -> layout.width(24));
        Label pageLabel = UiPaper.secondary(Component.empty(), Horizontal.CENTER);
        pageLabel.layout(layout -> layout.flexGrow(1));

        if (pageCount <= 1) {
            prev.setDisplay(false);
            next.setDisplay(false);
        }

        prev.setOnClick(e -> {
            page[0]--;
            showPage(content, pageLabel, prev, next, payload, orders, page[0], pageCount);
            UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
        });
        next.setOnClick(e -> {
            page[0]++;
            showPage(content, pageLabel, prev, next, payload, orders, page[0], pageCount);
            UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
        });
        root.addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            if (event.deltaY > 0 && page[0] > 0) {
                page[0]--;
                showPage(content, pageLabel, prev, next, payload, orders, page[0], pageCount);
                UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
            } else if (event.deltaY < 0 && page[0] < pageCount - 1) {
                page[0]++;
                showPage(content, pageLabel, prev, next, payload, orders, page[0], pageCount);
                UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
            }
            event.stopPropagation();
            event.stopPropagation();
        });

        showPage(content, pageLabel, prev, next, payload, orders, page[0], pageCount);

        UIElement navigation = new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4));
        navigation.addChildren(prev, pageLabel, next);

        root.addChildren(content, navigation);
        return ModularUI.of(UiStyles.ui(root));
    }

    private static void showPage(
            UIElement content,
            Label pageLabel,
            Button prev,
            Button next,
            NotebookPayload payload,
            List<Order> orders,
            int page,
            int pageCount) {
        content.clearAllChildren();

        if (page == 0) {
            content.addChild(UiPaper.heading(Component.literal(payload.companyName()), Horizontal.CENTER));
            content.addChild(UiPaper.primary(
                    Component.translatable(
                            "ui.createkontor.notebook.day_balance",
                            payload.day(),
                            ComponentFormatting.moneyColored(Money.ofCents(payload.balanceCents()))),
                    Horizontal.CENTER));
            content.addChild(UiPaper.primary(
                    Component.translatable("ui.createkontor.notebook.requests", payload.requestsAmount()),
                    Horizontal.CENTER));
            content.addChild(UiPaper.doubleDivider());
            content.addChild(UiPaper.primary(
                    Component.translatable("ui.createkontor.notebook.open_orders", orders.size()), Horizontal.LEFT));
        }

        List<Order> ordersOnPage = ordersOnPage(orders, page);
        for (int i = 0; i < ordersOnPage.size(); i++) {
            Order order = ordersOnPage.get(i);
            UIElement titleRow = new UIElement()
                    .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                            .justifyContent(AlignContent.FLEX_START)
                            .alignItems(AlignItems.CENTER)
                            .widthPercent(100)
                            .gapAll(4));

            ItemStack stack = BuiltInRegistries.ITEM
                    .get(ResourceLocation.tryParse(order.getProduct().value()))
                    .asItem()
                    .getDefaultInstance();
            stack.setCount(order.getQuantity());
            titleRow.addChild(UiPaper.itemSlot().setItem(stack));

            titleRow.addChild(UiPaper.paragraphPrimary(
                            Component.empty()
                                    .append(Component.literal(order.getPhase() == OrderPhase.GRACE_PERIOD ? "⚠ " : "")
                                            .withStyle(
                                                    order.getPhase() == OrderPhase.GRACE_PERIOD
                                                            ? ChatFormatting.RED
                                                            : ChatFormatting.RESET))
                                    .append(ComponentFormatting.quantityWithStacks(
                                            order.getQuantity(), stack.getMaxStackSize()))
                                    .append(Component.literal(" "
                                            + Component.translatable(stack.getDescriptionId())
                                                    .getString()))
                                    .append(Component.literal(" (#" + order.getNumber() + ")")
                                            .withStyle(ChatFormatting.DARK_GRAY)),
                            Horizontal.LEFT)
                    .layout(layout ->
                            layout.flexBasis(0).flexGrow(1).flexShrink(1).minWidth(0)));

            content.addChild(titleRow);

            if (i < ordersOnPage.size() - 1) {
                content.addChild(UiPaper.simpleDivider());
            }
        }

        pageLabel.setText(Component.literal((page + 1) + " / " + pageCount));
        prev.setActive(page > 0);
        next.setActive(page < pageCount - 1);
    }

    private static int pageCount(int orderCount) {
        return 1 + (Math.max(0, orderCount - ORDERS_FIRST_PAGE) + ORDERS_PER_PAGE - 1) / ORDERS_PER_PAGE;
    }

    private static List<Order> ordersOnPage(List<Order> orders, int page) {
        int from = page == 0 ? 0 : ORDERS_FIRST_PAGE + (page - 1) * ORDERS_PER_PAGE;
        int size = page == 0 ? ORDERS_FIRST_PAGE : ORDERS_PER_PAGE;
        return orders.subList(from, Math.min(orders.size(), from + size));
    }
}
