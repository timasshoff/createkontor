package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Tab;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.util.ComponentFormatting;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

public final class UiContainer {

    public static UIElement hudBox(Company company, Economy economy) {
        UIElement hud = new UIElement()
                .layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .top(3)
                        .right(3)
                        .flexDirection(FlexDirection.ROW)
                        .paddingAll(3)
                        .gapAll(4)
                        .alignItems(AlignItems.CENTER))
                .style(style -> style.background(Sprites.BORDER_DARK).zIndex(1));

        Label day = (Label) UiLabels.primary(Component.empty(), Horizontal.CENTER).textStyle(style -> style.textAlignVertical(Vertical.CENTER));
        day.addSyncValue(
                DataBindingBuilder.componentS2C(() -> ComponentFormatting.day(economy.currentDay()))
                        .onRemoteSyncReceived(day::setText)
                        .build().getSyncValue()
        );

        Label balance = (Label) UiLabels.primary(Component.empty(), Horizontal.CENTER).textStyle(style -> style.textAlignVertical(Vertical.CENTER));
        balance.addSyncValue(
                DataBindingBuilder.componentS2C(() -> ComponentFormatting.moneyColored(company.account().getBalance()))
                        .onRemoteSyncReceived(balance::setText)
                        .build().getSyncValue()
        );

        hud.addChildren(day, UiLabels.secondary(Component.literal("•"), Horizontal.CENTER), balance);
        return hud;
    }

    public static UIElement withHud(UIElement mainContent, UIElement hud) {
        return new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .justifyContent(AlignContent.CENTER)
                        .alignItems(AlignItems.CENTER))
                .addChildren(mainContent, hud);
    }

    public static UIElement small() {
       return new UIElement()
               .layout(layout -> layout.widthPercent(50).heightPercent(50).paddingAll(8).gapAll(4))
               .style(style -> style.background(Sprites.BORDER_DARK));
    }

    public static ScrollerView smallScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.widthPercent(50).heightPercent(50).paddingAll(8));
        scroller.style(style -> style.background(Sprites.BORDER_DARK));

        return scroller
                .scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL))
                .viewPort(viewPort -> viewPort
                        .layout(layout -> layout.paddingAll(0))
                        .style(style -> style.background(IGuiTexture.EMPTY)))
                .viewContainer(viewContainer -> viewContainer.layout(layout -> layout.gapAll(4)))
                .verticalScroller(vs -> vs.layout(layout -> layout.marginLeft(4)));
    }

    public static UIElement large() {
        return new UIElement()
                .layout(layout -> layout.widthPercent(95).heightPercent(92).paddingAll(8).gapAll(4))
                .style(style -> style.background(Sprites.BORDER_DARK));
    }

    public static ScrollerView largeScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.widthPercent(95).heightPercent(92).paddingAll(8));
        scroller.style(style -> style.background(Sprites.BORDER_DARK));

        return scroller
                .scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL))
                .viewPort(viewPort -> viewPort
                        .layout(layout -> layout.paddingAll(0))
                        .style(style -> style.background(IGuiTexture.EMPTY)))
                .viewContainer(viewContainer -> viewContainer.layout(layout -> layout.gapAll(4)))
                .verticalScroller(vs -> vs.layout(layout -> layout.marginLeft(4)));
    }

    public static TabView largeTabView() {
        TabView tabView = new TabView();
        tabView.layout(layout -> layout.widthPercent(95).heightPercent(92));
        tabView.tabScroller(scroller -> {
            Runnable updateOverlap = () -> {
                boolean overflowing = scroller.getContainerWidth() > scroller.viewPort.getContentWidth();
                scroller.layout(layout -> layout.marginBottom(overflowing ? -6 : -2));
            };
            scroller.viewContainer.addEventListener(UIEvents.LAYOUT_CHANGED, event -> updateOverlap.run());
            updateOverlap.run();
        });
        tabView.tabContentContainer(content -> content
                .layout(layout -> layout
                        .paddingAll(8)
                        .gapAll(4)
                        .flexBasis(0))
                .style(style -> style.background(Sprites.BORDER_DARK)));
        return tabView;
    }

    public static Tab tab(Component text) {
        return new Tab().setText(text);
    }

    public static ScrollerView tabScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.widthPercent(100).heightPercent(100));

        return scroller
                .scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL))
                .viewPort(viewPort -> viewPort
                        .layout(layout -> layout.paddingAll(0))
                        .style(style -> style.background(IGuiTexture.EMPTY)))
                .viewContainer(viewContainer -> viewContainer.layout(layout -> layout.gapAll(4)))
                .verticalScroller(vs -> vs.layout(layout -> layout.marginLeft(4)));
    }

    public static UIElement headerWithSubtitle(Label header, UIElement subtitle) {
        UIElement headerWithSubtitle = new UIElement()
                .layout(layout -> layout
                        .paddingAll(8)
                        .gapAll(4)
                        .widthFitContent()
                        .alignSelf(AlignItems.CENTER)
                        .alignItems(AlignItems.CENTER))
                .style(style -> style.background(Sprites.BORDER_DARK));
        headerWithSubtitle.addChild(header);
        headerWithSubtitle.addChild(subtitle);
        return headerWithSubtitle;
    }
}
