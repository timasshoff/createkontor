package com.timder.kontor.game.block.company;

import com.timder.kontor.core.company.Company;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@FunctionalInterface
public interface CompanyBindGate {

    CompanyBindGate ALWAYS_ALLOWED = company -> null;

    @Nullable
    Component checkBind(Company company);
}
