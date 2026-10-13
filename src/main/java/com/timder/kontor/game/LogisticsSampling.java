package com.timder.kontor.game;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.logistics.LogisticsState;
import com.timder.kontor.core.company.logistics.LogisticsStorageRules;
import com.timder.kontor.core.economy.Economy;

import java.util.Objects;
import java.util.Set;

public final class LogisticsSampling {
    public static boolean sampleAll(CompanyRegistry registry, Economy economy, Set<CompanyId> activeNow) {
        Objects.requireNonNull(registry, "registry must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(activeNow, "activeNow must not be null.");

        long nowTick = economy.ticksElapsed();
        boolean sampled = false;
        for (Company company : registry.all()) {
            LogisticsState state = company.logistics();
            if (!state.isConnected() || !activeNow.contains(company.id())) {
                continue;
            }
            if (state.hasSample() && nowTick < state.lastSampleTick()) {
                continue;
            }
            long items = LogisticsNetworkAccess.totalItemCount(state.networkId());
            company.setLogistics(LogisticsStorageRules.sample(state, nowTick, items));
            sampled = true;
        }
        return sampled;
    }
}
