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
        double[] policyRate = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            MacroHistoryEntry entry = entries.get(i);
            x[i] = -(double) (count - 1 - i);
            index[i] = entry.index();
            policyRate[i] = entry.policyRate() * 100;
            pointLabels.add("Day " + entry.day());
        }

        return ChartSpec.builder("Economy overview")
                .series(new ChartSeries("Cycle Index", ChartColors.BLUE, x, index))
                .series(new ChartSeries("Policy Rate in %", ChartColors.TEAL, x, policyRate))
                .xAxis("d", "now")
                .referenceLine("Cycle Boom Threshold", MacroRules.BOOM_THRESHOLD, ChartColors.GREEN)
                .referenceLine("Cycle Recession Threshold", MacroRules.RECESSION_THRESHOLD, ChartColors.RED)
                .pointLabels(pointLabels)
                .build();
    }
}
