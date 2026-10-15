package com.timder.kontor.game.block.buyer;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.BuyerUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.registry.KontorBlockEntities;
import com.timder.kontor.registry.KontorBlocks;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BuyerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("buyer");

    public BuyerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.BUYER_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "buyer",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.buyer"),
                this::buildWikiPage);
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.BUYER_DESK.get();
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        ItemStack mainHandItem = context.player().getMainHandItem();

        if (AllBlocks.CLIPBOARD.isIn(mainHandItem)) {
            List<List<ClipboardEntry>> list = ClipboardEntry.readAll(mainHandItem);
            return BuyerUi.create(context, list);
        }
        return BuyerUi.create(context, null);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BuyerDeskBlockEntity desk) {
            desk.dropPending();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.buyer.p1",
                String.format(Locale.ROOT, "%.1f%%", CompanyConfig.PURCHASE_MARKUP.get() * 100.0))));
        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.buyer.b1")));

        var level = new TrackedDummyWorld();
        level.addBlock(new BlockPos(0, 0, 0), BlockInfo.fromBlockState(KontorBlocks.BUYER_DESK.getDefaultState()));
        WikiUtils.addSeatedVillager(level, new BlockPos(0, 0, 1), Direction.NORTH, DyeColor.RED);
        level.addBlock(
                new BlockPos(-2, 0, 0),
                BlockInfo.fromBlockState(
                        AllBlocks.PACKAGE_POSTBOXES.get(DyeColor.WHITE).getDefaultState()));

        page.addChild(WikiUtils.scene(
                level,
                WikiUtils.SCENE_DEFAULT_HEIGHT,
                false,
                Component.translatable("wiki.createkontor.page.buyer.scene1")));

        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.buyer.p2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.buyer.p3")));
    }
}
