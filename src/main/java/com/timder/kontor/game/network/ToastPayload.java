package com.timder.kontor.game.network;

import com.timder.kontor.CreateKontor;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public record ToastPayload(ToastKind kind, Component message) implements CustomPacketPayload {

    public static final Type<ToastPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToastPayload> STREAM_CODEC = StreamCodec.composite(
            ToastKind.STREAM_CODEC, ToastPayload::kind,
            ComponentSerialization.STREAM_CODEC, ToastPayload::message,
            ToastPayload::new);

    public enum ToastKind {
        ERROR("error", ChatFormatting.DARK_RED, 8000L),
        INFO("info", ChatFormatting.AQUA, 5000L),
        SUCCESS("success", ChatFormatting.GREEN, 5000L);

        public static final StreamCodec<ByteBuf, ToastKind> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(index -> values()[index], ToastKind::ordinal);

        private final String key;
        private final ChatFormatting titleColor;
        private final long displayMillis;

        ToastKind(String key, ChatFormatting titleColor, long displayMillis) {
            this.key = key;
            this.titleColor = titleColor;
            this.displayMillis = displayMillis;
        }

        public String key() { return key; }
        public ChatFormatting titleColor() { return titleColor; }
        public long displayMillis() { return displayMillis; }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(Player player, Optional<S2CActionResult> result) {
        if (result.isPresent() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ToastPayload(result.get().kind(), result.get().message()));
        }
    }

    public static void send(Player player, ToastKind kind, Component message) {
        if (message.getString().isEmpty() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PacketDistributor.sendToPlayer(serverPlayer, new ToastPayload(kind, message));
    }

    public static void sendError(Player player, Component message) {
        send(player, ToastKind.ERROR, message);
    }

    public static void sendInfo(Player player, Component message) {
        send(player, ToastKind.INFO, message);
    }

    public static void sendSuccess(Player player, Component message) {
        send(player, ToastKind.SUCCESS, message);
    }
}
