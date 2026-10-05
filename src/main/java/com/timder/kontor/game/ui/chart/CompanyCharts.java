package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.company.CompanyHistoryEntry;
import com.timder.kontor.core.company.financial.Booking;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.*;

public class CompanyCharts {

    public static ChartSpec revenueResultSpecFromHistory(List<CompanyHistoryEntry> history) {
        int count = history.size();
        double[] x = new double[count];
        double[] result = new double[count];
        double[] revenue = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            CompanyHistoryEntry entry = history.get(i);
            x[i] = -(double) (count - 1 - i);
            result[i] = entry.result().cents() / 100.0;
            revenue[i] = entry.revenue().cents() / 100.0;
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + entry.day());
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.revenue_result.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.revenue_result.series.revenue").getString(), ChartColors.GREEN, x, revenue))
                .series(new ChartSeries(Component.translatable("chart.createkontor.revenue_result.series.result").getString(), ChartColors.BLUE, x, result))
                .referenceLine("0", 0, ChartColors.WHITE)
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

    public static ChartSpec costStructureFromHistory(List<CompanyHistoryEntry> history) {
        int count = history.size();
        double[] x = new double[count];
        List<String> pointLabels = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            x[i] = -(double) (count - 1 - i);
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + history.get(i).day());
        }

        Map<BookingKind, Long> totalCentsByKind = new EnumMap<>(BookingKind.class);
        for (CompanyHistoryEntry entry : history) {
            for (Map.Entry<BookingKind, Money> cost : entry.costsByKind().entrySet()) {
                totalCentsByKind.merge(cost.getKey(), cost.getValue().cents(), Long::sum);
            }
        }

        List<BookingKind> byTotalDesc = new ArrayList<>(totalCentsByKind.keySet());
        byTotalDesc.sort(Comparator.comparingLong(totalCentsByKind::get));

        int namedCount = byTotalDesc.size() <= ChartSpec.MAX_SERIES
                ? byTotalDesc.size()
                : ChartSpec.MAX_SERIES - 1;
        List<BookingKind> namedKinds = byTotalDesc.subList(0, namedCount);

        ChartSpec.Builder builder = ChartSpec.builder(Component.translatable("chart.createkontor.cost_structure.title").getString())
                .kind(ChartKind.STACKED_BAR)
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .hideXAxis()
                .pointLabels(pointLabels)
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30);

        for (int k = 0; k < namedKinds.size(); k++) {
            BookingKind kind = namedKinds.get(k);
            double[] y = new double[count];
            for (int i = 0; i < count; i++) {
                y[i] = history.get(i).costsByKind().getOrDefault(kind, Money.ZERO).negate().cents() / 100.0;
            }
            String label = Component.translatable("enum.createkontor.booking_kind." + kind.toString().toLowerCase()).getString();
            builder.series(new ChartSeries(label, ChartColors.STACK[k], x, y));
        }

        if (namedKinds.size() < byTotalDesc.size()) {
            double[] y = new double[count];
            for (int i = 0; i < count; i++) {
                double sum = 0.0;
                for (Map.Entry<BookingKind, Money> cost : history.get(i).costsByKind().entrySet()) {
                    if (!namedKinds.contains(cost.getKey())) {
                        sum += cost.getValue().negate().cents() / 100.0;
                    }
                }
                y[i] = sum;
            }
            builder.series(new ChartSeries(Component.translatable("chart.createkontor.other").getString(), ChartColors.STACK[namedKinds.size()], x, y));
        }

        return builder.build();
    }

}
