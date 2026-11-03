package com.timder.kontor.game.network;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.Company;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NotebookPayload(
        String companyName, long day, long balanceCents, int requestsAmount, CompoundTag orderBook)
        implements CustomPacketPayload {

    public static final Type<NotebookPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "notebook"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotebookPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(Company.MAX_NAME_LENGTH),
            NotebookPayload::companyName,
            ByteBufCodecs.VAR_LONG,
            NotebookPayload::day,
            ByteBufCodecs.VAR_LONG,
            NotebookPayload::balanceCents,
            ByteBufCodecs.INT,
            NotebookPayload::requestsAmount,
            ByteBufCodecs.COMPOUND_TAG,
            NotebookPayload::orderBook,
            NotebookPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
