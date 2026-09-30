package com.timder.kontor.util;

import com.timder.kontor.core.company.license.LicenseKey;
import com.timder.kontor.core.value.ItemId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Optional;

public final class LicenseNames {

    public static final String GENERATED_TRANSLATION_KEY = "license.createkontor.generated";

    public static MutableComponent of(LicenseKey key) {
        return switch (key) {
            case LicenseKey.Defined defined -> Component.translatable(defined.translationKey());
            case LicenseKey.Generated generated -> Component.translatable(GENERATED_TRANSLATION_KEY, itemName(generated.market()));
        };
    }

    private static Component itemName(ItemId market) {
        ResourceLocation location = ResourceLocation.tryParse(market.value());
        if (location != null) {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(location);
            if (item.isPresent()) {
                return item.get().getDescription();
            }
        }
        return Component.literal(market.value());
    }
}
