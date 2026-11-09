package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.hud.ModularHudLayer;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import com.lowdragmc.lowdraglib2.math.interpolate.IEase;
import com.simibubi.create.AllSoundEvents;
import com.timder.kontor.config.ClientConfig;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.network.SettlementPayload;
import com.timder.kontor.game.ui.element.UiPaper;
import com.timder.kontor.game.ui.element.UiStyles;
import com.timder.kontor.util.ComponentFormatting;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

public class SettlementHud implements ModularHudLayer {

    public static final SettlementHud INSTANCE = new SettlementHud();

    private static final float START_TILT_DEGREES = 4f;
    private static final float EXIT_TILT_DEGREES = -3f;

    private static final float SLIDE_IN_SECONDS = 0.45f;
    private static final float HOLD_SECONDS = 6.25f;
    private static final float SLIDE_OUT_SECONDS = 0.35f;

    private static final float RESULT_START_SCALE = 2.0f;
    private static final float RESULT_SECONDS = 0.5f;
    private static final float REVEAL_START_SECONDS = 0.3f;
    private static final float REVEAL_STEP_SECONDS = 0.6f;
    private static final float ROLL_SECONDS = 0.5f;
    private static final float ROLL_START_SCALE = 1.35f;

    private static final int COUNT_TICKS = 6;
    private static final float COUNT_TICK_VOLUME = 0.25f;

    public static final float HIDDEN_OFFSET_PERCENT = 110f;
    public static final float MIN_LABEL_WIDTH = 40f;
    public static final int MIN_STREAK_SHOWN = 3;

    public record Hud(ModularUI ui, UIElement paper) {}

    private final Deque<SettlementPayload> queue = new ArrayDeque<>();
    private Hud current;
    private ClientLevel level;

    @Override
    public @Nullable ModularUI getModularUI() {
        dropStateIfWorldChanged();
        Minecraft minecraft = Minecraft.getInstance();
        boolean canShow = minecraft.screen == null && !minecraft.options.hideGui;
        if (current == null && !queue.isEmpty() && canShow) {
            show(queue.poll());
        }
        return current == null ? null : current.ui();
    }

    public void enqueue(SettlementPayload payload) {
        dropStateIfWorldChanged();
        if (!ClientConfig.SETTLEMENT_HUD_ENABLED.get()) {
            return;
        }
        queue.add(payload);
    }

    private void show(SettlementPayload payload) {
        Hud hud = create(payload);
        current = hud;

        hud.paper().animation(animation -> {
            UISoundUtils.playSound(AllSoundEvents.DEPOT_SLIDE.getMainEvent(), 1.1f, 0.5f);
            animation
                    .duration(SLIDE_IN_SECONDS)
                    .ease(Eases.QUART_OUT)
                    .style(
                            PropertyRegistry.TRANSFORM_2D,
                            new Transform2D().translatePercent(0, 0).rotation(0).pivot(0.5f, 0.5f))
                    .onFinished(element -> slideOut(hud))
                    .start();
        });
    }

    private void slideOut(Hud hud) {
        boolean[] soundPlayed = {false};
        hud.paper()
                .animation()
                .delay(HOLD_SECONDS)
                .duration(SLIDE_OUT_SECONDS)
                .ease(Eases.QUAD_IN)
                .style(
                        PropertyRegistry.TRANSFORM_2D,
                        new Transform2D()
                                .translatePercent(HIDDEN_OFFSET_PERCENT, 0)
                                .rotation(EXIT_TILT_DEGREES)
                                .pivot(0.5f, 0.5f))
                .onInterpolate((runtime, element) -> {
                    if (!soundPlayed[0]) {
                        soundPlayed[0] = true;
                        UISoundUtils.playSound(AllSoundEvents.DEPOT_SLIDE.getMainEvent(), 0.8f, 0.5f);
                    }
                })
                .onFinished(element -> {
                    if (current == hud) {
                        current = null;
                    }
                })
                .start();
    }

