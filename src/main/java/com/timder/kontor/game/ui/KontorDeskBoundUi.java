package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
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
import com.timder.kontor.core.company.financial.Booking;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Loan;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.core.company.order.RequestOrigin;
import com.timder.kontor.core.company.request.Request;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.ui.chart.ChartSpec;
import com.timder.kontor.game.ui.chart.CompanyCharts;
import com.timder.kontor.client.ui.element.ChartElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.KontorDeskBlockEntity;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KontorDeskBoundUi {
    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity be, Company company, Economy economy) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.overview")), overviewTab(company, economy));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.account")), accountTab(company));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.requests_orders")), requestsOrdersTab(company, be));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(company, economy));

        return new ModularUI(UI.of(root, StylesheetManager.GDP), holder.player);
    }

    public static UIElement overviewTab(Company company, Economy economy) {
        ScrollerView overviewContent = UiContainer.tabScroller();

        Label title = (Label) UiLabels.h1(Component.empty(), Horizontal.CENTER).layout(layout -> layout.alignSelf(AlignItems.CENTER));
        title.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.literal(company.name()))
                    .onRemoteSyncReceived(title::setText)
                    .build().getSyncValue()
        );

        Label currentDay = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        currentDay.addSyncValue(
                DataBindingBuilder.componentS2C(() -> ComponentFormatting.day(economy.currentDay()))
                        .onRemoteSyncReceived(currentDay::setText)
                        .build().getSyncValue()
        );

        Label balance = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        balance.addSyncValue(
                DataBindingBuilder.componentS2C(() -> ComponentFormatting.moneyColored(company.account().getBalance()))
                    .onRemoteSyncReceived(balance::setText)
                    .build().getSyncValue()
        );

        Label liquidity = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        liquidity.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.translatable("enum.createkontor.liquidity." + company.liquidity().toString().toLowerCase()))
                    .onRemoteSyncReceived(liquidity::setText)
                    .build().getSyncValue()
        );

        Label openOrders = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        openOrders.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.translatable("ui.createkontor.kontor_desk.open_orders", company.orderBook().openOrders()))
                    .onRemoteSyncReceived(openOrders::setText)
                    .build().getSyncValue()
        );

        Label legalForm = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        legalForm.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.translatable("legal_forms.createkontor." + company.legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).toString()))
                    .onRemoteSyncReceived(legalForm::setText)
                    .build().getSyncValue()
        );

        UIElement headerSubtitle = UiLabels.seperatedLabelRow(List.of(
                currentDay,
                balance,
                liquidity,
                openOrders,
                legalForm
        ));

        overviewContent.addScrollViewChildren(UiContainer.headerWithSubtitle(title, headerSubtitle));

        UIElement dayResultChartBox = new UIElement();
        dayResultChartBox.layout(layout -> layout.widthPercent(100).height(200));
        dayResultChartBox.addSyncValue(
                DataBindingBuilder.tagS2C(() -> CompanyCharts.historyToTag(company.history()))
                        .onRemoteSyncReceived(tag -> {
                            ChartElement newChart = ChartElement.from(CompanyCharts.revenueResultSpecFromHistoryTag(tag));
                            newChart.layout(layout -> layout.widthPercent(100).flex(1));
                            dayResultChartBox.clearAllChildren();
                            dayResultChartBox.addChild(newChart);
                        })
                        .build()
                        .getSyncValue()
        );
        overviewContent.addScrollViewChildren(dayResultChartBox);

        return overviewContent;
    }

    public static UIElement accountTab(Company company) {
        UIElement content = new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .flexDirection(FlexDirection.COLUMN)
                        .gapAll(4));

        // Binding booking history
        ObservableList<Booking> bookingHistory = new ObservableList<>();
        SimpleBinding<Tag> bookingsBinding = DataBindingBuilder.tagS2C(() -> bookingsToTag(company.account().getBookings()))
                .onRemoteSyncReceived(tag -> bookingHistory.set(tagToBookings(tag)))
                .build();

        // Binding overdraft limit
        ObservableValue<Long> overdraftLimitCents = new ObservableValue<>(0L);
        SimpleBinding<Long> overdraftLimitBinding = DataBindingBuilder.longValS2C(() -> company.overdraftLimit(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).cents())
                .onRemoteSyncReceived(overdraftLimitCents::set)
                .build();

        Label balance = UiLabels.h1(Component.empty(), Horizontal.CENTER);
        balance.addSyncValue(DataBindingBuilder.componentS2C(() -> ComponentFormatting.moneyColored(company.account().getBalance()))
                .onRemoteSyncReceived(balance::setText)
                .build().getSyncValue()
        );

        Label liquidity = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        liquidity.addSyncValue(DataBindingBuilder.componentS2C(() -> Component.translatable("enum.createkontor.liquidity." + company.liquidity().toString().toLowerCase()))
                .onRemoteSyncReceived(liquidity::setText)
                .build().getSyncValue()
        );

        Label overdraftLimitLabel = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        overdraftLimitCents.addListener(() -> overdraftLimitLabel.setText(Component.translatable("ui.createkontor.kontor_desk.current_overdraft_limit", Money.ofCents(overdraftLimitCents.get()).toString())));
        overdraftLimitLabel.addSyncValue(overdraftLimitBinding.getSyncValue());

        content.addChild(UiContainer.headerWithSubtitle(
                balance,
                UiLabels.seperatedLabelRow(List.of(liquidity, overdraftLimitLabel))
        ));

        TabView tabView = (TabView) UiContainer.largeTabView().layout(layout -> layout
                .widthPercent(100)
                .flexGrow(1)
                .flexBasis(0));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.overview")), accountTabOverview(bookingHistory, bookingsBinding, overdraftLimitCents, overdraftLimitBinding));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.bookings")), accountTabBookings(bookingHistory, bookingsBinding));
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.kontor_desk.tab.open_loans")), accountTabLoans(company));
        content.addChild(tabView);

        return content;
    }

    public static UIElement accountTabOverview(ObservableList<Booking> bookings, SimpleBinding<Tag> bookingsBinding, ObservableValue<Long> overdraftLimit, SimpleBinding<Long> overdraftLimitBinding) {
        UIElement content = new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .flexDirection(FlexDirection.COLUMN)
                        .gapAll(4));

        UIElement balanceChartBox = new UIElement();
        balanceChartBox.layout(layout -> layout.widthPercent(100).heightPercent(100));

        bookings.addListener(() -> buildBalanceChart(bookings.get(), overdraftLimit.get(), balanceChartBox));
        overdraftLimit.addListener(() -> buildBalanceChart(bookings.get(), overdraftLimit.get(), balanceChartBox));

        balanceChartBox.addSyncValue(bookingsBinding.getSyncValue());
        balanceChartBox.addSyncValue(overdraftLimitBinding.getSyncValue());

        content.addChild(balanceChartBox);
        return content;
    }

    public static UIElement accountTabBookings(ObservableList<Booking> bookings, SimpleBinding<Tag> bookingsBinding) {
        ScrollerView content = UiContainer.tabScroller();

        Label title = UiLabels.h2(Component.translatable("ui.createkontor.kontor_desk.tab.bookings"), Horizontal.LEFT);
        bookings.addListener(() -> title.setText(Component.translatable("ui.createkontor.kontor_desk.bookings_count", bookings.get().size())));
        title.addSyncValue(bookingsBinding.getSyncValue());
        content.addScrollViewChildren(title);

        UIElement history = new UIElement().addSyncValue(bookingsBinding.getSyncValue());
        bookings.addListener(() -> {
            history.clearAllChildren();
            List<Booking> reversedBookings = bookings.get().reversed();
            long lastDay = 0;
            boolean first = true;
            for (Booking booking : reversedBookings) {
                if (booking.day() != lastDay) {
                    Component combined = Component.empty()
                            .append(ComponentFormatting.day(booking.day()))
                            .append(ComponentFormatting.standard(" ("))
                            .append(ComponentFormatting.moneyColored(booking.balanceAfter()))
                            .append(ComponentFormatting.standard(")"));
                    Label dayLabel = UiLabels.secondary(combined, Horizontal.LEFT);
                    if (!first) {
                        dayLabel.layout(layout -> layout.marginTop(8));
                    }
                    history.addChild(dayLabel);
                    first = false;
                    lastDay = booking.day();
                }

                List<Label> labels = new ArrayList<>(List.of(
                        (Label) UiLabels.primary(ComponentFormatting.moneyColored(booking.amount()), Horizontal.LEFT)
                                .textStyle(style -> style.adaptiveWidth(false))
                                .layout(layout -> layout.width(70)),
                        UiLabels.secondary(Component.translatable("enum.createkontor.booking_kind." + booking.kind().toString().toLowerCase()), Horizontal.LEFT)
                ));
                if (!booking.reference().isBlank()) {
                    labels.add(UiLabels.tertiary(Component.literal(booking.reference()), Horizontal.LEFT));
                }
                UIElement bookingElement = UiLabels.seperatedLabelRow(labels).layout(layout -> layout
                        .marginLeft(8)
                        .justifyContent(AlignContent.FLEX_START));
                history.addChild(bookingElement);
            }
        });

        content.addScrollViewChildren(history);

        return content;
    }

    public static UIElement accountTabLoans(Company company) {
        ScrollerView content = UiContainer.tabScroller();

        ObservableList<Loan> loans = new ObservableList<>();
        SimpleBinding<Tag> loansBinding = DataBindingBuilder.tagS2C(() -> loansToTag(company.loans()))
                .onRemoteSyncReceived(tag -> loans.set(tagToLoans(tag)))
                .build();

        Label title = UiLabels.h2(Component.translatable("ui.createkontor.kontor_desk.tab.open_loans"), Horizontal.LEFT);
        loans.addListener(() -> title.setText(Component.translatable("ui.createkontor.kontor_desk.loans_count", loans.get().size())));
        title.addSyncValue(loansBinding.getSyncValue());
        content.addScrollViewChildren(title);

        UIElement loanBox = new UIElement().addSyncValue(loansBinding.getSyncValue());
        loans.addListener(() -> {
            loanBox.clearAllChildren();
            for (Loan loan : loans.get()) {
                if (loan.isRepaid())
                    continue;

                UIElement loanElement = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8));

                loanElement.addChild(UiLabels.primary(Component.translatable(
                        "ui.createkontor.kontor_desk.loan.principal",
                        Component.translatable("loan.createkontor." + loan.kind().toString().toLowerCase()),
                        ComponentFormatting.moneyColored(loan.principal())
                ), Horizontal.LEFT));

                loanElement.addChild(UiLabels.seperatedLabelRow(List.of(
                        UiLabels.secondary(Component.translatable("ui.createkontor.kontor_desk.loan.outstanding", loan.outstanding().toString()), Horizontal.CENTER),
                        UiLabels.secondary(Component.translatable("ui.createkontor.kontor_desk.loan.rate", loan.rate() + "%"), Horizontal.CENTER),
                        UiLabels.secondary(Component.translatable("ui.createkontor.kontor_desk.loan.repayment", loan.repayment().toString()), Horizontal.CENTER),
                        UiLabels.secondary(Component.translatable("ui.createkontor.kontor_desk.loan.term_days", loan.termDays()), Horizontal.CENTER),
                        UiLabels.secondary(Component.translatable("ui.createkontor.kontor_desk.loan.free_days", loan.freeDays()), Horizontal.CENTER)
                )).layout(layout -> layout.justifyContent(AlignContent.FLEX_START).gapAll(0)));

                loanBox.addChild(loanElement);
            }
        });

        content.addScrollViewChildren(loanBox);

        return content;
    }

    public static UIElement requestsOrdersTab(Company company, KontorDeskBlockEntity be) {
        SplitView.Horizontal split = new SplitView.Horizontal();
        split.layout(layout -> layout.widthPercent(100).heightPercent(100));
        split.setMinPercentage(30);
        split.setMaxPercentage(70);

        split.left(requests(company, be));
        split.right(orders(company));

        return split;
    }

    public static UIElement requests(Company company, KontorDeskBlockEntity be) {
        ScrollerView content = (ScrollerView) UiContainer.tabScroller().style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8));

        ObservableList<Request> requests = new ObservableList<>();
        SimpleBinding<Tag> requestsBinding = DataBindingBuilder.tagS2C(() -> requestsToTag(company.requestBoard().allOpenRequests()))
                .onRemoteSyncReceived(tag -> requests.set(tagToRequests(tag)))
                .build();

        ObservableValue<Integer> requestsLimit = new ObservableValue<>(0);
        SimpleBinding<Integer> requestsLimitBinding = DataBindingBuilder.intValS2C(() -> company.legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).maxOpenRequestsTotal())
                .onRemoteSyncReceived(requestsLimit::set)
                .build();

        Label title = UiLabels.h2(Component.translatable("ui.createkontor.kontor_desk.requests"), Horizontal.LEFT);
        requests.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.kontor_desk.requests_count",
                requests.get().size(),
                requestsLimit.get())
        ));
        requestsLimit.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.kontor_desk.requests_count",
                requests.get().size(),
                requestsLimit.get())
        ));
        title.addSyncValue(requestsBinding.getSyncValue());
        title.addSyncValue(requestsLimitBinding.getSyncValue());
        content.addScrollViewChildren(title);

        Component[] error = { Component.empty() };
        UIElement requestsBox = new UIElement().layout(layout -> layout.gapColumn(4)).addSyncValue(requestsBinding.getSyncValue());
        requestsBox.onMessage("c2s_accept_request", tag -> error[0] = be.acceptRequest(tag.getLong("Number")));

        Label errorLabel = UiLabels.paragraphError(Component.empty(), Horizontal.LEFT);
        var errorBinding = DataBindingBuilder.componentS2C(() -> error[0])
                .onRemoteSyncReceived(errorLabel::setText)
                .build();
        errorLabel.addSyncValue(errorBinding.getSyncValue());
        content.addScrollViewChildren(errorLabel);

        requests.addListener(() -> { // This does not exist on the server
            requestsBox.clearAllChildren();
            List<Request> sorted = new ArrayList<>(List.copyOf(requests.get()));
            sorted.sort(Comparator.comparing(Request::remainingOfferTicks));
            for (Request request : sorted) {
                if (request.hasExpired())
                    continue;

                UIElement requestElement = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8));

                UIElement titleRow = new UIElement()
                        .layout(layout -> layout
                                .flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .justifyContent(AlignContent.FLEX_START)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .marginBottom(4)
                                .widthPercent(100));
                titleRow.addChild(new ItemSlot().setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(request.getProduct().value())), request.getQuantity())));
                titleRow.addChild(UiLabels.paragraphPrimary(Component.translatable(
                    "ui.createkontor.kontor_desk.request.title",
                        ComponentFormatting.highlightStandard(request.getQuantity() + " " + BuiltInRegistries.ITEM.get(ResourceLocation.parse(request.getProduct().value())).asItem().getDescription().getString()),
                        ComponentFormatting.moneyColored(Money.fromDollar(request.getUnitPrice() * request.getQuantity())),
                        Component.literal(String.valueOf(request.getNumber())).withStyle(ChatFormatting.DARK_GRAY)
                ), Horizontal.LEFT).textStyle(style -> style.textAlignVertical(Vertical.CENTER).adaptiveWidth(true)));
                requestElement.addChild(titleRow);

                requestElement.addChild(UiLabels.paragraphSecondary(Component.translatable(
                        "ui.createkontor.kontor_desk.request.unit_price",
                        ComponentFormatting.moneyColored(Money.fromDollar(request.getUnitPrice()))
                ), Horizontal.LEFT));

                requestElement.addChild(UiLabels.paragraphSecondary(Component.translatable(
                        "ui.createkontor.kontor_desk.request.offer_time",
                        ComponentFormatting.ticks(request.remainingOfferTicks()).withStyle(ChatFormatting.GOLD)
                ), Horizontal.LEFT));

                requestElement.addChild(UiLabels.paragraphSecondary(Component.translatable(
                        "ui.createkontor.kontor_desk.request.deadline",
                        ComponentFormatting.ticks(request.getDeadlineTicks())
                ), Horizontal.LEFT));

                requestElement.addChild(UiButtons.primary(Component.translatable("ui.createkontor.kontor_desk.request.accept"))
                        .setOnClick((e) -> {
                            CompoundTag tag = new CompoundTag();
                            tag.putLong("Number", request.getNumber());
                            requestsBox.sendMessage("c2s_accept_request", tag); // Send accept message to request box (exists on client AND server)
                        })
                        .layout(layout -> layout.marginTop(4)));

                requestsBox.addChild(requestElement);
            }
        });
        content.addScrollViewChildren(requestsBox);

        return content;
    }

    public static UIElement orders(Company company) {
        ScrollerView content = (ScrollerView) UiContainer.tabScroller().style(style -> style.background(Sprites.BORDER_DARK)).layout(layout -> layout.paddingAll(8));

        ObservableList<Order> orders = new ObservableList<>();
        SimpleBinding<Tag> ordersBinding = DataBindingBuilder.tagS2C(() -> ordersToTag(company.orderBook().allOrders()))
                .onRemoteSyncReceived(tag -> orders.set(tagToOrders(tag)))
                .build();

        ObservableValue<Integer> ordersLimit = new ObservableValue<>(0);
        SimpleBinding<Integer> ordersLimitBinding = DataBindingBuilder.intValS2C(() -> company.legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).maxOpenOrders())
                .onRemoteSyncReceived(ordersLimit::set)
                .build();

        Label title = UiLabels.h2(Component.translatable("ui.createkontor.kontor_desk.orders"), Horizontal.LEFT);
        orders.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.kontor_desk.orders_count",
                orders.get().size(),
                ordersLimit.get())
        ));
        ordersLimit.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.kontor_desk.orders_count",
                orders.get().size(),
                ordersLimit.get())
        ));
        title.addSyncValue(ordersBinding.getSyncValue());
        title.addSyncValue(ordersLimitBinding.getSyncValue());
        content.addScrollViewChildren(title);

        content.addScrollViewChildren(UiLabels.paragraphError(Component.empty(), Horizontal.LEFT)); // Placeholder so that the orders start on the same height as the requests

        UIElement ordersBox = new UIElement().layout(layout -> layout.gapColumn(4)).addSyncValue(ordersBinding.getSyncValue());
        orders.addListener(() -> { // This does not exist on the server
            ordersBox.clearAllChildren();
            List<Order> sorted = new ArrayList<>(List.copyOf(orders.get()));
            sorted.sort(Comparator.comparing(Order::remainingDeadlineTicks));
            for (Order order : sorted) {
                if (order.isFullyDelivered())
                    continue;

                UIElement orderElement = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8));

                UIElement titleRow = new UIElement()
                        .layout(layout -> layout
                                .flexDirection(FlexDirection.ROW)
                                .flexWrap(FlexWrap.WRAP)
                                .justifyContent(AlignContent.FLEX_START)
                                .alignItems(AlignItems.CENTER)
                                .gapAll(4)
                                .marginBottom(4)
                                .widthPercent(100));
                titleRow.addChild(new ItemSlot().setItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(order.getProduct().value())), order.getQuantity())));
                titleRow.addChild(UiLabels.paragraphPrimary(Component.translatable(
                        "ui.createkontor.kontor_desk.request.title",
                        ComponentFormatting.highlightStandard(order.getQuantity() + " " + BuiltInRegistries.ITEM.get(ResourceLocation.parse(order.getProduct().value())).asItem().getDescription().getString()),
                        ComponentFormatting.moneyColored(Money.fromDollar(order.getUnitPrice() * order.getQuantity())),
                        Component.literal(String.valueOf(order.getNumber())).withStyle(ChatFormatting.DARK_GRAY)
                ), Horizontal.LEFT).textStyle(style -> style.textAlignVertical(Vertical.CENTER).adaptiveWidth(true)));
                orderElement.addChild(titleRow);

                if (order.getPhase() == OrderPhase.OPEN) {
                    orderElement.addChild(UiLabels.paragraphSecondary(Component.translatable(
                            "ui.createkontor.kontor_desk.delivery_time",
                            ComponentFormatting.ticks(order.remainingDeadlineTicks()).withStyle(ChatFormatting.GOLD)
                    ), Horizontal.LEFT));
                } else {
                    orderElement.addChild(UiLabels.paragraphError(Component.translatable("ui.createkontor.kontor_desk.grace_period"), Horizontal.LEFT));
                }

                ordersBox.addChild(orderElement);
            }
        });
        content.addScrollViewChildren(ordersBox);

        return content;
    }

    private static List<Booking> tagToBookings(Tag tag) {
        ListTag list = (ListTag) tag;
        List<Booking> bookings = new ArrayList<>();
        for (Tag t : list) {
            CompoundTag bookingTag = (CompoundTag) t;
            bookings.add(new Booking(
                    bookingTag.getLong("Day"),
                    BookingKind.valueOf(bookingTag.getString("Kind")),
                    Money.ofCents(bookingTag.getLong("Amount")),
                    bookingTag.getString("Reference"),
                    Money.ofCents(bookingTag.getLong("BalanceAfter"))));
        }
        return bookings;
    }

    private static Tag bookingsToTag(List<Booking> bookings) {
        ListTag tag = new ListTag();
        for (Booking booking : bookings) {
            CompoundTag bookingTag = new CompoundTag();
            bookingTag.putLong("Day", booking.day());
            bookingTag.putString("Kind", booking.kind().name());
            bookingTag.putLong("Amount", booking.amount().cents());
            bookingTag.putString("Reference", booking.reference());
            bookingTag.putLong("BalanceAfter", booking.balanceAfter().cents());
            tag.add(bookingTag);
        }
        return tag;
    }

    private static void buildBalanceChart(List<Booking> bookings, long overdraftLimit, UIElement parent) {
        ChartElement newChart = ChartElement.from(CompanyCharts.balanceSpecFromBookings(bookings, -overdraftLimit / 100.0));
        newChart.layout(layout -> layout.widthPercent(100).flex(1));
        parent.clearAllChildren();
        parent.addChild(newChart);
    }

    private static List<Loan> tagToLoans(Tag tag) {
        ListTag list = (ListTag) tag;
        List<Loan> loans = new ArrayList<>();
        for (Tag t : list) {
            loans.add(Loan.restore(CompanySavedData.readLoan((CompoundTag) t)));
        }
        return loans;
    }

    private static Tag loansToTag(List<Loan> loans) {
        ListTag tag = new ListTag();
        for (Loan loan : loans) {
            tag.add(CompanySavedData.writeLoan(loan.getSaveState()));
        }
        return tag;
    }

    private static List<Request> tagToRequests(Tag tag) {
        List<Request> requests = new ArrayList<>();
        ListTag list = (ListTag) tag;
        for (Tag t : list) {
            CompoundTag requestTag = (CompoundTag) t;
            requests.add(new Request(
                    requestTag.getLong("Number"),
                    new ItemId(requestTag.getString("Product")),
                    requestTag.getInt("Quantity"),
                    requestTag.getInt("QuantityFactor"),
                    requestTag.getDouble("Urgency"),
                    requestTag.getDouble("UnitPrice"),
                    requestTag.getLong("DeadlineTicks"),
                    requestTag.getLong("RemainingOfferTicks")));
        }
        return requests;
    }

    private static Tag requestsToTag(List<Request> requests) {
        ListTag tag = new ListTag();
        for (Request request : requests) {
            CompoundTag requestTag = new CompoundTag();
            requestTag.putLong("Number", request.getNumber());
            requestTag.putString("Product", request.getProduct().value());
            requestTag.putInt("Quantity", request.getQuantity());
            requestTag.putInt("QuantityFactor", request.getQuantityFactor());
            requestTag.putDouble("Urgency", request.getUrgency());
            requestTag.putDouble("UnitPrice", request.getUnitPrice());
            requestTag.putLong("DeadlineTicks", request.getDeadlineTicks());
            requestTag.putLong("RemainingOfferTicks", request.remainingOfferTicks());
            tag.add(requestTag);
        }
        return tag;
    }

    private static List<Order> tagToOrders(Tag tag) {
        List<Order> orders = new ArrayList<>();
        ListTag list = (ListTag) tag;
        for (Tag t : list) {
            CompoundTag orderTag = (CompoundTag) t;
            orders.add(new Order(
                    orderTag.getLong("Number"),
                    new ItemId(orderTag.getString("Product")),
                    orderTag.getInt("Quantity"),
                    orderTag.getDouble("UnitPrice"),
                    orderTag.getDouble("Urgency"),
                    orderTag.getLong("DeadlineTicks"),
                    orderTag.getLong("GracePeriodTicks"),
                    new RequestOrigin(orderTag.getLong("OriginRequestNumber")), // TODO needs changing when more order origins get added
                    orderTag.getInt("DeliveredQuantity"),
                    orderTag.getLong("RemainingDeadlineTicks"),
                    orderTag.getLong("RemainingGracePeriodTicks"),
                    OrderPhase.valueOf(orderTag.getString("Phase"))));
        }
        return orders;
    }

    private static Tag ordersToTag(List<Order> orders) {
        ListTag tag = new ListTag();
        for (Order order : orders) {
            CompoundTag orderTag = new CompoundTag();
            orderTag.putLong("Number", order.getNumber());
            orderTag.putString("Product", order.getProduct().value());
            orderTag.putInt("Quantity", order.getQuantity());
            orderTag.putDouble("UnitPrice", order.getUnitPrice());
            orderTag.putDouble("Urgency", order.getUrgency());
            orderTag.putLong("DeadlineTicks", order.getDeadlineTicks());
            orderTag.putLong("GracePeriodTicks", order.getGracePeriodTicks());
            orderTag.putLong("OriginRequestNumber", ((RequestOrigin) order.getOrigin()).requestNumber());
            orderTag.putInt("DeliveredQuantity", order.getDeliveredQuantity());
            orderTag.putLong("RemainingDeadlineTicks", order.remainingDeadlineTicks());
            orderTag.putLong("RemainingGracePeriodTicks", order.remainingGracePeriodTicks());
            orderTag.putString("Phase", order.getPhase().name());
            tag.add(orderTag);
        }
        return tag;
    }
}
