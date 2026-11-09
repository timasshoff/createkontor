package com.timder.kontor.game.network;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.CompanyHistoryEntry;
import com.timder.kontor.core.company.financial.Money;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SettlementPayload(
        String companyName, long day, Money revenue, Money costs, Money result, boolean recordDay, int profitStreak)
        implements CustomPacketPayload {

    public static final Type<SettlementPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "settlement"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SettlementPayload> STREAM_CODEC =
            StreamCodec.of(SettlementPayload::write, SettlementPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SettlementPayload payload) {
        buf.writeUtf(payload.companyName());
        buf.writeVarLong(payload.day());
        buf.writeVarLong(payload.revenue().cents());
        buf.writeVarLong(payload.costs().cents());
        buf.writeVarLong(payload.result().cents());
        buf.writeBoolean(payload.recordDay());
        buf.writeVarInt(payload.profitStreak());
    }

    private static SettlementPayload read(RegistryFriendlyByteBuf buf) {
        return new SettlementPayload(
                buf.readUtf(),
                buf.readVarLong(),
                Money.ofCents(buf.readVarLong()),
                Money.ofCents(buf.readVarLong()),
                Money.ofCents(buf.readVarLong()),
                buf.readBoolean(),
                buf.readVarInt());
    }

    public static SettlementPayload of(
            String companyName, CompanyHistoryEntry entry, boolean recordDay, int profitStreak) {
        Money costs = Money.ZERO;
        for (Money kindCost : entry.costsByKind().values()) {
            costs = costs.plus(kindCost);
        }
        return new SettlementPayload(
                companyName, entry.day(), entry.revenue(), costs.negate(), entry.result(), recordDay, profitStreak);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
