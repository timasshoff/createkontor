package com.timder.kontor.util;

import com.timder.kontor.core.company.financial.Money;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ComponentFormatting {

    public static final ChatFormatting DEFAULT = ChatFormatting.GRAY;
    public static final ChatFormatting DEFAULT_HIGHLIGHTED = ChatFormatting.GOLD;
    public static final ChatFormatting ERROR = ChatFormatting.RED;
    public static final ChatFormatting ERROR_HIGHLIGHTED = ChatFormatting.DARK_RED;

    public static final ChatFormatting MONEY_POSITIVE = ChatFormatting.GREEN;
    public static final ChatFormatting MONEY_NEUTRAL = ChatFormatting.GRAY;
    public static final ChatFormatting MONEY_NEGATIVE = ChatFormatting.RED;

    public static Component defaultComponentTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(DEFAULT);
    }

    public static Component defaultComponent(String text) {
        return Component.literal(text).withStyle(DEFAULT);
    }

    public static Component highlightDefaultComponentTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(DEFAULT_HIGHLIGHTED);
    }

    public static Component highlightDefaultComponent(String text) {
        return Component.literal(text).withStyle(DEFAULT_HIGHLIGHTED);
    }

    public static Component errorComponentTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(ERROR);
    }

    public static Component errorComponent(String text) {
        return Component.literal(text).withStyle(ERROR);
    }

    public static Component highlightErrorComponentTranslatable(String translationKey) {
        return Component.translatable(translationKey).withStyle(ERROR_HIGHLIGHTED);
    }

    public static Component highlightErrorComponent(String text) {
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

}
