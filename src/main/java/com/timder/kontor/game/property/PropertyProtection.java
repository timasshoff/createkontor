package com.timder.kontor.game.property;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.property.ChunkKey;
import com.timder.kontor.core.company.property.DimensionId;
import com.timder.kontor.core.company.property.PropertyIndex;
import com.timder.kontor.game.CompanySavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.timder.kontor.game.block.PaymentTerminalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class PropertyProtection {

    private static final PropertyIndex INDEX = new PropertyIndex();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        INDEX.rebuild(CompanySavedData.get(event.getServer()).getRegistry().all());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        INDEX.clear();
    }

    public static void onAcquired(ChunkKey chunk, Company company) {
        INDEX.register(chunk, company.id());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        Optional<Company> blocking = blockedBy(level, event.getPos(), player);
        if (blocking.isPresent()) {
            event.setCanceled(true);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity actor = event.getEntity();
        List<BlockPos> positions = new ArrayList<>();
        positions.add(event.getPos());
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            for (BlockSnapshot snapshot : multi.getReplacedBlockSnapshots()) {
                positions.add(snapshot.getPos());
            }
        }

        Optional<Company> firstBlocking = Optional.empty();
        for (BlockPos pos : positions) {
            Optional<Company> blocking = blockedBy(level, pos, actor);
            if (blocking.isPresent()) {
                firstBlocking = blocking;
            }
        }

        if (firstBlocking.isPresent()) {
            event.setCanceled(true);
            if (actor instanceof Player player) {
                warn(player, firstBlocking.get());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (blockedBy(level, event.getPos(), event.getEntity()).isPresent()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getEntity();
        Optional<Company> blocking = blockedByClick(level, player, event.getPos(), event.getFace());
        if (blocking.isPresent()) {
            if (level.getBlockState(event.getPos()).getBlock() instanceof PaymentTerminalBlock) {
                event.setUseItem(TriState.FALSE);
                return;
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getEntity();
        Optional<Company> blocking = blockedByClick(level, player, event.getPos(), event.getFace());
        if (blocking.isPresent()) {
            event.setCanceled(true);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Item item = event.getItemStack().getItem();
        if (!(item instanceof BucketItem || item instanceof PlaceOnWaterBlockItem)) {
            return;
        }
        Player player = event.getEntity();
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, true);
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
            return;
        }
        Optional<Company> blocking = blockedByClick(level, player, blockHit.getBlockPos(), blockHit.getDirection());
        if (blocking.isPresent()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getEntity();
        Optional<Company> blocking = blockedBy(level, event.getTarget().blockPosition(), player);
        if (blocking.isPresent()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getEntity();
        Optional<Company> blocking = blockedBy(level, event.getTarget().blockPosition(), player);
        if (blocking.isPresent()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            warn(player, blocking.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        DimensionId dimension = BiomeSampling.dimensionId(level);
        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;
        for (BlockPos pos : event.getAffectedBlocks()) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (chunkX == lastChunkX && chunkZ == lastChunkZ) {
                continue;
            }
            lastChunkX = chunkX;
            lastChunkZ = chunkZ;
            if (isProtected(level, new ChunkKey(dimension, chunkX, chunkZ))) {
                event.getAffectedBlocks().clear();
                event.getAffectedEntities().clear();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPistonPre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        DimensionId dimension = BiomeSampling.dimensionId(level);
        ChunkKey origin = new ChunkKey(
                dimension, event.getPos().getX() >> 4, event.getPos().getZ() >> 4);
        if (!isAnyHeldAround(origin)) {
            return;
        }
        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) {
            return;
        }

        Direction pushDirection = event.getPistonMoveType().isExtend
                ? event.getDirection()
                : event.getDirection().getOpposite();
        List<BlockPos> touched = new ArrayList<>();
        touched.add(event.getFaceOffsetPos());
        for (BlockPos pos : resolver.getToPush()) {
            touched.add(pos);
            touched.add(pos.relative(pushDirection));
        }
        touched.addAll(resolver.getToDestroy());

        CompanyRegistry registry = registry(level);
        for (BlockPos pos : touched) {
            ChunkKey target = new ChunkKey(dimension, pos.getX() >> 4, pos.getZ() >> 4);
            if (!INDEX.mayBeReached(origin, target, registry)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        if (isProtected(level, chunkOf(level, event.getPos()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Enemy) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (isProtected(level, chunkOf(level, entity.blockPosition()))) {
            event.setCanGrief(false);
        }
    }

    @SubscribeEvent
    public static void onEnteringSection(EntityEvent.EnteringSection event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        int oldX = SectionPos.x(event.getPackedOldPos());
        int oldZ = SectionPos.z(event.getPackedOldPos());
        int newX = SectionPos.x(event.getPackedNewPos());
        int newZ = SectionPos.z(event.getPackedNewPos());

        if (oldX == newX && oldZ == newZ) {
            return;
        }

        DimensionId dimension = BiomeSampling.dimensionId(level);
        Optional<Company> oldCompany = holderOf(level, new ChunkKey(dimension, oldX, oldZ));
        Optional<Company> newCompany = holderOf(level, new ChunkKey(dimension, newX, newZ));
        if (oldCompany.map(Company::id).equals(newCompany.map(Company::id))) {
            return;
        }

        if (newCompany.isPresent()) {
            if (newCompany.get().isMember(player.getUUID())) {
                player.displayClientMessage(Component.translatable("message.createkontor.entered_own_property"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable(
                                "message.createkontor.entered_foreign_property",
                                newCompany.get().name()),
                        true);
            }
            return;
        }
        if (oldCompany.isPresent()) {
            if (oldCompany.get().isMember(player.getUUID())) {
                player.displayClientMessage(Component.translatable("message.createkontor.left_own_property"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable(
                                "message.createkontor.left_foreign_property",
                                oldCompany.get().name()),
                        true);
            }
        }
    }

    private static ChunkKey chunkOf(ServerLevel level, BlockPos pos) {
        return new ChunkKey(BiomeSampling.dimensionId(level), pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static CompanyRegistry registry(ServerLevel level) {
        return CompanySavedData.get(level.getServer()).getRegistry();
    }

    private static boolean isProtected(ServerLevel level, ChunkKey chunk) {
        return INDEX.isIndexed(chunk) && INDEX.isProtected(chunk, registry(level));
    }

    private static boolean isAnyHeldAround(ChunkKey center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (INDEX.isIndexed(new ChunkKey(center.dimension(), center.x() + dx, center.z() + dz))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Optional<Company> blockedBy(ServerLevel level, BlockPos pos, @Nullable Entity actor) {
        ChunkKey chunk = chunkOf(level, pos);
        if (!INDEX.isIndexed(chunk)) {
            return Optional.empty();
        }
        CompanyRegistry registry = registry(level);
        boolean allowed = actor instanceof Player player
                ? INDEX.mayBeModified(chunk, player.getUUID(), registry)
                : !INDEX.isProtected(chunk, registry);
        return allowed ? Optional.empty() : INDEX.holderOf(chunk, registry);
    }

    private static Optional<Company> blockedByClick(
            ServerLevel level, Player player, BlockPos clicked, @Nullable Direction face) {
        Optional<Company> blocking = blockedBy(level, clicked, player);
        if (blocking.isEmpty() && face != null) {
            blocking = blockedBy(level, clicked.relative(face), player);
        }
        return blocking;
    }

    private static Optional<Company> holderOf(ServerLevel level, ChunkKey chunk) {
        return INDEX.isIndexed(chunk) ? INDEX.holderOf(chunk, registry(level)) : Optional.empty();
    }

    private static void warn(Player player, Company company) {
        player.displayClientMessage(
                Component.translatable("message.createkontor.protected", company.name())
                        .withStyle(ChatFormatting.DARK_RED),
                true);
    }
}
