package com.timder.kontor.game;

import com.timder.kontor.core.economy.Economy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EconomyTickHandler {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EconomySavedData data = EconomySavedData.get(event.getServer());
        Economy economy = data.getEconomy();

        long before = economy.ticksElapsed();
        economy.advanceTo(event.getServer().overworld().getGameTime());

        if (economy.ticksElapsed() != before) {
            // Only write to disk if economy actually advanced
            data.setDirty();
        }
    }
}
