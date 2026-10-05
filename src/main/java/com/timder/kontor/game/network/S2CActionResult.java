package com.timder.kontor.game.network;

import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * An action result being sent to the client and displayed as a toast.
 * Usually wrapped as an Optional.
 * Empty Optional means there is nothing to display.
 * @param kind The result kind
 * @param message The message to display
 */
public record S2CActionResult(ToastPayload.ToastKind kind, Component message) {

    public static Optional<S2CActionResult> empty() {
        return Optional.empty();
    }

    public static Optional<S2CActionResult> success(Component message) {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.SUCCESS, message));
    }

    public static Optional<S2CActionResult> error(Component message) {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.ERROR, message));
    }

    public static Optional<S2CActionResult> info(Component message) {
        return Optional.of(new S2CActionResult(ToastPayload.ToastKind.INFO, message));
    }

    public static Optional<S2CActionResult> illegalEnvironment() {
        return S2CActionResult.error(ComponentFormatting.error("Illegal Environment."));
    }
}
