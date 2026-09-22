package com.timder.kontor.chart;

import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
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
}
