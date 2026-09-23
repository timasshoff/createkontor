package com.timder.kontor;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.data.*;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.KontorTickHandler;
import com.timder.kontor.game.command.CompanyCommands;
import com.timder.kontor.game.command.EconomyCommands;
import com.timder.kontor.game.command.MarketCommands;
import com.timder.kontor.game.command.RawMaterialCommands;
import com.timder.kontor.game.network.KontorNetwork;
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
        modEventBus.addListener(KontorNetwork::registerPayloads);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(KontorTickHandler.class);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new RawMaterialDataLoader());
            event.addListener(new GroupDefDataLoader());
            event.addListener(new MarketDefinitionDataLoader());
            event.addListener(new ProcessCostDataLoader());
            event.addListener(new LegalFormDefDataLoader());
        });

        NeoForge.EVENT_BUS.addListener(CreateKontor::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.SERVER, EconomyConfig.SPEC, "createkontor-server-economy.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, CompanyConfig.SPEC, "createkontor-server-company.toml");
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
        event.getGenerator().addProvider(
                event.includeServer(),
                new LegalFormDefDataProvider(generator.getPackOutput()));
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MarketCommands.register(event.getDispatcher());
        RawMaterialCommands.register(event.getDispatcher());
        EconomyCommands.register(event.getDispatcher());
        CompanyCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EconomySavedData.get(event.getServer());
    }
}
