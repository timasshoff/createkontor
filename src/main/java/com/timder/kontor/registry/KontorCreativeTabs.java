package com.timder.kontor.registry;

import com.timder.kontor.CreateKontor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KontorCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKontor.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createkontor"))
                    .icon(() -> new ItemStack(KontorBlocks.SHIPPING_EXIT.get()))
                    .displayItems((parameters, output) ->
                            KontorItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());
}
