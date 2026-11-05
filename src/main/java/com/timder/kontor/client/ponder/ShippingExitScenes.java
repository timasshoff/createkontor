package com.timder.kontor.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.timder.kontor.game.block.ShippingExitBlock;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class ShippingExitScenes {
    public static void delivery(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("shipping_exit_delivery", "Delivering Orders");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos exitPos = util.grid().at(2, 1, 2);
        BlockPos chestPos = util.grid().at(1, 1, 2);
        scene.world().showSection(util.select().position(exitPos), Direction.DOWN);
        scene.world().showSection(util.select().position(chestPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("The Shipping Exit delivers open orders by taking items from adjacent inventories.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(exitPos))
                .placeNearTarget();
        scene.idle(90);

        Selection vaultSelection =
                util.select().fromTo(util.grid().at(2, 1, 3), util.grid().at(3, 2, 4));
        scene.world().showSection(vaultSelection, Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("It will use every available adjacent inventory in every direction.")
                .attachKeyFrame()
                .pointAt(vaultSelection.getCenter())
                .placeNearTarget();
        scene.idle(90);
        scene.world().hideSection(vaultSelection, Direction.UP);

        scene.overlay()
                .showText(70)
                .text("An order is only delivered if all required items are present.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(exitPos))
                .placeNearTarget();
        scene.idle(80);

        BlockPos leverPos = util.grid().at(2, 1, 1);
        scene.world().showSection(util.select().position(leverPos), Direction.DOWN);
        scene.overlay()
                .showText(60)
                .text("A redstone pulse will check the order book and deliver as many orders as possible.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(leverPos))
                .placeNearTarget();
        scene.idle(70);

        scene.world().toggleRedstonePower(util.select().position(leverPos));
        scene.effects().indicateRedstone(leverPos);
        scene.world().modifyBlock(exitPos, state -> state.setValue(ShippingExitBlock.POWERED, true), false);
        scene.idle(10);
        scene.world().modifyBlock(exitPos, state -> state.setValue(ShippingExitBlock.DELIVERING, true), false);
        scene.idle(10);
        scene.addInstruction(s -> {
            Vec3 center = Vec3.atCenterOf(exitPos).add(0, 0.4, 0);
            s.getWorld().addParticle(new WiFiParticle.Data(), center.x, center.y, center.z, 0, 1, 0);
        });
        scene.idle(10);
        scene.world().modifyBlock(exitPos, state -> state.setValue(ShippingExitBlock.DELIVERING, false), false);
        scene.idle(10);
        scene.effects().indicateSuccess(exitPos);
        scene.idle(10);

        scene.overlay()
                .showText(70)
                .text("The required items are taken, the order is completed and the payout gets booked.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(exitPos))
                .placeNearTarget();
        scene.idle(80);

        scene.world().setBlock(leverPos, AllBlocks.PULSE_TIMER.getDefaultState(), false);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("You can use a pulse timer to regularly deliver orders.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(leverPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70).text("If you need more information about delivering orders, ask a consultant.");
    }
}
