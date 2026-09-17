package com.timder.kontor;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.timder.kontor.client.TestUI;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.core.value.RecipeNode;
import com.timder.kontor.game.TestCommand;
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

import java.util.List;

@Mod(CreateKontor.MODID)
public class CreateKontor {

    public static final String MODID = "createkontor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation MY_UI_ID = ResourceLocation.fromNamespaceAndPath("mymod", "my_ui");

    public CreateKontor(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        PlayerUIMenuType.register(MY_UI_ID, player -> p -> TestUI.createModularUI());

        NeoForge.EVENT_BUS.addListener(CreateKontor::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TestCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        var server = event.getServer();
        FullRecipeGraph recipeGraph = new FullRecipeGraph(server.getRecipeManager().getRecipes());
        List<RecipeNode> t = recipeGraph.recipesFor(new ItemId("minecraft:iron_nugget"));
        for (RecipeNode n : t) {
            LOGGER.info(n.toString());
        }
    }
}
