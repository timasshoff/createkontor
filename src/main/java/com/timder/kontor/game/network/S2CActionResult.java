package com.timder.kontor.game.network;

import com.timder.kontor.util.ComponentFormatting;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * An action result being sent to the client and displayed as a toast.
 * Usually wrapped as an Optional.
 * Empty Optional means there is nothing to display.
 * @param kind The result kind
 * @param message The message to display
 */
public record S2CActionResult(ToastPayload.ToastKind kind, Component message, boolean silent) {

    public static Optional<S2CActionResult> empty() {
        return Optional.empty();
    }

    public static Optional<S2CActionResult> success(Component message) {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.SUCCESS, message, false));
    }

    public static Optional<S2CActionResult> silentSuccess() {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.SUCCESS, Component.empty(), true));
    }

    public static Optional<S2CActionResult> error(Component message) {
        return Optional.of(
                new S2CActionResult(ToastPayload.ToastKind.ERROR, message.copy().withStyle(ChatFormatting.RED), false));
    }

    public static Optional<S2CActionResult> info(Component message) {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.INFO, message, false));
    }

    public static Optional<S2CActionResult> illegalEnvironment() {
        return S2CActionResult.error(ComponentFormatting.error("Illegal Environment."));
    }
}
