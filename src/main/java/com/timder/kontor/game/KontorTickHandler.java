package com.timder.kontor.game;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.legalform.UpgradeEvent;
import com.timder.kontor.core.company.request.RequestArrivals;
import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.data.KontorData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
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

        long currentGameTime = server.overworld().getGameTime();
        long deltaTicks = currentGameTime - economyData.getLastSyncedGameTime();
        if (deltaTicks > 0) {
            long before = economy.ticksElapsed();
            economy.advanceTicks(deltaTicks);
            long actuallyAdvanced = economy.ticksElapsed() - before;
            economyData.setLastSyncedGameTime(economyData.getLastSyncedGameTime() + actuallyAdvanced);
        }
        boolean tradingTickPassed = economy.ticksElapsed() != ticksBefore;

        if (tradingTickPassed) {
            economyData.setDirty();
        }

        boolean orderBurstOccurred = false; // = an order has failed
        boolean upgradeEventOccurred = false;
        boolean requestArrived = false;
        if (registry.size() > 0) {
            CompanyParams companyParams = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            List<CompanyRegistry.BurstOrder> bursts = registry.advance(1, economy.currentDay());
            orderBurstOccurred = !bursts.isEmpty();
            if (orderBurstOccurred) {
                registry.applyBurstReputation(bursts, economy, companyParams);
                economyData.setDirty();
                // TODO once notifications exist: tell the company's members an order burst.
            }

            List<UpgradeEvent> upgradeEvents = registry.advanceUpgrades(1, KontorTickHandler::canProcessUpgrade, companyParams);
            upgradeEventOccurred = !upgradeEvents.isEmpty();
            if (upgradeEventOccurred) {
                PlayerNotifications.sendUpgradeEventNotifications(server, registry, upgradeEvents, companyParams);
            }

            requestArrived = letRequestsArrive(server, companyData, economy, tradingTickPassed);
        }

        if (orderBurstOccurred || requestArrived || upgradeEventOccurred || (tradingTickPassed && registry.size() > 0)) {
            companyData.setDirty();
        }

        if (economy.currentDay() != dayBefore) {
            settleCompanies(server, companyData, economy, lastHistoryDayBefore);
        }
    }

    private static boolean letRequestsArrive(MinecraftServer server, CompanySavedData companyData, Economy economy, boolean tradingTickPassed) {
        CompanyParams companyParams = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        RequestParams requestParams = EconomyConfig.toRequestParams();

        List<RequestArrivals.Arrival> arrivals = companyData.getArrivals(server).advance(companyData.getRegistry(), economy, companyParams, requestParams, 1, tradingTickPassed);
        return !arrivals.isEmpty();
    }

    public static void settleCompanies(MinecraftServer server, CompanySavedData companyData, Economy economy, long lastHistoryDayBefore) {
        if (companyData.getRegistry().size() == 0) {
            return;
        }

        List<MacroHistoryEntry> newDays = economy.macroHistorySince(lastHistoryDayBefore);
        if (newDays.isEmpty()) {
            return;
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        List<UpgradeEvent> upgradeEvents = new ArrayList<>();
        for (MacroHistoryEntry entry : newDays) {
            companyData.getRegistry().settleDay(entry.day(), entry.policyRate(), params, economy);
            upgradeEvents.addAll(companyData.getRegistry().advanceUpgradeDay(params));
        }
        if (!upgradeEvents.isEmpty()) {
            PlayerNotifications.sendUpgradeEventNotifications(server, companyData.getRegistry(), upgradeEvents, params);
        }
        companyData.setDirty();
    }

    public static long lastHistoryDay(Economy economy) {
        List<MacroHistoryEntry> history = economy.macroHistory();
        return history.isEmpty() ? -1 : history.get(history.size() - 1).day();
    }

    public static boolean canProcessUpgrade(Company company) {
        return true;
    }
}
