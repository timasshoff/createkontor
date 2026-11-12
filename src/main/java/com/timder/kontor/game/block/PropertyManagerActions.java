package com.timder.kontor.game.block;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.PropertyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.property.BiomeId;
import com.timder.kontor.core.company.property.ChunkKey;
import com.timder.kontor.core.company.property.PropertyParams;
import com.timder.kontor.core.company.property.PropertyRules;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.advancement.Milestones;
import com.timder.kontor.game.block.employee.EmployeeActions;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.chunk.KontorChunkLoading;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.property.BiomeSampling;
import com.timder.kontor.game.property.PropertyProtection;
import com.timder.kontor.util.ComponentFormatting;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

public class PropertyManagerActions extends EmployeeActions {
    public PropertyManagerActions(EmployeeDeskBlockEntity desk) {
        super(desk);
    }

    public Optional<S2CActionResult> acquire(ChunkPos chunk, PropertyRules.Mode mode) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        BiomeId biome = BiomeSampling.biomeAt(serverLevel, chunk);
        ChunkKey key = new ChunkKey(BiomeSampling.dimensionId(serverLevel), chunk.x, chunk.z);

        PropertyRules.AcquireResult result;
        try {
            result = PropertyRules.acquire(
                    company, comapnyData.getRegistry().all(), key, biome, mode, economy.currentDay(), params);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        if (!result.succeeded()) {
            return S2CActionResult.error(Component.translatable("message.createkontor.property_manager."
                    + result.status().name().toLowerCase()));
        }
        comapnyData.setDirty();
        PropertyProtection.onAcquired(key, company);
        Milestones.awardToMembers(serverLevel.getServer(), company, "first_property");
        if (result.status() == PropertyRules.AcquireResult.Status.BOUGHT
                || result.status() == PropertyRules.AcquireResult.Status.CONVERTED) {
            return S2CActionResult.silentSuccess();
        }
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.property_manager."
                        + result.status().name().toLowerCase(),
                chunk.toString()));
    }

    public Optional<S2CActionResult> cancel(ChunkPos chunk) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        ChunkKey key = new ChunkKey(BiomeSampling.dimensionId(serverLevel), chunk.x, chunk.z);

        PropertyRules.CancelResult result = PropertyRules.cancelRent(company, key);

        if (result == PropertyRules.CancelResult.CANCELLED) {
            return S2CActionResult.success(Component.translatable(
                    "message.createkontor.property_manager." + result.name().toLowerCase(), chunk.toString()));
        }
        return S2CActionResult.error(Component.translatable(
                "message.createkontor.property_manager." + result.name().toLowerCase(), chunk.toString()));
    }

    public Optional<S2CActionResult> sell(ChunkPos chunk) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        ChunkKey key = new ChunkKey(BiomeSampling.dimensionId(serverLevel), chunk.x, chunk.z);

        PropertyRules.SellResult result = PropertyRules.sell(company, key, economy.currentDay(), params);
        if (result.status() == PropertyRules.SellResult.Status.SOLD) {
            KontorChunkLoading.refreshHeld(serverLevel.getServer(), comapnyData.getRegistry(), company.id());
            return S2CActionResult.silentSuccess();
        }
        return S2CActionResult.error(Component.translatable(
                "message.createkontor.property_manager."
                        + result.status().name().toLowerCase(),
                chunk.toString()));
    }

    public Optional<S2CActionResult> setChunkLoading(ChunkPos chunk, boolean enabled) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        ChunkKey key = new ChunkKey(BiomeSampling.dimensionId(serverLevel), chunk.x, chunk.z);
        PropertyParams params = PropertyConfig.toPropertyParams();

        PropertyRules.ChunkLoadingResult result = PropertyRules.setChunkLoading(company, key, enabled, params);
        Component message = Component.translatable(
                "message.createkontor.property_manager.chunk_loading_"
                        + result.name().toLowerCase(),
                chunk.toString(),
                params.maxLoadedChunks());
        if (result == PropertyRules.ChunkLoadingResult.ENABLED || result == PropertyRules.ChunkLoadingResult.DISABLED) {
            comapnyData.setDirty();
            KontorChunkLoading.refreshHeld(serverLevel.getServer(), comapnyData.getRegistry(), company.id());
            return S2CActionResult.success(message);
        }
        return S2CActionResult.error(message);
    }
}
