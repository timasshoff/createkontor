package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class UiPaper {

    private static final int PRIMARY = 0x2A2418;
    private static final int SECONDARY = 0x5E5338;
    private static final int TERTIARY = 0x7A6F4C;
    private static final int ERROR = 0xA3342C;

    private static final int SIMPLE_DIVIDER = 0xFFB8A878;
    private static final int DOUBLE_DIVIDER = 0xFF2A2418;

    private static final Map<Integer, Integer> COMPONENT_PAPER_COLORS = Map.of(
            ChatFormatting.GREEN.getColor(), 0x1A6029,
            ChatFormatting.GOLD.getColor(), 0x7A4F00,
            ChatFormatting.RED.getColor(), 0xA02020,
            ChatFormatting.GRAY.getColor(), 0x5E5338);

    // LABELS

    public static Label primary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, PRIMARY, false);
    }

    public static Label secondary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, SECONDARY, false);
    }

    public static Label tertiary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, TERTIARY, false);
    }

    public static Label error(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, ERROR, false);
    }

    public static Label paragraphPrimary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, PRIMARY, true);
    }

    public static Label paragraphSecondary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, SECONDARY, true);
    }

    public static Label paragraphTertiary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, TERTIARY, true);
    }

    public static Label paragraphError(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, ERROR, true);
    }

    private static Label label(Component text, Horizontal horizontalAlignment, int color, boolean paragraph) {
        return (Label) UiLabels.label(adapt(text), horizontalAlignment, color, paragraph)
                .textStyle(style -> style.textShadow(false));
    }

    public static UIElement labelValue(Component label, Component value) {
        return labelValue(adapt(label), primary(adapt(value), Horizontal.RIGHT));
    }

    public static UIElement labelValue(Component label, Label valueLabel) {
        return UiLabels.labelValueRow(secondary(adapt(label), Horizontal.LEFT), valueLabel, true);
    }

    public static UIElement labelValueNoWrap(Component label, Component value) {
        return UiLabels.labelValueRow(
                secondary(adapt(label), Horizontal.LEFT), primary(adapt(value), Horizontal.RIGHT), false);
    }

    public static Component adapt(Component text) {
        MutableComponent result = Component.empty();
        text.visit(
                (style, content) -> {
                    TextColor color = style.getColor();
                    Integer ink = color == null ? null : COMPONENT_PAPER_COLORS.get(color.getValue());
                    Style adapted = ink == null ? style : style.withColor(ink);
                    result.append(Component.literal(content).withStyle(adapted));
                    return Optional.empty();
                },
                Style.EMPTY);
        return result;
    }

    // BUTTONS

    public static Button button(Component text) {
        return (Button) new Button().setText(text).addClass("kontor-paper").layout(layout -> layout.height(20));
    }

    // ITEM SLOTS

    public static ItemSlot itemSlot() {
        return (ItemSlot) new ItemSlot().addClass("kontor-paper");
    }

    // DIVIDERS

    public static UIElement simpleDivider() {
        return new UIElement()
                .layout(layout -> layout.widthPercent(100).height(1).marginVertical(2))
                .style(style -> style.background(new ColorRectTexture(SIMPLE_DIVIDER)));
    }

    public static UIElement doubleDivider() {
        UIElement rule = new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.COLUMN)
                        .widthPercent(100)
                        .gapAll(1)
                        .marginVertical(2));
        rule.addChild(new UIElement()
                .layout(layout -> layout.widthPercent(100).height(1))
                .style(style -> style.background(new ColorRectTexture(DOUBLE_DIVIDER))));
        rule.addChild(new UIElement()
                .layout(layout -> layout.widthPercent(100).height(1))
                .style(style -> style.background(new ColorRectTexture(DOUBLE_DIVIDER))));
        return rule;
    }
}