    private void dropStateIfWorldChanged() {
        ClientLevel now = Minecraft.getInstance().level;
        if (now != level) {
            queue.clear();
            current = null;
            level = now;
        }
    }

    public static Hud create(SettlementPayload payload) {
        UIElement paper = new UIElement()
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .right(8f)
                        .bottom(8f)
                        .width(145f)
                        .flexDirection(FlexDirection.COLUMN)
                        .paddingAll(8)
                        .gapAll(2))
                .style(UiStyles.PAPER)
                .transform(t -> t.translatePercent(0, HIDDEN_OFFSET_PERCENT)
                        .rotation(START_TILT_DEGREES)
                        .pivot(0.5f, 0.5f));

        Label revenueValue =
                UiPaper.primary(UiPaper.adapt(ComponentFormatting.moneyColored(payload.revenue())), Horizontal.RIGHT);
        Label costsValue = UiPaper.primary(
                UiPaper.adapt(ComponentFormatting.moneyColored(payload.costs().negate())), Horizontal.RIGHT);
        Label resultValue =
                UiPaper.primary(UiPaper.adapt(ComponentFormatting.moneyColored(payload.result())), Horizontal.RIGHT);

        boolean positiveResult = payload.result().isPositive();
        rollIn(
                revenueValue,
                payload.revenue(),
                ComponentFormatting::moneyColored,
                REVEAL_START_SECONDS,
                ROLL_SECONDS,
                ROLL_START_SCALE,
                Eases.QUAD_OUT,
                () -> playTick(positiveResult ? 1.0f : 1.498f, positiveResult));
        rollIn(
                costsValue,
                payload.costs().negate(),
                ComponentFormatting::moneyColored,
                REVEAL_START_SECONDS + REVEAL_STEP_SECONDS,
                ROLL_SECONDS,
                ROLL_START_SCALE,
                Eases.QUAD_OUT,
                () -> playTick(positiveResult ? 1.26f : 1.189f, positiveResult));

