package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.test.TestJava;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GettingStartedWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p1")));

        var level = new TrackedDummyWorld();
        level.addBlock(new BlockPos(0, 0, 0), BlockInfo.fromBlockState(KontorBlocks.KONTOR_DESK.getDefaultState()));

        level.addBlock(new BlockPos(2, 0, 0), BlockInfo.fromBlockState(KontorBlocks.LAWYER_DESK.getDefaultState()));
        WikiUtils.addSeatedVillager(level, new BlockPos(2, 0, 1), Direction.NORTH, DyeColor.RED);

        level.addBlock(new BlockPos(-2, 0, 0), BlockInfo.fromBlockState(KontorBlocks.SHIPPING_EXIT.getDefaultState()));
        BlockState lever = Blocks.LEVER.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        level.addBlock(new BlockPos(-2, 1, 0), BlockInfo.fromBlockState(lever));
        level.addBlock(new BlockPos(-3, 0, 0), BlockInfo.fromBlockState(Blocks.CHEST.defaultBlockState()));

        page.addChild(WikiUtils.scene(level, WikiUtils.SCENE_DEFAULT_HEIGHT, false, Component.translatable("wiki.createkontor.page.getting_started.scene1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.getting_started.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p2", Money.ofDollars(CompanyConfig.FOUNDER_LOAN_IN_DOLLARS.get()).toString(), CompanyConfig.FOUNDER_LOAN_FREE_DAYS.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.getting_started.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p3")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.getting_started.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p4")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.getting_started.b5")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p6")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.getting_started.b6")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.getting_started.p7")));
    }
}
