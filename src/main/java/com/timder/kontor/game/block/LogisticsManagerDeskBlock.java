package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.LogisticsManagerUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.registry.KontorBlockEntities;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;

public class LogisticsManagerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("logistics_manager");
    public static final String RESOURCE_KEY = "logistics_manager_desk";

    public LogisticsManagerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.LOGISTICS_MANAGER_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "logistics_manager",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.logistics_manager"),
                this::buildWikiPage);
    }

    @Override
    protected CompanyBindGate bindGate() {
        return company -> company.boundResourceCount(RESOURCE_KEY) > 0
                ? Component.translatable("message.createkontor.logistics_manager_desk.limit")
                : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (LogisticsNetworkAccess.isStockLink(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.LOGISTICS_MANAGER_DESK.get();
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return LogisticsManagerUi.create(context);
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.logistics_manager.p1")));
        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.logistics_manager.b1")));

        var level = new TrackedDummyWorld();
        level.addBlock(
                new BlockPos(0, 0, 0), BlockInfo.fromBlockState(KontorBlocks.LOGISTICS_MANAGER_DESK.getDefaultState()));
        WikiUtils.addSeatedVillager(level, new BlockPos(0, 0, 1), Direction.NORTH, DyeColor.RED);

        BlockState link = AllBlocks.STOCK_LINK
                .getDefaultState()
                .setValue(PackagerLinkBlock.FACE, AttachFace.WALL)
                .setValue(PackagerLinkBlock.FACING, Direction.WEST);
        level.addBlock(new BlockPos(-1, 0, 0), BlockInfo.fromBlockState(link));

        page.addChild(WikiUtils.scene(
                level,
                WikiUtils.SCENE_DEFAULT_HEIGHT,
                false,
                Component.translatable("wiki.createkontor.page.logistics_manager.scene1")));

        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.logistics_manager.p2")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.logistics_manager.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.logistics_manager.p3",
                Money.fromDollar(CompanyConfig.STORAGE_FEE_PER_ITEM_DAY_IN_DOLLARS.get())
                        .toString())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.logistics_manager.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.logistics_manager.p4")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.logistics_manager.p5")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.logistics_manager.p6")));
    }
}
