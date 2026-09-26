package com.timder.kontor.game.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;

public final class UiContainer {
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
                .layout(layout -> layout.widthPercent(90).heightPercent(90).paddingAll(8).gapAll(4))
                .style(style -> style.background(Sprites.BORDER_DARK));
    }

    public static ScrollerView largeScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.widthPercent(90).heightPercent(90).paddingAll(8));
        scroller.style(style -> style.background(Sprites.BORDER_DARK));

        return scroller
                .scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL))
                .viewPort(viewPort -> viewPort
                        .layout(layout -> layout.paddingAll(0))
                        .style(style -> style.background(IGuiTexture.EMPTY)))
                .viewContainer(viewContainer -> viewContainer.layout(layout -> layout.gapAll(4)))
                .verticalScroller(vs -> vs.layout(layout -> layout.marginLeft(4)));
    }
}
