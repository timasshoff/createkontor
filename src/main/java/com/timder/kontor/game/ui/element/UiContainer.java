package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class UiContainer {

    private static final float GRID_GAP = 4;

    public static UIElement hudBox(Company company, Economy economy) {
        UIElement hud = new UIElement()
                .layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .top(2)
                        .right(2)
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
                .layout(layout -> layout.widthPercent(95).heightPercent(95).paddingAll(8).gapAll(4))
                .style(style -> style.background(Sprites.BORDER_DARK));
    }

    public static ScrollerView largeScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.widthPercent(95).heightPercent(95).paddingAll(8));
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
        tabView.layout(layout -> layout.widthPercent(95).heightPercent(95));
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

    public static UIElement twoColumns(List<? extends UIElement> elements) {
        int split = (elements.size() + 1) / 2;
        return twoColumns(elements.subList(0, split), elements.subList(split, elements.size()));
    }

    public static UIElement twoColumns(List<? extends UIElement> left, List<? extends UIElement> right) {
        UIElement row = new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.FLEX_START)
                        .gapAll(8)
                        .widthPercent(100));
        row.addChild(column(left));
        row.addChild(columnDivider());
        row.addChild(column(right));
        return row;
    }

    private static UIElement columnDivider() {
        return new UIElement()
                .layout(layout -> layout
                        .width(1)
                        .alignSelf(AlignItems.STRETCH))
                .style(style -> style.background(new ColorRectTexture(0xFF555555)));
    }

    private static UIElement column(List<? extends UIElement> elements) {
        UIElement column = new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.COLUMN)
                        .flexBasis(0)
                        .flexGrow(1)
                        .gapAll(4));
        for (UIElement element : elements) {
            column.addChild(element);
        }
        return column;
    }

    public static UIElement itemSlotRow(List<ItemStack> items) {
        UIElement row = new UIElement().layout(layout -> layout
                .widthPercent(100)
                .flexDirection(FlexDirection.ROW)
                .flexWrap(FlexWrap.WRAP)
                .alignItems(AlignItems.FLEX_START)
                .gapAll(2));
        for (ItemStack stack : items) {
            row.addChild(new ItemSlot().setItem(stack));
        }
        return row;
    }

    public static <T> UIElement searchableGrid(ObservableList<T> entries,
                                               Function<T, UIElement> cardFactory,
                                               Function<T, List<String>> searchTexts,
                                               Component searchPlaceholder,
                                               Component noResultsText,
                                               int maxColumns,
                                               float minCardWidth) {

        ObservableValue<String> query = new ObservableValue<>("");

        TextField searchField = new TextField();
        searchField.textFieldStyle(style -> style.placeholder(searchPlaceholder));
        searchField.setTextResponder(query::set);
        searchField.layout(layout -> layout.widthPercent(100));

        ScrollerView scroller = tabScroller();
        scroller.layout(layout -> layout.heightAuto().flexBasis(0).flexGrow(1).minHeight(0));
        scroller.viewContainer.layout(layout -> layout.gapAll(0).display(TaffyDisplay.GRID));

        int[] appliedTracks = {0};
        float[] appliedTrackWidth = {0};
        float[] appliedWidth = {0};
        boolean[] showingEmpty = {false};

        Runnable applyLayout = () -> {
            float available = scroller.viewPort.getContentWidth();
            if (available <= 0) return;

            int columns = Math.max(1, Math.min(maxColumns, (int) (available / minCardWidth)));
            int tracks = showingEmpty[0] ? 1 : columns;
            float trackWidth = (float) Math.floor(available / tracks * 100f) / 100f;

            if (tracks != appliedTracks[0] || trackWidth != appliedTrackWidth[0]) {
                appliedTracks[0] = tracks;
                appliedTrackWidth[0] = trackWidth;
                String template = String.format(Locale.ROOT, "%.2fpx ", trackWidth).repeat(tracks).trim();
                scroller.viewContainer.layout(layout -> layout.gridTemplateColumns(template));
            }
            if (Math.abs(available - appliedWidth[0]) >= 0.01f) {
                appliedWidth[0] = available;
                scroller.viewContainer.layout(layout -> layout.width(available));
            }
        };

        Runnable rebuild = () -> {
            List<String> terms = searchTerms(query.get());
            scroller.clearAllScrollViewChildren();

            int shown = 0;
            for (T entry : entries.get()) {
                if (!matchesAny(searchTexts.apply(entry), terms)) continue;
                UIElement card = cardFactory.apply(entry);
                card.layout(layout -> layout.marginAll(GRID_GAP / 2f));
                scroller.addScrollViewChild(card);
                shown++;
            }
            showingEmpty[0] = shown == 0;
            if (shown == 0) {
                scroller.addScrollViewChild(UiLabels.secondary(noResultsText, Horizontal.CENTER)
                        .layout(layout -> layout.widthPercent(100)));
            }
            applyLayout.run();
        };

        scroller.viewPort.addEventListener(UIEvents.LAYOUT_CHANGED, event -> applyLayout.run());
        query.addListener(rebuild);
        entries.addListener(rebuild);
        rebuild.run();

        return new UIElement()
                .layout(layout -> layout.widthPercent(100).flexBasis(0).flexGrow(1).minHeight(0).gapAll(4))
                .addChildren(searchField, scroller);
    }

    private static List<String> searchTerms(String query) {
        String trimmed = query.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? List.of() : List.of(trimmed.split("\\s+"));
    }

    private static boolean matchesAny(List<String> texts, List<String> terms) {
        if (terms.isEmpty()) {
            return true;
        }
        return texts.stream()
                .map(text -> text.toLowerCase(Locale.ROOT))
                .anyMatch(haystack -> terms.stream().allMatch(haystack::contains));
    }
}
