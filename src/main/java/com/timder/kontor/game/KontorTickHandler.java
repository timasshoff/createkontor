package com.timder.kontor.game;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
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

        long ticksBefore = economy.ticksElapsed();
        long dayBefore = economy.currentDay();

        economy.advanceTo(server.overworld().getGameTime());

        if (economy.ticksElapsed() != ticksBefore) {
            // Only write to disk if the economy actually advanced
            economyData.setDirty();
        }

        if (economy.currentDay() != dayBefore) {
            settleCompanies(server, economy, dayBefore);
        }
    }

    private static void settleCompanies(MinecraftServer server, Economy economy, long dayBefore) {
        CompanySavedData companyData = CompanySavedData.get(server);
        if (companyData.getRegistry().size() == 0) {
            return; // No companies to settle
        }

        List<MacroHistoryEntry> newDays = economy.macroHistory().stream() // In case we advanced multiple days
                .filter(entry -> entry.day() > dayBefore)
                .toList();
        if (newDays.isEmpty()) {
            return;
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (MacroHistoryEntry entry : newDays) {
            companyData.getRegistry().settleDay(entry.day(), entry.policyRate(), params);
        }
        companyData.setDirty();
    }
}
