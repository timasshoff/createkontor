package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.ui.chart.CompanyCharts;
import com.timder.kontor.client.ui.element.ChartElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.KontorDeskBlockEntity;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.util.ComponentFormatting;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.chat.Component;

public class KontorDeskBoundUi {
    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity be, Company company, Economy economy) {
        var root = UiContainer.largeTabView();

        root.addTab(UiContainer.tab(Component.literal("Overview")), overviewTab(company, economy));
        root.addTab(UiContainer.tab(Component.literal("Requests")), requestsTab());

        return new ModularUI(UI.of(root, StylesheetManager.GDP), holder.player);
    }

    public static UIElement overviewTab(Company company, Economy economy) {
        ScrollerView overviewContent = UiContainer.tabScroller();

        UIElement header = new UIElement()
                .layout(layout -> layout
                        .paddingAll(8)
                        .gapAll(4)
                        .widthFitContent()
                        .alignSelf(AlignItems.CENTER)
                        .alignItems(AlignItems.CENTER))
                .style(style -> style.background(Sprites.BORDER_DARK));

        Label title = (Label) UiLabels.h1(Component.empty(), Horizontal.CENTER).layout(layout -> layout.alignSelf(AlignItems.CENTER));
        var titleBinding = DataBindingBuilder.componentS2C(() -> Component.literal(company.name()))
                .onRemoteSyncReceived(title::setText)
                .build();
        title.addSyncValue(titleBinding.getSyncValue());
        header.addChild(title);

        Label currentDay = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        currentDay.addSyncValue(
                DataBindingBuilder.componentS2C(() -> ComponentFormatting.day(economy.currentDay()))
                        .onRemoteSyncReceived(currentDay::setText)
                        .build().getSyncValue()
        );

        Label balance = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        var balanceBinding = DataBindingBuilder.componentS2C(() -> ComponentFormatting.moneyColored(company.account().getBalance()))
                        .onRemoteSyncReceived(balance::setText)
                        .build();
        balance.addSyncValue(balanceBinding.getSyncValue());

        Label liquidity = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        var liquidityBinding = DataBindingBuilder.componentS2C(() -> Component.translatable("enum.createkontor.liquidity." + company.liquidity().toString().toLowerCase()))
                .onRemoteSyncReceived(liquidity::setText)
                .build();
        liquidity.addSyncValue(liquidityBinding.getSyncValue());

        Label openOrders = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        var openOrdersBinding = DataBindingBuilder.componentS2C(() -> Component.translatable("ui.createkontor.kontor_desk.open_orders", company.orderBook().openOrders()))
                .onRemoteSyncReceived(openOrders::setText)
                .build();
        openOrders.addSyncValue(openOrdersBinding.getSyncValue());

        Label legalForm = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        var legalFormBinding = DataBindingBuilder.componentS2C(() -> Component.translatable("legal_forms.createkontor." + company.legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).toString()))
                .onRemoteSyncReceived(legalForm::setText)
                .build();
        legalForm.addSyncValue(legalFormBinding.getSyncValue());

        UIElement headerRow = new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .flexWrap(FlexWrap.WRAP)
                        .justifyContent(AlignContent.CENTER)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100))
                .addChildren(
                        currentDay,
                        UiLabels.secondary(Component.literal("•"), Horizontal.CENTER),
                        balance,
                        UiLabels.secondary(Component.literal("•"), Horizontal.CENTER),
                        liquidity,
                        UiLabels.secondary(Component.literal("•"), Horizontal.CENTER),
                        openOrders,
                        UiLabels.secondary(Component.literal("•"), Horizontal.CENTER),
                        legalForm
                );

        header.addChild(headerRow);
        overviewContent.addScrollViewChildren(header);

        UIElement dayResultChartBox = new UIElement();
        dayResultChartBox.layout(layout -> layout.widthPercent(100).height(200));
        dayResultChartBox.addSyncValue(
                DataBindingBuilder.tagS2C(() -> CompanyCharts.historyToTag(company.history()))
                        .onRemoteSyncReceived(tag -> {
                            ChartElement newChart = ChartElement.from(CompanyCharts.specFromHistoryTag(tag));
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

    public static UIElement requestsTab() {
        ScrollerView content = UiContainer.tabScroller();
        content.addScrollViewChildren(UiLabels.h1(Component.literal("Requests"), Horizontal.LEFT));
        return content;
    }
}
