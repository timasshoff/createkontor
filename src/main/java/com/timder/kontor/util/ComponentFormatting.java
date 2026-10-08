package com.timder.kontor.util;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.market.MarketRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class ComponentFormatting {

    public static final ChatFormatting DEFAULT = ChatFormatting.GRAY;
    public static final ChatFormatting DEFAULT_HIGHLIGHTED = ChatFormatting.GOLD;
    public static final ChatFormatting ERROR = ChatFormatting.RED;
    public static final ChatFormatting ERROR_HIGHLIGHTED = ChatFormatting.DARK_RED;

    public static final ChatFormatting MONEY_POSITIVE = ChatFormatting.GREEN;
    public static final ChatFormatting MONEY_NEUTRAL = ChatFormatting.GRAY;
    public static final ChatFormatting MONEY_NEGATIVE = ChatFormatting.RED;

    public static final ChatFormatting STAR_FILLED = ChatFormatting.GOLD;
    public static final ChatFormatting STAR_EMPTY = ChatFormatting.DARK_GRAY;
    public static final int MAX_STARS = 5;

    private static final String FILLED_STAR = "\u2605";
    private static final String EMPTY_STAR = "\u2606";

    public static Component standardTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(DEFAULT);
    }

    public static Component standard(String text) {
        return Component.literal(text).withStyle(DEFAULT);
    }

    public static Component highlightStandardTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(DEFAULT_HIGHLIGHTED);
    }

    public static Component highlightStandard(String text) {
        return Component.literal(text).withStyle(DEFAULT_HIGHLIGHTED);
    }

    public static Component errorTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(ERROR);
    }

    public static Component error(String text) {
        return Component.literal(text).withStyle(ERROR);
    }

    public static Component highlightErrorTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(ERROR_HIGHLIGHTED);
    }

    public static Component highlightError(String text) {
        return Component.literal(text).withStyle(ERROR_HIGHLIGHTED);
    }

    public static Component moneyColored(Money money) {
        if (money.isPositive()) {
            return Component.literal(money.toString()).withStyle(MONEY_POSITIVE);
        }
        if (money.isZero()) {
            return Component.literal(money.toString()).withStyle(MONEY_NEUTRAL);
        }
        return Component.literal(money.toString()).withStyle(MONEY_NEGATIVE);
    }

    public static Component moneyPositive(Money money) {
        return Component.literal(money.toString()).withStyle(MONEY_POSITIVE);
    }

    public static Component moneyNegative(Money money) {
        return Component.literal(money.toString()).withStyle(MONEY_NEGATIVE);
    }

    public static Component percentColored(double fraction) {
        if (!Double.isFinite(fraction)) {
            return Component.literal("Inf.");
        }
        double percent = BigDecimal.valueOf(fraction * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
        String text = String.format(Locale.ROOT, "%.2f%%", percent);
        if (percent > 0.0) {
            return Component.literal("+" + text).withStyle(MONEY_POSITIVE);
        }
        if (percent == 0.0) {
            return Component.literal(text ).withStyle(MONEY_NEUTRAL);
        }
        return Component.literal(text ).withStyle(MONEY_NEGATIVE);
    }

    public static Component day(long day) {
        return Component.translatable("economy.createkontor.current_day", Component.literal(Long.toString(day)).withStyle(ChatFormatting.GOLD));
    }

    public static MutableComponent ticks(long ticks) {
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String s = hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds) + " h"
                : String.format("%d:%02d", minutes, seconds) + " min";
        return Component.literal(s);
    }

    public static MutableComponent check(boolean met) {
        return met
                ? Component.literal("\u2713")
                : Component.literal("\u2717");
    }

    public static MutableComponent progress(String actual, String required, boolean met) {
        return Component.empty()
                .append(Component.literal(actual).withStyle(met ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(required));
    }

    public static int filledStars(double stars) {
        if (!Double.isFinite(stars)) {
            return 0;
        }
        return (int) Math.max(0L, Math.min(MAX_STARS, Math.round(stars)));
    }

    public static MutableComponent stars(double stars) {
        int filled = filledStars(stars);
        return Component.literal(FILLED_STAR.repeat(filled)).withStyle(STAR_FILLED)
                .append(Component.literal(EMPTY_STAR.repeat(MAX_STARS - filled)).withStyle(STAR_EMPTY));
    }

    public static MutableComponent starsWithValue(double stars) {
        String value = Double.isFinite(stars) ? String.format(Locale.ROOT, "%.1f", stars) : "-";
        return stars(stars).append(Component.literal(" " + value).withStyle(DEFAULT));
    }
}
