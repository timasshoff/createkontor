package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.timder.kontor.game.block.KontorDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ManagerInviteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kontorinvite")
                .then(Commands.literal("accept")
                        .then(Commands.argument("company", IntegerArgumentType.integer())
                                .then(Commands.argument("inviter", UuidArgument.uuid())
                                        .executes(ManagerInviteCommands::accept)))));
    }

    private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<S2CActionResult> result = KontorDeskBlockEntity.acceptManagerInvitation(
                player,
                IntegerArgumentType.getInteger(context, "company"),
                UuidArgument.getUuid(context, "inviter"));
        result.ifPresent(r -> player.displayClientMessage(r.message(), false));
        return result.isEmpty() ? 1 : 0;
    }
}
