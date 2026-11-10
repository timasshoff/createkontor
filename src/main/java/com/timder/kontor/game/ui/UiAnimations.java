package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import com.simibubi.create.AllSoundEvents;
import com.timder.kontor.core.company.financial.Money;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class UiAnimations {

    private static final float ROLL_START_SCALE = 1.3f;
    private static final float ROLL_SECONDS = 0.5f;
    private static final int COUNT_TICKS = 6;
    private static final float COUNT_TICK_VOLUME = 0.25f;

    public static void rollValue(
            Label label, Money from, Money target, Function<Money, Component> format, Runnable onReveal) {
        long delta = target.cents() - from.cents();
        boolean positive = delta > 0;
        boolean[] revealed = {false};
        int[] lastStep = {0};

        label.transform(t -> t.scale(ROLL_START_SCALE).pivot(0.5f, 0.5f));
        label.animation()
                .duration(ROLL_SECONDS)
                .ease(Eases.QUAD_OUT)
                .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().pivot(0.5f, 0.5f))
                .onInterpolate((runtime, element) -> {
                    if (positive && !revealed[0]) {
                        revealed[0] = true;
                        onReveal.run();
                    }
                    double eased =
                            Eases.QUAD_OUT.interpolate(runtime.getInterpolator().getNormalizedTime());
                    label.setText(format.apply(Money.ofCents(from.cents() + Math.round(delta * eased))));

                    int step = Math.min(COUNT_TICKS, (int) (eased * COUNT_TICKS + 1e-6));
                    if (step > lastStep[0]) {
                        lastStep[0] = step;
                        float pitch = (float) Math.pow(2.0, (positive ? step : -step) / 12.0);
                        UISoundUtils.playSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), pitch, COUNT_TICK_VOLUME);
                    }
                })
                .onFinished(element -> label.setText(format.apply(target)))
                .start();
    }
}
