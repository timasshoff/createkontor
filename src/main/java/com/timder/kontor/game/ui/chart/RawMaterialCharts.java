package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
import com.timder.kontor.game.ui.chart.classic.ChartSeries;
import com.timder.kontor.game.ui.chart.classic.ChartSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class RawMaterialCharts {

    public static ChartSpec full(ResourceLocation material, List<RawMaterialHistoryEntry> entries) {
        int count = entries.size();
        double[] x = new double[count];
        double[] price = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            RawMaterialHistoryEntry entry = entries.get(i);
            x[i] = -(double) (count - 1 - i);
            price[i] = entry.price();
            pointLabels.add("Day " + entry.day());
        }

        return ChartSpec.builder(material.toString())
                .series(new ChartSeries("Price in $", ChartColors.BLUE, x, price))
                .xAxis("d", "now")
                .pointLabels(pointLabels)
                .build();
    }

    public static ChartSpec price(List<RawMaterialHistoryEntry> history) {
        int count = history.size();
        double[] x = new double[count];
        double[] price = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            RawMaterialHistoryEntry entry = history.get(i);
            x[i] = -(double) (count - 1 - i);
            price[i] = entry.price();
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + entry.day());
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.raw_material_price.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.raw_material_price.series.price").getString(), ChartColors.GREEN, x, price))
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30)
                .pointLabels(pointLabels)
                .build();
    }
}
