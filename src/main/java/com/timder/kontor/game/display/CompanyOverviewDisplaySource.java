package com.timder.kontor.game.display;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.KontorDeskBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

public class CompanyOverviewDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.level() instanceof ServerLevel level)) return EMPTY;
        if (!(context.getSourceBlockEntity() instanceof KontorDeskBlockEntity desk)) return EMPTY;
        if (desk.getCompanyId() == null) return EMPTY;

        Company company = CompanySavedData.get(level.getServer())
                .getRegistry()
                .get(desk.getCompanyId())
                .orElse(null);
        if (company == null) {
            return EMPTY;
        }

        int orders = 0;
        int gracePeriodOrders = 0;
        List<Order> allOrders = company.orderBook().allOrders();
        for (Order order : allOrders) {
            if (!order.isFullyDelivered()) {
                if (order.getPhase() == OrderPhase.OPEN) {
                    orders++;
                }
                if (order.getPhase() == OrderPhase.GRACE_PERIOD) {
                    gracePeriodOrders++;
                }
            }
        }

        List<MutableComponent> lines = List.of(
                Component.literal(company.name()),
                Component.translatable(
                        "display.createkontor.company_overview.balance",
                        company.account().getBalance().toString()),
                Component.translatable(
                        "display.createkontor.company_overview.open_requests",
                        company.requestBoard().openRequestsTotal()),
                Component.translatable("display.createkontor.company_overview.open_orders", orders),
                Component.translatable("display.createkontor.company_overview.grace_period_orders", gracePeriodOrders));
        int rows = Math.max(1, stats.maxRows());
        return lines.size() <= rows ? lines : lines.subList(0, rows);
    }
}
