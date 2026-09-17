package com.timder.kontor;

import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.EconomyTickHandler;
import com.timder.kontor.game.command.KontorCommands;
import net.minecraft.resources.ResourceLocation;
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

    public static final ResourceLocation MY_UI_ID = ResourceLocation.fromNamespaceAndPath("mymod", "my_ui");

    public CreateKontor(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(EconomyTickHandler.class);

        NeoForge.EVENT_BUS.addListener(CreateKontor::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        KontorCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EconomySavedData.get(event.getServer());
    }
}
