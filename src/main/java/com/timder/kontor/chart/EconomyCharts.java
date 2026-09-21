package com.timder.kontor.chart;

import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.core.macro.MacroRules;

import java.util.ArrayList;
import java.util.List;

public final class EconomyCharts {

    public static ChartSpec full(List<MacroHistoryEntry> entries) {
        int count = entries.size();
        double[] x = new double[count];
        double[] index = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            MacroHistoryEntry entry = entries.get(i);
            x[i] = -(double) (count - 1 - i);
            index[i] = entry.index();
            pointLabels.add("Day " + entry.day());
        }

        return ChartSpec.builder("Economy overview")
                .series(new ChartSeries("Cycle Index", ChartColors.BLUE, x, index))
                .xAxis("d", "now")
                .referenceLine("Boom Threshold", MacroRules.BOOM_THRESHOLD, ChartColors.GREEN)
                .referenceLine("Recession Threshold", MacroRules.RECESSION_THRESHOLD, ChartColors.RED)
                .pointLabels(pointLabels)
                .build();
    }
}
