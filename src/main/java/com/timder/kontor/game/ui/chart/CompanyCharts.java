package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.company.CompanyHistoryEntry;
import com.timder.kontor.core.company.financial.Booking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

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
            e.putLong("balanceCents", entry.balance().cents());
            list.add(e);
        }
        return list;
    }

    public static ChartSpec revenueResultSpecFromHistoryTag(Tag tag) {
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
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + e.getLong("day"));
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.revenue_result.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.revenue_result.series.revenue").getString(), ChartColors.BLUE, x, result))
                .series(new ChartSeries(Component.translatable("chart.createkontor.revenue_result.series.result").getString(), ChartColors.TEAL, x, revenue))
                .includeZero(true)
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .pointLabels(pointLabels)
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30)
                .build();
    }

    public static ChartSpec balanceSpecFromBookings(List<Booking> bookings, double overdraftLimit) {
        int count = bookings.size();
        double[] x = new double[count];
        double[] balance = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        long mostRecentDay = count > 0 ? bookings.get(count - 1).day() : 0;

        int i = 0;
        while (i < count) {
            long day = bookings.get(i).day();
            int start = i;
            while (i < count && bookings.get(i).day() == day) {
                i++;
            }
            int dayCount = i - start;
            for (int j = start; j < i; j++) {
                double fraction = (j - start) / (double) dayCount;
                Booking booking = bookings.get(j);
                x[j] = (day - mostRecentDay) + fraction;
                balance[j] = booking.balanceAfter().cents() / 100.0;
                pointLabels.add(Component.translatable("chart.createkontor.day").getString() + booking.day());
            }
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.balance.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.balance.series.balance").getString(), ChartColors.BLUE, x, balance))
                .referenceLine(Component.translatable("chart.createkontor.balance.reference.overdraft").getString(), overdraftLimit, ChartColors.RED)
                .includeZero(true)
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .pointLabels(pointLabels)
                .xRangeOptions(List.of(10, 25, 50, 100, 250), 25)
                .xRangeUnit(Component.translatable("chart.createkontor.balance.bookings").getString())
                .build();
    }

}
