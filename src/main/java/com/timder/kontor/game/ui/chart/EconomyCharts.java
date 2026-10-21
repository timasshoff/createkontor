package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.core.macro.MacroRules;
import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import com.timder.kontor.game.ui.chart.classic.ChartSeries;
import com.timder.kontor.game.ui.chart.classic.ChartSpec;
import net.minecraft.network.chat.Component;

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

    public static ChartSpec policyRate(List<MacroHistoryEntry> history) {
        int count = history.size();
        double[] x = new double[count];
        double[] policyRate = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            MacroHistoryEntry entry = history.get(i);
            x[i] = -(double) (count - 1 - i);
            policyRate[i] = entry.policyRate();
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + entry.day());
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.policy_rate.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.policy_rate.series.policy_rate").getString(), ChartColors.TEAL, x, policyRate))
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30)
                .pointLabels(pointLabels)
                .build();
    }
}
