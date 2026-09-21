package com.timder.kontor.game.network;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.chart.ChartSpec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChartPayload(ChartSpec spec) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChartPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "chart"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChartPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> ChartSpecCodec.write(buf, payload.spec()),
            buf -> new ChartPayload(ChartSpecCodec.read(buf)));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
