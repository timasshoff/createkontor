package com.timder.kontor.registry;

import com.timder.kontor.CreateKontor;
import com.tterrag.registrate.Registrate;
import net.neoforged.bus.api.IEventBus;

public final class KontorRegistries {

    public static final Registrate REGISTRATE = Registrate.create(CreateKontor.MODID);

    public static void register(IEventBus modEventBus) {
        KontorBlocks.touch();
        KontorBlockEntities.touch();
        KontorLanguage.touch();
        KontorCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
