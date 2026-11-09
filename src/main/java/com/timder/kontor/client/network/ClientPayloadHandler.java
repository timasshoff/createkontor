package com.timder.kontor.client.network;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.timder.kontor.client.ui.BasicChartUI;
import com.timder.kontor.client.ui.KontorToasts;
import com.timder.kontor.client.ui.NotebookUi;
import com.timder.kontor.client.ui.SettlementHud;
import com.timder.kontor.game.network.ChartPayload;
import com.timder.kontor.game.network.NotebookPayload;
import com.timder.kontor.game.network.SettlementPayload;
import com.timder.kontor.game.network.ToastPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleChart(ChartPayload payload, IPayloadContext context) {
        Minecraft.getInstance()
                .setScreen(new ModularUIScreen(
                        BasicChartUI.create(payload.spec()),
                        Component.literal(payload.spec().title())));
    }

    public static void handleToast(ToastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            KontorToasts.show(payload.kind(), payload.message());
        });
    }

    public static void handleNotebook(NotebookPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
            Minecraft.getInstance()
                    .setScreen(new ModularUIScreen(
                            NotebookUi.create(payload), Component.translatable("ui.createkontor.notebook.title")));
        });
    }

    public static void handleSettlement(SettlementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SettlementHud.INSTANCE.enqueue(payload));
    }
}
