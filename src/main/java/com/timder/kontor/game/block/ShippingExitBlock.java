package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.ui.element.WikiElements;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.registry.KontorBlockEntities;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ShippingExitBlock extends AbstractCompanyBlock implements IBE<ShippingExitBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String RESOURCE_KEY = "shipping_exit";

    public ShippingExitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
        ConsultantWiki.register("shipping_exit", WikiCategory.BLOCKS, Component.translatable("block.createkontor.shipping_exit"), this::buildWikiPage);
    }

    @Override
    protected CompanyBindGate bindGate() {
        return company -> {
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            LegalFormDef legalForm = company.legalForm(params);
            if (legalForm.allowsMoreShippingExits(company.boundResourceCount(RESOURCE_KEY))) {
                return null;
            }
            return Component.translatable("message.createkontor.shipping_exit.limit", ComponentFormatting.highlightError(String.valueOf(legalForm.maxShippingExits())));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public Class<ShippingExitBlockEntity> getBlockEntityClass() {
        return ShippingExitBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ShippingExitBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.SHIPPING_EXIT.get();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getBlockEntityOptional(level, pos).map(pbe -> pbe.isSignallingDelivery() ? 15 : 0)
                .orElse(0);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) {
            return;
        }

        boolean wasPowered = state.getValue(POWERED);
        boolean isPowered = level.hasNeighborSignal(pos);
        if (isPowered == wasPowered) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, isPowered), Block.UPDATE_CLIENTS);

        if (isPowered && level.getBlockEntity(pos) instanceof ShippingExitBlockEntity blockEntity) {
            blockEntity.onRedstonePulse();
        }
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiElements.blockScene(defaultBlockState()));
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.shipping_exit.p1")));

        var level = new TrackedDummyWorld();
        level.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(defaultBlockState()));
        level.addBlock(new BlockPos(1, 0, 0), BlockInfo.fromBlockState(Blocks.CHEST.defaultBlockState()));
        level.addBlock(new BlockPos(-1, 0, 0), BlockInfo.fromBlockState(AllBlocks.PULSE_TIMER.getDefaultState().rotate(Rotation.COUNTERCLOCKWISE_90)));
        page.addChild(WikiElements.scene(level, WikiElements.SCENE_DEFAULT_HEIGHT, false, Component.translatable("wiki.createkontor.page.shipping_exit.scene1")));

        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.shipping_exit.p2")));
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.shipping_exit.p3")));
    }
}
