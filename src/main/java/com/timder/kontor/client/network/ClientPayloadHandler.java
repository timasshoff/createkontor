package com.timder.kontor.client.network;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.timder.kontor.client.ui.BasicChartUI;
import com.timder.kontor.client.ui.KontorToasts;
import com.timder.kontor.game.network.ChartPayload;
import com.timder.kontor.game.network.ToastPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleChart(ChartPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new ModularUIScreen(
                BasicChartUI.create(payload.spec()),
                Component.literal(payload.spec().title())));
    }

    public static void handleToast(ToastPayload payload, IPayloadContext context) {
        KontorToasts.show(payload.kind(), payload.message());
    }
}
