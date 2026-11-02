package com.timder.kontor.game.network;

import com.timder.kontor.CreateKontor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NotebookPayload() implements CustomPacketPayload {

    public static final Type<NotebookPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "notebook"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotebookPayload> STREAM_CODEC =
            StreamCodec.unit(new NotebookPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
