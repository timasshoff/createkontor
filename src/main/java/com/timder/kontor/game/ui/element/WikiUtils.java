package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.simibubi.create.AllBlocks;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public final class WikiUtils {

    public static final float SCENE_DEFAULT_HEIGHT = 100;
    public static final float SCENE_DEFAULT_WIDTH_PERCENT = 75;

    private static final double SEAT_RIDER_OFFSET_Y = 1.0 / 16 + 0.35;

    public static UIElement paragraph(Component component) {
        return new Label()
                .setText(component)
                .textStyle(style -> style.textColor(0xAAAAAA)
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true))
                .layout(layout -> layout.widthPercent(100));
    }

    public static UIElement secondaryParagraph(Component component) {
        return new Label()
                .setText(component)
                .textStyle(style -> style.textColor(0x555555)
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true))
                .layout(layout -> layout.widthPercent(100));
    }

    public static UIElement heading(Component component) {
        return new Label()
                .setText(Component.empty().append(component).withStyle(ChatFormatting.BOLD))
                .textStyle(style -> style.textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true))
                .layout(layout -> layout.widthPercent(100));
    }

    public static UIElement bulletList(Component component) {
        UIElement list =
                new UIElement().layout(layout -> layout.widthPercent(100).gapAll(2));
        for (String item : component.getString().split("\n")) {
            if (item.isBlank()) {
                continue;
            }
            UIElement row = new UIElement()
                    .layout(layout -> layout.widthPercent(100)
                            .flexDirection(FlexDirection.ROW)
                            .gapAll(4));
            UIElement bullet = new Label()
                    .setText(Component.literal("•"))
                    .textStyle(style ->
                            style.adaptiveWidth(true).adaptiveHeight(true).textColor(0xAAAAAA));
            UIElement text = paragraph(Component.literal(item.trim()))
                    .layout(layout -> layout.widthAuto()
                            .flexBasis(0)
                            .flexGrow(1)
                            .flexShrink(1)
                            .minWidth(0));
            row.addChildren(bullet, text);
            list.addChild(row);
        }
        return list;
    }

    public static UIElement employeeScene(BlockState state) {
        if (!LDLib2.isClient()) {
            return new UIElement();
        }

        var level = new TrackedDummyWorld();
        level.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(state));
        addSeatedVillager(level, new BlockPos(0, 0, 1), Direction.NORTH, DyeColor.RED);

        var scene = new Scene();
        scene.setRenderFacing(false);
        scene.setZoom(0.5f);
        scene.setRenderSelect(false);
        scene.setScalable(false);
        scene.setShowHoverBlockTips(true);
        scene.xeiLookup();
        scene.createScene(level);
        var blocks = level.getFilledBlocks().longStream().mapToObj(BlockPos::of).toList();
        scene.setRenderedCore(blocks);
        scene.setTickWorld(false);
        scene.layout(layout -> layout.widthPercent(SCENE_DEFAULT_WIDTH_PERCENT)
                .height(SCENE_DEFAULT_HEIGHT)
                .paddingAll(3)
                .alignSelf(AlignItems.CENTER));
        return scene;
    }

    public static UIElement blockScene(BlockState state) {
        if (!LDLib2.isClient()) {
            return new UIElement();
        }

        var level = new TrackedDummyWorld();
        level.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(state));

        var scene = new Scene();
        scene.setRenderFacing(false);
        scene.setRenderSelect(false);
        scene.setScalable(false);
        scene.setShowHoverBlockTips(true);
        scene.xeiLookup();
        scene.createScene(level);
        scene.setRenderedCore(Collections.singleton(BlockPos.ZERO), null);
        scene.setTickWorld(false);
        scene.layout(layout -> layout.widthPercent(SCENE_DEFAULT_WIDTH_PERCENT)
                .height(SCENE_DEFAULT_HEIGHT)
                .paddingAll(3)
                .alignSelf(AlignItems.CENTER));
        return scene;
    }

    public static UIElement scene(TrackedDummyWorld level, float height, boolean tickWorld) {
        if (!LDLib2.isClient()) {
            return new UIElement();
        }

        var scene = new Scene();
        scene.setRenderFacing(false);
        scene.setRenderSelect(false);
        scene.setScalable(true);
        scene.setShowHoverBlockTips(true);
        scene.xeiLookup();
        scene.createScene(level);
        var blocks = level.getFilledBlocks().longStream().mapToObj(BlockPos::of).toList();
        scene.setRenderedCore(blocks);
        scene.setTickWorld(tickWorld);
        scene.layout(layout -> layout.widthPercent(SCENE_DEFAULT_WIDTH_PERCENT)
                .height(height)
                .paddingAll(3)
                .alignSelf(AlignItems.CENTER));
        return scene;
    }

    public static UIElement scene(TrackedDummyWorld level, float widthPercent, float height, boolean tickWorld) {
        if (!LDLib2.isClient()) {
            return new UIElement();
        }

        var scene = new Scene();
        scene.setRenderFacing(false);
        scene.setRenderSelect(false);
        scene.setScalable(true);
        scene.setShowHoverBlockTips(true);
        scene.xeiLookup();
        scene.createScene(level);
        var blocks = level.getFilledBlocks().longStream().mapToObj(BlockPos::of).toList();
        scene.setRenderedCore(blocks);
        scene.setTickWorld(tickWorld);
        scene.layout(layout ->
                layout.widthPercent(widthPercent).height(height).paddingAll(3).alignSelf(AlignItems.CENTER));
        return scene;
    }

    public static UIElement scene(TrackedDummyWorld level, float height, boolean tickWorld, Component caption) {
        Scene scene = (Scene) scene(level, 100, height, tickWorld);
        if (LDLib2.isClient()) {
            scene.style(UiStyles.CARD);
        }
        return new UIElement()
                .layout(layout -> layout.widthPercent(SCENE_DEFAULT_WIDTH_PERCENT)
                        .alignSelf(AlignItems.CENTER)
                        .gapAll(2))
                .addChildren(scene, secondaryParagraph(caption));
    }

    public static void addSeatedVillager(TrackedDummyWorld level, BlockPos seatPos, Direction facing, DyeColor color) {
        level.addBlock(
                seatPos, BlockInfo.fromBlockState(AllBlocks.SEATS.get(color).getDefaultState()));

        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return;
        }
        villager.setPos(seatPos.getX() + 0.5, seatPos.getY() + SEAT_RIDER_OFFSET_Y, seatPos.getZ() + 0.5);

        float yaw = facing.toYRot();
        villager.setYRot(yaw);
        villager.yRotO = yaw;
        villager.yBodyRot = yaw;
        villager.yBodyRotO = yaw;
        villager.yHeadRot = yaw;
        villager.yHeadRotO = yaw;

        villager.setNoGravity(true);
        level.addEntity(villager);
    }
}
