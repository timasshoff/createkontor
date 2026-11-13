package com.timder.kontor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.timder.kontor.client.ponder.KontorPonderPlugin;
import com.timder.kontor.client.ui.SettlementHud;
import com.timder.kontor.config.ClientConfig;
import com.timder.kontor.registry.KontorBlockEntities;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateKontor.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateKontor.MODID, value = Dist.CLIENT)
public class CreateKontorClient {
    public CreateKontorClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "createkontor-client.toml");
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new KontorPonderPlugin());
        event.enqueueWork(() -> SimpleBlockEntityVisualizer.builder(KontorBlockEntities.POWER_OUTLET.get())
                .factory(OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF))
                .skipVanillaRender(be -> true)
                .apply());
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "settlement_slip"), SettlementHud.INSTANCE);
    }
}
