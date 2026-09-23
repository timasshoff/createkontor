package com.timder.kontor.game;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.LegalForms;
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
        if (registry.size() > 0) {
            orderBurstOccurred = !registry.advance(1, economy.currentDay()).isEmpty();
            // TODO once Economy.registerParticipant has a caller: apply ReputationRules.changeFailed for each burst.
            // TODO once notifications exist: tell the company's members an order burst.
        }

        if (orderBurstOccurred || (tradingTickPassed && registry.size() > 0)) {
            companyData.setDirty();
        }

        if (economy.currentDay() != dayBefore) {
            settleCompanies(companyData, economy, lastHistoryDayBefore);
        }
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
