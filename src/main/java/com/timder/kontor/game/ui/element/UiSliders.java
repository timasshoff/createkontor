package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Slider;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Function;

public final class UiSliders {

    private static final long MIN_TYPING_COMMIT_DELAY_MILLIS = 1000;

    private enum Layout {
        STACKED,
        INLINE_FIELD_RIGHT,
        INLINE_FIELD_LEFT
    }

    public static UIElement labeled(float min, float max, float initial, float step, int decimals, long commitDelayMillis, Function<Float, Component> format, FloatConsumer onChange, FloatConsumer onCommit) {
        return create(Layout.STACKED, min, max, initial, step, decimals, commitDelayMillis, format, onChange, onCommit);
    }

    public static UIElement labeledInline(float min, float max, float initial, float step, int decimals, long commitDelayMillis, Function<Float, Component> format, FloatConsumer onChange, FloatConsumer onCommit) {
        return create(Layout.INLINE_FIELD_RIGHT, min, max, initial, step, decimals, commitDelayMillis, format, onChange, onCommit);
    }

    public static UIElement labeledInlineLeft(float min, float max, float initial, float step, int decimals, long commitDelayMillis, Function<Float, Component> format, FloatConsumer onChange, FloatConsumer onCommit) {
        return create(Layout.INLINE_FIELD_LEFT, min, max, initial, step, decimals, commitDelayMillis, format, onChange, onCommit);
    }

    private static UIElement create(Layout layoutType, float min, float max, float initial, float step, int decimals, long commitDelayMillis,
                                    Function<Float, Component> format, FloatConsumer onChange, FloatConsumer onCommit) {
        if (max <= min) {
            throw new IllegalArgumentException("max must be greater than min.");
        }

        Slider.Horizontal slider = new Slider.Horizontal();
        slider.setRange(min, max);
        slider.setValue(initial, false);
        if (step > 0) {
            slider.sliderStyle(style -> style.sliderStep(step / (max - min)));
        }

        TextField field = new TextField();
        field.setNumbersOnlyDouble(min, max);
        field.style(style -> style.tooltips(Component.translatable(
                "ldlib.gui.text_field.number.0", fixed(min, decimals), fixed(max, decimals))));
        field.setWheelDur(decimals, step > 0 ? step : (max - min) / 100f);
        field.setText(fixed(slider.getValue(), decimals), false);

        Label minLabel = UiLabels.secondary(format.apply(min), Horizontal.LEFT);
        Label maxLabel = UiLabels.secondary(format.apply(max), Horizontal.RIGHT);

        UIElement container;
        switch (layoutType) {
            case INLINE_FIELD_RIGHT -> {
                slider.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).minWidth(0));
                field.layout(layout -> layout.width(50).marginLeft(8));
                container = inlineRow();
                container.addChildren(minLabel, slider, maxLabel, field);
            }
            case INLINE_FIELD_LEFT -> {
                slider.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).minWidth(0));
                field.layout(layout -> layout.width(50).marginRight(8));
                container = inlineRow();
                container.addChildren(field, minLabel, slider, maxLabel);
            }
            default -> {
                slider.layout(layout -> layout.widthPercent(100));
                field.layout(layout -> layout.width(50));
                minLabel.textStyle(style -> style.adaptiveWidth(false));
                minLabel.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
                maxLabel.textStyle(style -> style.adaptiveWidth(false));
                maxLabel.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));

                UIElement labelRow = new UIElement()
                        .layout(layout -> layout
                                .flexDirection(FlexDirection.ROW)
                                .justifyContent(AlignContent.SPACE_BETWEEN)
                                .alignItems(AlignItems.CENTER)
                                .widthPercent(100));
                labelRow.addChildren(minLabel, field, maxLabel);

                container = new UIElement()
                        .layout(layout -> layout
                                .flexDirection(FlexDirection.COLUMN)
                                .gapAll(2)
                                .widthPercent(100));
                container.addChildren(slider, labelRow);
            }
        }

        long[] lastChange = { 0L };
        long[] delay = { commitDelayMillis };
        boolean[] pending = { false };

        slider.setOnValueChanged(value -> {
            float snapped = snap(value, min, max, step);
            if (snapped != value) {
                slider.setValue(snapped, false);
            }
            field.setText(fixed(snapped, decimals), false);
            onChange.accept(snapped);
            lastChange[0] = System.currentTimeMillis();
            delay[0] = commitDelayMillis;
            pending[0] = true;
        });

        field.setTextResponder(text -> {
            float parsed;
            try {
                parsed = Float.parseFloat(text);
            } catch (NumberFormatException e) {
                return;
            }
            float snapped = snap(parsed, min, max, step);
            slider.setValue(snapped, false);
            onChange.accept(snapped);
            lastChange[0] = System.currentTimeMillis();
            delay[0] = Math.max(commitDelayMillis, MIN_TYPING_COMMIT_DELAY_MILLIS);
            pending[0] = true;
        });

        field.addEventListener(UIEvents.BLUR, event -> {
            field.setText(fixed(slider.getValue(), decimals), false);
            if (pending[0]) {
                pending[0] = false;
                onCommit.accept(slider.getValue());
            }
        });

        container.addEventListener(UIEvents.TICK, event -> {
            if (pending[0] && System.currentTimeMillis() - lastChange[0] >= delay[0]) {
                pending[0] = false;
                onCommit.accept(slider.getValue());
            }
        });

        return container;
    }

    private static UIElement inlineRow() {
        return new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100));
    }

    private static float snap(float value, float min, float max, float step) {
        float clamped = Math.max(min, Math.min(max, value));
        if (step <= 0) {
            return clamped;
        }
        return Math.min(max, min + Math.round((clamped - min) / step) * step);
    }

    private static String fixed(float value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
