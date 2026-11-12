package com.timder.kontor.game.chunk;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.property.ChunkKey;
import com.timder.kontor.core.company.property.PropertyHolding;
import com.timder.kontor.game.block.company.CompanyBound;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class KontorChunkLoading {

    public static final int RADIUS = 1;

    private static final Map<CompanyId, Set<ChunkKey>> forcedHeldChunks = new HashMap<>();

    public static final TicketController DESK_CONTROLLER = new TicketController(
            ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "company_desks"),
            KontorChunkLoading::removeAllTickets);

    public static final TicketController HELD_CONTROLLER = new TicketController(
            ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, "held_chunks"),
            KontorChunkLoading::removeAllTickets);

    private static final Set<CompanyId> loadedCompanies = new HashSet<>();

    public static void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(DESK_CONTROLLER);
        event.register(HELD_CONTROLLER);
    }

    private static void removeAllTickets(ServerLevel level, TicketHelper helper) {
        for (BlockPos owner : helper.getBlockTickets().keySet()) {
            helper.removeAllTickets(owner);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        loadedCompanies.clear();
        forcedHeldChunks.clear();
    }

    public static void update(MinecraftServer server, CompanyRegistry registry, Set<CompanyId> activeNow) {
        DeskLocationsSavedData data = DeskLocationsSavedData.get(server);

        for (CompanyId company : activeNow) {
            if (loadedCompanies.add(company)) {
                loadDesks(server, data, company);
                refreshHeld(server, registry, company);
            }
        }

        Iterator<CompanyId> iterator = loadedCompanies.iterator();
        while (iterator.hasNext()) {
            CompanyId company = iterator.next();
            if (!activeNow.contains(company)) {
                iterator.remove();
                unloadDesks(server, data, company);
                refreshHeld(server, registry, company);
                if (registry.get(company).isEmpty()) {
                    data.removeCompany(company);
                }
            }
        }
    }

    public static void deskBound(
            ServerLevel level, BlockPos pos, @Nullable CompanyId oldCompany, @Nullable CompanyId newCompany) {
        DeskLocationsSavedData data = DeskLocationsSavedData.get(level.getServer());
        DeskLocationsSavedData.DeskLocation location = new DeskLocationsSavedData.DeskLocation(level.dimension(), pos);
        if (oldCompany != null) {
            data.remove(oldCompany, location);
            setTickets(level, pos, false);
        }
        if (newCompany != null) {
            data.add(newCompany, location);
            if (loadedCompanies.contains(newCompany)) {
                setTickets(level, pos, true);
            }
        }
    }

    public static void deskLoaded(ServerLevel level, BlockPos pos, CompanyId company) {
        DeskLocationsSavedData data = DeskLocationsSavedData.get(level.getServer());
        data.add(company, new DeskLocationsSavedData.DeskLocation(level.dimension(), pos));
        if (loadedCompanies.contains(company)) {
            setTickets(level, pos, true);
        }
    }

    private static void loadDesks(MinecraftServer server, DeskLocationsSavedData data, CompanyId company) {
        for (DeskLocationsSavedData.DeskLocation location : data.of(company)) {
            ServerLevel level = server.getLevel(location.dimension());
            if (level == null) {
                CreateKontor.LOGGER.warn(
                        "Forgetting the desk of company {} at {} in {}: the dimension does not exist.",
                        company.value(),
                        location.pos(),
                        location.dimension().location());
                data.remove(company, location);
                continue;
            }
            setTickets(level, location.pos(), true);
            if (!holdsDeskOf(level, location.pos(), company)) {
                CreateKontor.LOGGER.info(
                        "Forgetting the desk of company {} at {} in {}: it is not there anymore.",
                        company.value(),
                        location.pos(),
                        location.dimension().location());
                setTickets(level, location.pos(), false);
                data.remove(company, location);
            }
        }
    }

    private static void unloadDesks(MinecraftServer server, DeskLocationsSavedData data, CompanyId company) {
        for (DeskLocationsSavedData.DeskLocation location : data.of(company)) {
            ServerLevel level = server.getLevel(location.dimension());
            if (level != null) {
                setTickets(level, location.pos(), false);
            }
        }
    }

    private static boolean holdsDeskOf(ServerLevel level, BlockPos pos, CompanyId company) {
        if (!(level.getBlockState(pos).getBlock() instanceof ChunkLoadingDesk)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CompanyBound bound) {
            return company.equals(bound.getCompanyId());
        }
        return true;
    }

    private static void setTickets(ServerLevel level, BlockPos owner, boolean add) {
        ChunkPos center = new ChunkPos(owner);
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                DESK_CONTROLLER.forceChunk(level, owner, center.x + dx, center.z + dz, add, true);
            }
        }
    }

    public static void refreshHeld(MinecraftServer server, CompanyRegistry registry, CompanyId id) {
        Set<ChunkKey> wanted = new HashSet<>();
        if (loadedCompanies.contains(id)) {
            registry.get(id).ifPresent(company -> {
                for (PropertyHolding holding : company.properties()) {
                    if (holding.chunkLoading()) {
                        wanted.add(holding.chunk());
                    }
                }
            });
        }

        Set<ChunkKey> forced = forcedHeldChunks.computeIfAbsent(id, key -> new HashSet<>());
        forced.removeIf(chunk -> {
            if (wanted.contains(chunk)) {
                return false;
            }
            setHeldTicket(server, chunk, false);
            return true;
        });
        for (ChunkKey chunk : wanted) {
            if (!forced.contains(chunk) && setHeldTicket(server, chunk, true)) {
                forced.add(chunk);
            }
        }
        if (forced.isEmpty()) {
            forcedHeldChunks.remove(id);
        }
    }

    private static boolean setHeldTicket(MinecraftServer server, ChunkKey chunk, boolean add) {
        ServerLevel level = server.getLevel(ResourceKey.create(
                Registries.DIMENSION, ResourceLocation.parse(chunk.dimension().value())));
        if (level == null) {
            return false;
        }
        BlockPos owner = new BlockPos(chunk.x() << 4, 0, chunk.z() << 4);
        HELD_CONTROLLER.forceChunk(level, owner, chunk.x(), chunk.z(), add, true);
        return true;
    }
}
