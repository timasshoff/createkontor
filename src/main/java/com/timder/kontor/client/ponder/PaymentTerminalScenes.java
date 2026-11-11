package com.timder.kontor.client.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.timder.kontor.game.block.PaymentTerminalBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;

public class PaymentTerminalScenes {

    public static void payments(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("payment_terminal_payments", "Taking Payments");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos stoneBelowTerminal = util.grid().at(1, 1, 2);
        BlockPos terminalPos = util.grid().at(1, 2, 2);
        scene.world().showSection(util.select().position(stoneBelowTerminal), Direction.DOWN);
        scene.world().showSection(util.select().position(terminalPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("When placed by a company member, the Payment Terminal is bound to that company.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(terminalPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay()
                .showControls(util.vector().centerOf(terminalPos), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);
        scene.overlay()
                .showText(70)
                .text("Right-click the Payment Terminal to configure name, price and the redstone signal.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(terminalPos))
                .placeNearTarget();
        scene.idle(80);

        Selection selection =
                util.select().layersFrom(1).substract(util.select().fromTo(stoneBelowTerminal, terminalPos));
        scene.world().showSection(selection, Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showControls(util.vector().centerOf(terminalPos), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);
        scene.overlay()
                .showText(80)
                .text(
                        "When a member of another company right clicks the Payment Terminal, they will see the name, price and a payment button.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(terminalPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay()
                .showText(80)
                .text(
                        "If they decide to pay, the price will be booked from their bank account to yours and the configured redstone signal is given off.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(terminalPos))
                .placeNearTarget();
        scene.idle(90);

        BlockPos doorPos = util.grid().at(2, 1, 2);
        Selection door = util.select().fromTo(doorPos, doorPos.above());
        scene.world().modifyBlock(terminalPos, state -> state.setValue(PaymentTerminalBlock.ACTIVE, true), false);
        scene.world().modifyBlocks(door, state -> state.setValue(DoorBlock.OPEN, true), false);
        scene.effects().indicateRedstone(terminalPos);
        scene.idle(50);
        scene.world().modifyBlock(terminalPos, state -> state.setValue(PaymentTerminalBlock.ACTIVE, false), false);
        scene.world().modifyBlocks(door, state -> state.setValue(DoorBlock.OPEN, false), false);

        scene.overlay().showText(70).text("If you need more information about the Payment Gate, ask a consultant.");
    }

    public static void signalModes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("payment_terminal_signal_modes", "Signal Modes");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos continuousTerminal = util.grid().at(1, 2, 2);
        BlockPos continuousLamp = util.grid().at(1, 1, 2);
        BlockPos pulseTerminal = util.grid().at(3, 2, 2);
        BlockPos pulseLamp = util.grid().at(3, 1, 2);

        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        scene.overlay()
                .showText(80)
                .text(
                        "The signal can be configured in two modes. The length decides how long it lasts or how far the pulses are apart.")
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay()
                .showText(80)
                .text("Continuous: The signal stays on for the entire configured length.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(continuousTerminal))
                .placeNearTarget();
        scene.overlay()
                .showControls(util.vector().centerOf(continuousTerminal), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);
        scene.world()
                .modifyBlock(continuousTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, true), false);
        scene.world().modifyBlock(continuousLamp, state -> state.setValue(RedstoneLampBlock.LIT, true), false);
        scene.effects().indicateRedstone(continuousTerminal);
        scene.idle(60);
        scene.world()
                .modifyBlock(continuousTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, false), false);
        scene.world().modifyBlock(continuousLamp, state -> state.setValue(RedstoneLampBlock.LIT, false), false);
        scene.idle(20);

        scene.overlay()
                .showText(90)
                .text("Impulse: One pulse at the start and one after the configured length.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(pulseTerminal))
                .placeNearTarget();
        scene.overlay()
                .showControls(util.vector().centerOf(pulseTerminal), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);
        scene.world().modifyBlock(pulseTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, true), false);
        scene.world().modifyBlock(pulseLamp, state -> state.setValue(RedstoneLampBlock.LIT, true), false);
        scene.effects().indicateRedstone(pulseTerminal);
        scene.idle(10);
        scene.world().modifyBlock(pulseTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, false), false);
        scene.world().modifyBlock(pulseLamp, state -> state.setValue(RedstoneLampBlock.LIT, false), false);
        scene.idle(50);
        scene.world().modifyBlock(pulseTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, true), false);
        scene.world().modifyBlock(pulseLamp, state -> state.setValue(RedstoneLampBlock.LIT, true), false);
        scene.effects().indicateRedstone(pulseTerminal);
        scene.idle(10);
        scene.world().modifyBlock(pulseTerminal, state -> state.setValue(PaymentTerminalBlock.ACTIVE, false), false);
        scene.world().modifyBlock(pulseLamp, state -> state.setValue(RedstoneLampBlock.LIT, false), false);
        scene.idle(20);
    }
}
