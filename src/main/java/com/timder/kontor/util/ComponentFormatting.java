package com.timder.kontor.util;

import com.timder.kontor.core.company.financial.Money;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ComponentFormatting {

    public static final ChatFormatting DEFAULT = ChatFormatting.GRAY;
    public static final ChatFormatting DEFAULT_HIGHLIGHTED = ChatFormatting.GOLD;
    public static final ChatFormatting ERROR = ChatFormatting.RED;
    public static final ChatFormatting ERROR_HIGHLIGHTED = ChatFormatting.DARK_RED;

    public static final ChatFormatting MONEY_POSITIVE = ChatFormatting.GREEN;
    public static final ChatFormatting MONEY_NEUTRAL = ChatFormatting.GRAY;
    public static final ChatFormatting MONEY_NEGATIVE = ChatFormatting.RED;

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

}
