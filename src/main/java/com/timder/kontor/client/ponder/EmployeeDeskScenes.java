package com.timder.kontor.client.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EmployeeDeskScenes {
    public static void hiring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("employee_desk_hiring", "Hiring Employees");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos deskPos = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(deskPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("To hire an employee, place down an employee desk. The desk determines the role of the employee.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(deskPos))
                .placeNearTarget();
        scene.idle(90);

        BlockPos seatPos = util.grid().at(2, 1, 3);
        scene.world().showSection(util.select().position(seatPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("Each desk needs exactly one seat right next to it.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(seatPos))
                .placeNearTarget();
        scene.idle(80);

        ElementLink<EntityElement> villager = scene.world().createEntity(level -> {
            Villager entity = EntityType.VILLAGER.create(level);
            Vec3 pos = util.vector().of(seatPos.getX() + 0.5, seatPos.getY() + 0.5, seatPos.getZ() + 0.5);
            entity.setPos(pos.x, pos.y, pos.z);
            entity.xo = pos.x;
            entity.yo = pos.y;
            entity.zo = pos.z;
            entity.setYRot(180);
            entity.yRotO = 180;
            entity.yHeadRot = 180;
            entity.yHeadRotO = 180;
            return entity;
        });
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("Place an adult villager on the seat.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(seatPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay()
                .showText(100)
                .text(
                        "The employee is automatically hired, if it is active, has a free slot in the company, and the company can pay the hiring bonus.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(seatPos))
                .placeNearTarget();
        scene.idle(110);

        scene.overlay()
                .showControls(util.vector().topOf(seatPos), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);
        scene.overlay()
                .showText(90)
                .text(
                        "Once hired, the desk can be opened by interacting with the employee. The company pays his salary and the one-time hiring bonus automatically.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(seatPos))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay()
                .showControls(util.vector().centerOf(deskPos), Pointing.DOWN, 20)
                .withItem(AllItems.GOGGLES.asStack());
        scene.idle(30);
        scene.overlay()
                .showText(80)
                .text("Use goggles to get info about the hiring status, salary and other details about the employee.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(deskPos))
                .placeNearTarget();
        scene.idle(90);
        scene.overlay().showText(70).text("If you need more information about employees, ask a consultant.");
        scene.idle(80);

        scene.world().modifyEntity(villager, Entity::discard);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("If the seat stays empty for a few seconds, the employee is dismissed.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(seatPos))
                .placeNearTarget();
        scene.idle(90);
    }

    public static void chunkLoading(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("employee_desk_chunks", "Chunk Loading");
        scene.scaleSceneView(0.2f);
        scene.world().showSection(util.select().fromTo(16, 0, 16, 31, 1, 31), Direction.UP);
        scene.removeShadow();
        scene.idle(10);

        BlockPos deskPos = util.grid().at(24, 2, 23);
        scene.world().showSection(util.select().position(deskPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, "chunks", new AABB(16, 0, 16, 32, 16, 32), 80);
        scene.overlay()
                .showText(80)
                .text("An employee desk keeps its own chunk loaded.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(deskPos))
                .placeNearTarget();
        scene.idle(90);

        scene.world()
                .showSection(
                        util.select().everywhere().substract(util.select().fromTo(16, 0, 16, 31, 16, 31)),
                        Direction.UP);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, "chunks", new AABB(0, 0, 0, 48, 16, 48), 240);
        scene.overlay()
                .showText(80)
                .text("It also loads the eight chunks around it.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(deskPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay()
                .showText(80)
                .text("Chunks stay loaded while the company is active (= at least one company member online).")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(deskPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70).text("If you need more information about chunk loading, ask a consultant.");
        scene.idle(80);
    }
}
