package com.timder.kontor.registry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.block.buyer.BuyerDeskPortTarget;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KontorPackagePortTargets {

    private static final DeferredRegister<PackagePortTargetType> REGISTER = DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, CreateKontor.MODID);

    public static final Holder<PackagePortTargetType> BUYER_DESK = REGISTER.register("buyer_desk", BuyerDeskPortTarget.Type::new);

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
