package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.company.CompanyHistoryEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class CompanyCharts {

    public static Tag historyToTag(List<CompanyHistoryEntry> history) {
        ListTag list = new ListTag();
        for (CompanyHistoryEntry entry : history) {
            CompoundTag e = new CompoundTag();
            e.putLong("day", entry.day());
            e.putLong("resultCents", entry.result().cents());
            e.putLong("revenueCents", entry.revenue().cents());
            list.add(e);
        }
        return list;
    }

    public static ChartSpec specFromHistoryTag(Tag tag) {
        ListTag list = (ListTag) tag;
        int count = list.size();
        double[] x = new double[count];
        double[] result = new double[count];
        double[] revenue = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            CompoundTag e = list.getCompound(i);
            x[i] = -(double) (count - 1 - i);
            result[i] = e.getLong("resultCents") / 100.0;
            revenue[i] = e.getLong("revenueCents") / 100.0;
            pointLabels.add("Day " + e.getLong("day"));
        }

        return ChartSpec.builder("Day Result")
                .series(new ChartSeries("Result", ChartColors.BLUE, x, result))
                .series(new ChartSeries("Revenue", ChartColors.TEAL, x, revenue))
                .includeZero(true)
                .xAxis("d", "Today")
                .pointLabels(pointLabels)
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30)
                .build();
    }
}
