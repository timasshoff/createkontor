package com.timder.kontor.game;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.client.TestUI;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

public class TestCommand {
    // Define and register the command structure
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("hello") // The base command name: /hello
                        .requires(source -> source.hasPermission(0)) // Permission level (0 = everyone, 2 = OP/Admin)
                        .executes(TestCommand::run) // The method that runs when executed
        );
    }

    // The execution logic
    private static int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // Build your UI
        PlayerUIMenuType.openUI(source.getPlayer(), CreateKontor.MY_UI_ID);

        // Send a message back to the player/source executing it
        source.sendSuccess(() -> Component.literal("Hello from your custom NeoForge command!"), false);

        return 1; // Return 1 indicates a successful execution
    }
}
