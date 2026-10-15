package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class UiProgressBars {

    private static final float HEIGHT = 18f;

    public static ProgressBar ofFraction(double fraction) {
        float percent = (float) (Math.max(0.0, Math.min(1.0, fraction)) * 100.0);
        ProgressBar bar = new ProgressBar();
        bar.setRange(0, 100);
        bar.setProgress(percent);
        bar.label(label -> label.setText(Component.literal(Math.round(percent) + " %").withStyle(ChatFormatting.GOLD)));
        bar.layout(layout -> layout.height(HEIGHT));
        return bar;
    }

    public static ProgressBar ofPercent(int percent) {
        return ofFraction(percent / 100.0);
    }
}
