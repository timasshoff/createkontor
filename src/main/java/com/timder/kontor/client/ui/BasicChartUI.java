package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.timder.kontor.game.ui.chart.classic.ChartElement;
import com.timder.kontor.game.ui.chart.classic.ChartSpec;
import com.timder.kontor.game.ui.element.UiStyles;

public final class BasicChartUI {
    public static ModularUI create(ChartSpec spec) {
        UIElement root = new UIElement();

        root.layout(layout ->
                layout.widthPercent(90).heightPercent(90).paddingAll(8).gapAll(4));
        root.style(UiStyles.PANEL);

        root.addChild(new Label().setText("Chart"));

        ChartElement chart = ChartElement.from(spec);
        chart.layout(layout -> layout.widthPercent(100).flex(1));
        root.addChild(chart);

        return ModularUI.of(UI.of(root));
    }
}
