package com.timder.kontor.game.block.company;

import com.timder.kontor.core.company.CompanyId;

import javax.annotation.Nullable;

public interface CompanyBound {

    /**
     * @return The company this entity belongs to, null if it is unbound
     */
    @Nullable
    CompanyId getCompanyId();

    /**
     * Binds the block to a company or unbinds it.
     * @param id The company, null to unbind
     */
    void setCompanyId(@Nullable CompanyId id);
}
