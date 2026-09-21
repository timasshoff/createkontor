package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.client.ui.element.ChartElement;
import com.timder.kontor.chart.ChartSpec;

public final class BasicChartUI {
    public static ModularUI create(ChartSpec spec) {
        UIElement root = new UIElement();

        root.layout(layout -> layout.widthPercent(90).heightPercent(90).paddingAll(8).gapAll(4));
        root.style(style -> style.background(Sprites.BORDER_DARK));

        root.addChild(new Label().setText("Chart"));

        ChartElement chart = ChartElement.from(spec);
        chart.layout(layout -> layout.widthPercent(100).flex(1));
        root.addChild(chart);

        return ModularUI.of(UI.of(root));
    }
}
