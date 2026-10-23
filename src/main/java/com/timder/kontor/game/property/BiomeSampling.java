package com.timder.kontor.game.property;

import com.timder.kontor.core.company.property.BiomeId;
import com.timder.kontor.core.company.property.DimensionId;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

public final class BiomeSampling {

    public static DimensionId dimensionId(ServerLevel level) {
        return new DimensionId(level.dimension().location().toString());
    }

    public static BiomeId biomeAt(ServerLevel level, int blockX, int blockZ, int ceilingSampleY) {
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkGenerator generator = chunkSource.getGenerator();
        RandomState randomState = chunkSource.randomState();

        int y = level.dimensionType().hasCeiling()
                ? ceilingSampleY
                : generator.getBaseHeight(blockX, blockZ, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);

        Holder<Biome> biome = generator
                .getBiomeSource()
                .getNoiseBiome(
                        QuartPos.fromBlock(blockX),
                        QuartPos.fromBlock(y),
                        QuartPos.fromBlock(blockZ),
                        randomState.sampler());
        return idOf(biome);
    }

    public static Set<BiomeId> possibleBiomes(ServerLevel level) {
        Set<BiomeId> biomes = new LinkedHashSet<>();
        for (Holder<Biome> biome :
                level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
            biomes.add(idOf(biome));
        }
        return biomes;
    }

    private static BiomeId idOf(Holder<Biome> biome) {
        return biome.unwrapKey()
                .map(key -> new BiomeId(key.location().toString()))
                .orElseThrow(() -> new IllegalStateException("A biome without a registry key cannot be measured."));
    }
}
