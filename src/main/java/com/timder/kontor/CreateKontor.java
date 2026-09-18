package com.timder.kontor;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.data.*;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.EconomyTickHandler;
import com.timder.kontor.game.command.KontorCommands;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(CreateKontor.MODID)
public class CreateKontor {

    public static final String MODID = "createkontor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateKontor(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(EconomyTickHandler.class);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new RawMaterialDataLoader());
            event.addListener(new GroupDefDataLoader());
            event.addListener(new MarketDefinitionDataLoader());
            event.addListener(new ProcessCostDataLoader());
        });

        NeoForge.EVENT_BUS.addListener(CreateKontor::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.SERVER, EconomyConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        event.getGenerator().addProvider(
                event.includeServer(),
                new RawMaterialDataProvider(generator.getPackOutput()));
        event.getGenerator().addProvider(
                event.includeServer(),
                new GroupDefDataProvider(generator.getPackOutput()));
        event.getGenerator().addProvider(
                event.includeServer(),
                new MarketDefinitionDataProvider(generator.getPackOutput()));
        event.getGenerator().addProvider(
                event.includeServer(),
                new ProcessCostDataProvider(generator.getPackOutput()));
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        KontorCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EconomySavedData.get(event.getServer());
    }
}
