package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                        .then(Commands.argument("company", IntegerArgumentType.integer(1))
                                .then(Commands.argument("inviter", UuidArgument.uuid())
                                        .then(Commands.argument("issuedAt", LongArgumentType.longArg(0))
                                                .then(Commands.argument("token", StringArgumentType.word())
                                                        .executes(ManagerInviteCommands::accept)))))));
    }

    private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<S2CActionResult> result = KontorDeskBlockEntity.acceptManagerInvitation(
                player,
                IntegerArgumentType.getInteger(context, "company"),
                UuidArgument.getUuid(context, "inviter"),
                LongArgumentType.getLong(context, "issuedAt"),
                StringArgumentType.getString(context, "token"));
        result.ifPresent(r -> player.displayClientMessage(r.message(), false));
        return result.isEmpty() ? 1 : 0;
    }
}
