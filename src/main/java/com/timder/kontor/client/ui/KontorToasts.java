package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.simibubi.create.AllSoundEvents;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.UiPaper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.EnumMap;
import java.util.Map;

public final class KontorToasts {

    private static final Map<ToastPayload.ToastKind, SystemToast.SystemToastId> IDS = new EnumMap<>(ToastPayload.ToastKind.class);

    static {
        for (ToastPayload.ToastKind kind : ToastPayload.ToastKind.values()) {
            IDS.put(kind, new SystemToast.SystemToastId(kind.displayMillis()));
        }
    }

    public static void show(ToastPayload.ToastKind kind, Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        Component title = Component.translatable("toast.createkontor." + kind.key() + ".title")
                .withStyle(kind.titleColor()).withStyle(ChatFormatting.BOLD);
        minecraft.getToasts().addToast(SystemToast.multiline(minecraft, IDS.get(kind), title, message));
        switch (kind) {
            case SUCCESS -> {
                UISoundUtils.playSound(AllSoundEvents.CONFIRM.getMainEvent(), 1.0f, 0.5f);
            }
            case ERROR -> UISoundUtils.playSound(AllSoundEvents.DENY.getMainEvent(), 1.0f, 0.5f);
        }
    }
}
