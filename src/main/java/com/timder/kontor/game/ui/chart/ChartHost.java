package com.timder.kontor.game.ui.chart;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

import javax.annotation.Nullable;

public class ChartHost extends UIElement {
    @Nullable
    private ChartElement chart;

    public void show(ChartSpec spec) {
        if (chart == null) {
            chart = ChartElement.from(spec);
            chart.layout(layout -> layout.widthPercent(100).flex(1));
            addChild(chart);
        } else {
            chart.update(spec);
        }
    }
}
