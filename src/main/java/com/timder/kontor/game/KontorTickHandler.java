package com.timder.kontor.game;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.core.company.request.RequestArrivals;
import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.data.KontorData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

public class KontorTickHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        EconomySavedData economyData = EconomySavedData.get(server);
        Economy economy = economyData.getEconomy();
        CompanySavedData companyData = CompanySavedData.get(server);
        CompanyRegistry registry = companyData.getRegistry();

        long ticksBefore = economy.ticksElapsed();
        long dayBefore = economy.currentDay();
        long lastHistoryDayBefore = lastHistoryDay(economy);

        economy.advanceTo(server.overworld().getGameTime());
        boolean tradingTickPassed = economy.ticksElapsed() != ticksBefore;

        if (tradingTickPassed) {
            economyData.setDirty();
        }

        boolean orderBurstOccurred = false; // = an order has failed
        boolean requestArrived = false;
        if (registry.size() > 0) {
            List<CompanyRegistry.BurstOrder> bursts = registry.advance(1, economy.currentDay());
            orderBurstOccurred = !bursts.isEmpty();
            if (orderBurstOccurred) {
                applyBurstReputation(bursts, registry, economy);
                economyData.setDirty();
                // TODO once notifications exist: tell the company's members an order burst.
            }

            requestArrived = letRequestsArrive(server, companyData, economy, tradingTickPassed);
        }

        if (orderBurstOccurred || requestArrived || (tradingTickPassed && registry.size() > 0)) {
            companyData.setDirty();
        }

        if (economy.currentDay() != dayBefore) {
            settleCompanies(companyData, economy, lastHistoryDayBefore);
        }
    }

    private static void applyBurstReputation(List<CompanyRegistry.BurstOrder> bursts, CompanyRegistry registry, Economy economy) {
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (CompanyRegistry.BurstOrder burst : bursts) {
            Company company = registry.get(burst.companyId()).orElseThrow();
            economy.recordOrderFailed(burst.order().getProduct(), burst.companyId(), burst.order(), company.legalForm(params));
        }
    }

    private static boolean letRequestsArrive(MinecraftServer server, CompanySavedData companyData, Economy economy, boolean tradingTickPassed) {
        CompanyParams companyParams = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        RequestParams requestParams = EconomyConfig.toRequestParams();

        List<RequestArrivals.Arrival> arrivals = companyData.getArrivals(server).advance(companyData.getRegistry(), economy, companyParams, requestParams, 1, tradingTickPassed);
        return !arrivals.isEmpty();
    }

    public static void settleCompanies(CompanySavedData companyData, Economy economy, long lastHistoryDayBefore) {
        if (companyData.getRegistry().size() == 0) {
            return;
        }

        List<MacroHistoryEntry> newDays = economy.macroHistorySince(lastHistoryDayBefore);
        if (newDays.isEmpty()) {
            return;
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (MacroHistoryEntry entry : newDays) {
            companyData.getRegistry().settleDay(entry.day(), entry.policyRate(), params);
        }
        companyData.setDirty();
    }

    public static long lastHistoryDay(Economy economy) {
        List<MacroHistoryEntry> history = economy.macroHistory();
        return history.isEmpty() ? -1 : history.get(history.size() - 1).day();
    }
}
