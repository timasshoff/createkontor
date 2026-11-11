package com.timder.kontor.client.ponder;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.registry.KontorBlocks;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class KontorPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateKontor.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(
                        KontorBlocks.LAWYER_DESK,
                        KontorBlocks.BUYER_DESK,
                        KontorBlocks.MARKET_ANALYST_DESK,
                        KontorBlocks.CONSULTANT_DESK,
                        KontorBlocks.LOGISTICS_MANAGER_DESK,
                        KontorBlocks.FINANCES_MANAGER_DESK,
                        KontorBlocks.PROPERTY_MANAGER_DESK)
                .addStoryBoard("employee_desk/hiring", EmployeeDeskScenes::hiring)
                .addStoryBoard("employee_desk/chunk_loading", EmployeeDeskScenes::chunkLoading);

        HELPER.forComponents(KontorBlocks.SHIPPING_EXIT)
                .addStoryBoard("shipping_exit/delivery", ShippingExitScenes::delivery);

        HELPER.forComponents(KontorBlocks.PAYMENT_TERMINAL)
                .addStoryBoard("payment_terminal/payments", PaymentTerminalScenes::payments)
                .addStoryBoard("payment_terminal/signal_modes", PaymentTerminalScenes::signalModes);
    }
}
