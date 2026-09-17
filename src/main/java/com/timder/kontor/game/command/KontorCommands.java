package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketSnapshot;
import com.timder.kontor.core.raw.RawMaterialSnapshot;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class KontorCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("market")
                                .then(Commands.literal("info")
                                        .then(Commands.argument("product", ResourceLocationArgument.id())
                                                .executes(KontorCommands::marketInfo)))));

        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("raw")
                                .then(Commands.literal("info")
                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                .executes(KontorCommands::rawMaterialInfo)))));

        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("economy")
                                .then(Commands.literal("advance")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(KontorCommands::economyAdvance)))));
    }

    private static int marketInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId itemId = new ItemId(location.toString());

        MarketSnapshot snapshot;
        try {
            snapshot = economy.marketSnapshot(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no market for " + location));
            return 0;
        }

        String text = String.format(Locale.ROOT,
                "=== %s ===\n" +
                        "Price: %.2f (Reference-Price: %.2f, Deviation: %+.1f%%)\n" +
                        "Competitors: %d\n" +
                        "Demand: %.1f | Utilisation: %.1f%% | Overflow: %.1f",
                location,
                snapshot.displayedPrice(),
                snapshot.referenceCost(),
                snapshot.deviation() * 100.0,
                snapshot.visibleCompanies(),
                snapshot.demand(),
                snapshot.utilisation() * 100.0,
                snapshot.overflow());

        source.sendSuccess(() -> Component.literal(text), false);

        return 1;
    }

    private static int rawMaterialInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "material");
        ItemId itemId = new ItemId(location.toString());

        RawMaterialSnapshot snapshot;
        try {
            snapshot = economy.rawMaterialSnapshot(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no raw material called " + location));
            return 0;
        }

        String text = String.format(Locale.ROOT,
                "=== %s ===\nPrice: %.2f",
                location,
                snapshot.price());

        source.sendSuccess(() -> Component.literal(text), false);

        return 1;
    }

    private static int economyAdvance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        EconomySavedData data = EconomySavedData.get(source.getServer());
        Economy economy = data.getEconomy();

        int days = IntegerArgumentType.getInteger(context, "days");
        economy.advanceTicks(days * Economy.DAY_LENGTH);
        data.setDirty();

        source.sendSuccess(() -> Component.literal("Advanced the economy by " + days + " days."), false);

        return 1;
    }
}
