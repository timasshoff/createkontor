package com.timder.kontor.game.chunk;

import com.timder.kontor.core.company.CompanyId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class DeskLocationsSavedData extends SavedData {

    private static final String NAME = "kontor_desk_locations";

    public record DeskLocation(ResourceKey<Level> dimension, BlockPos pos) {
        public DeskLocation {
            Objects.requireNonNull(dimension, "dimension must not be null.");
            pos = pos.immutable();
        }
    }

    private final Map<CompanyId, Set<DeskLocation>> locations = new LinkedHashMap<>();

    public static DeskLocationsSavedData get(MinecraftServer server) {
        SavedData.Factory<DeskLocationsSavedData> factory = new SavedData.Factory<>(
                DeskLocationsSavedData::new,
                (tag, registries) -> load(tag)
        );
        return server.overworld().getDataStorage().computeIfAbsent(factory, NAME);
    }

    /**
     * @return True if the location was not known yet
     */
    public boolean add(CompanyId company, DeskLocation location) {
        boolean added = locations.computeIfAbsent(company, id -> new LinkedHashSet<>()).add(location);
        if (added) {
            setDirty();
        }
        return added;
    }

    /**
     * @return True if the location was known
     */
    public boolean remove(CompanyId company, DeskLocation location) {
        Set<DeskLocation> set = locations.get(company);
        if (set == null || !set.remove(location)) {
            return false;
        }
        if (set.isEmpty()) {
            locations.remove(company);
        }
        setDirty();
        return true;
    }

    public void removeCompany(CompanyId company) {
        if (locations.remove(company) != null) {
            setDirty();
        }
    }

    public Set<DeskLocation> of(CompanyId company) {
        Set<DeskLocation> set = locations.get(company);
        return set == null ? Set.of() : Set.copyOf(set);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<CompanyId, Set<DeskLocation>> entry : locations.entrySet()) {
            for (DeskLocation location : entry.getValue()) {
                CompoundTag desk = new CompoundTag();
                desk.putInt("Company", entry.getKey().value());
                desk.putString("Dimension", location.dimension().location().toString());
                desk.putLong("Pos", location.pos().asLong());
                list.add(desk);
            }
        }
        tag.put("Desks", list);
        return tag;
    }

    private static DeskLocationsSavedData load(CompoundTag tag) {
        DeskLocationsSavedData data = new DeskLocationsSavedData();
        for (Tag t : tag.getList("Desks", Tag.TAG_COMPOUND)) {
            CompoundTag desk = (CompoundTag) t;
            ResourceLocation dimension = ResourceLocation.tryParse(desk.getString("Dimension"));
            int company = desk.getInt("Company");
            if (dimension == null || company <= 0) {
                continue;
            }
            data.locations.computeIfAbsent(new CompanyId(company), id -> new LinkedHashSet<>()).add(new DeskLocation(ResourceKey.create(Registries.DIMENSION, dimension), BlockPos.of(desk.getLong("Pos"))));
        }
        return data;
    }
}
