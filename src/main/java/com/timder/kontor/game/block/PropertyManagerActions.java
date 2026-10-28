package com.timder.kontor.game.block;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.property.BiomeId;
import com.timder.kontor.core.company.property.ChunkKey;
import com.timder.kontor.core.company.property.PropertyRules;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.employee.EmployeeActions;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.property.BiomeSampling;
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
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.property_manager."
                        + result.status().name().toLowerCase(),
                chunk.toString()));
    }
}