        rollIn(
                resultValue,
                payload.result(),
                ComponentFormatting::moneyColored,
                REVEAL_START_SECONDS + 2 * REVEAL_STEP_SECONDS,
                RESULT_SECONDS,
                RESULT_START_SCALE,
                Eases.QUAD_IN,
                () -> {
                    if (payload.result().isPositive()) {
                        UISoundUtils.playSound(AllSoundEvents.STOCK_TICKER_TRADE.getMainEvent(), 1.0f, 1.0f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.0f, 0.5f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.26f, 0.5f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.498f, 0.5f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 2.0f, 0.5f);
                    } else if (payload.result().isNegative()) {
                        UISoundUtils.playSound(AllSoundEvents.DENY.getMainEvent(), 0.63f, 1.0f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0f, 0.5f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.189f, 0.5f);
                        UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.498f, 0.5f);
                    }
                });

        Label bonusLabel = UiPaper.paragraphPrimary(Component.literal(" "), Horizontal.CENTER);
        bonusLabel.setVisible(false);

        if (payload.comeback()) {
            bonusLabel.setText(UiPaper.adapt(Component.translatable("hud.createkontor.settlement.comeback")
                    .withStyle(ChatFormatting.GOLD)));
        }
        if (payload.profitStreak() >= MIN_STREAK_SHOWN) {
            bonusLabel.setText(UiPaper.adapt(
                    Component.translatable("hud.createkontor.settlement.profit_streak", payload.profitStreak())
                            .withStyle(ChatFormatting.GOLD)));
        }
        if (payload.recordDay()) {
            bonusLabel.setText(UiPaper.adapt(Component.translatable("hud.createkontor.settlement.best_day")
                    .withStyle(ChatFormatting.GOLD)));
        }

        if (payload.recordDay() || payload.profitStreak() >= MIN_STREAK_SHOWN || payload.comeback()) {
            bonusLabel.transform(t -> t.scale(1.5f).pivot(0.5f, 0.5f));
            bonusLabel.animation(animation -> animation
                    .delay(REVEAL_START_SECONDS + 3 * REVEAL_STEP_SECONDS)
                    .duration(0.25f)
                    .ease(Eases.QUAD_OUT)
                    .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().pivot(0.5f, 0.5f))
                    .onInterpolate((runtime, target) -> {
                        if (!target.isVisible()) {
                            target.setVisible(true);
                            if (payload.recordDay()) {
                                UISoundUtils.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 0.5f);
                                UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 2.0f, 0.5f);
                            } else if (payload.profitStreak() >= MIN_STREAK_SHOWN) {
                                float pitch = (float)
                                        Math.pow(2.0, Math.min(payload.profitStreak() - MIN_STREAK_SHOWN, 12) / 12.0);
                                UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), pitch, 0.5f);
                            } else {
                                UISoundUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 0.6f);
                                UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.498f, 0.4f);
                            }
                        }
                    })
                    .start());
        }

        paper.addChildren(
                UiPaper.paragraphSecondary(Component.literal(payload.companyName()), Horizontal.CENTER),
                UiPaper.paragraphPrimary(
                        Component.translatable("hud.createkontor.settlement.title", payload.day()), Horizontal.CENTER),
                UiPaper.doubleDivider(),
                UiPaper.labelValue(
                        Component.translatable("hud.createkontor.settlement.revenue"), revenueValue, MIN_LABEL_WIDTH),
                UiPaper.simpleDivider(),
                UiPaper.labelValue(
                        Component.translatable("hud.createkontor.settlement.costs"), costsValue, MIN_LABEL_WIDTH),
                UiPaper.doubleDivider(),
                UiPaper.labelValue(
                        Component.translatable("hud.createkontor.settlement.result"), resultValue, MIN_LABEL_WIDTH),
                bonusLabel);

        UIElement root =
                new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        root.addChild(paper);
        return new Hud(ModularUI.of(UiStyles.ui(root)), paper);
    }

    private static void rollIn(
            Label label,
            Money target,
            Function<Money, Component> format,
            float delaySeconds,
            float durationSeconds,
            float startScale,
            IEase scaleEase,
            Runnable onReveal) {
        label.setVisible(false);
        label.transform(t -> t.scale(startScale).pivot(1f, 0.5f));
        boolean ticks = !target.isZero();
        int[] lastStep = {0};
        label.animation(animation -> animation
                .delay(delaySeconds)
                .duration(durationSeconds)
                .ease(scaleEase)
                .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().pivot(1f, 0.5f))
                .onInterpolate((runtime, element) -> {
                    if (!element.isVisible()) {
                        float finalWidth = element.getSizeWidth();
                        element.layout(layout ->
                                layout.minWidth(finalWidth)); // Stops element from changing layout during roll-in
                        element.setVisible(true);
                        onReveal.run();
                    }
                    float progress = runtime.getInterpolator().getNormalizedTime();
                    Money shown = target.scaled(Eases.QUAD_OUT.interpolate(progress));
                    double eased = Eases.QUAD_OUT.interpolate(progress);
                    label.setValue(UiPaper.adapt(format.apply(shown)));

                    if (ticks) {
                        int step = Math.min(COUNT_TICKS, (int) (eased * COUNT_TICKS + 1e-6));
                        if (step > lastStep[0]) {
                            lastStep[0] = step;
                            float pitch = (float) Math.pow(2.0, (target.isPositive() ? step : -step) / 12.0);
                            UISoundUtils.playSound(
                                    AllSoundEvents.SCROLL_VALUE.getMainEvent(), pitch, COUNT_TICK_VOLUME);
                        }
                    }
                })
                .start());
    }

    private static void playTick(float pitch, boolean positive) {
        if (positive) {
            UISoundUtils.playSound(AllSoundEvents.CONFIRM_2.getMainEvent(), pitch, 0.4f);
            UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), pitch, 0.5f);
        } else {
            UISoundUtils.playSound(AllSoundEvents.DENY.getMainEvent(), pitch * 0.63f, 0.4f);
            UISoundUtils.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), pitch, 0.6f);
        }
    }
}
