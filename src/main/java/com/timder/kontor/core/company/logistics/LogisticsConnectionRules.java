package com.timder.kontor.core.company.logistics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.Money;
import java.util.Objects;
import java.util.UUID;

public class LogisticsConnectionRules {

    public enum Check {
        OK,
        LEGAL_FORM_NOT_ALLOWED,
        COMPANY_HAS_OTHER_NETWORK,
        NETWORK_USED_BY_OTHER_COMPANY,
        NETWORK_OWNER_NOT_MEMBER;

        public boolean isOk() {
            return this == OK;
        }
    }

    public static Check check(
            CompanyRegistry registry, Company company, UUID networkId, UUID networkOwner, CompanyParams params) {
        Objects.requireNonNull(registry, "registry must not be null.");
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(networkId, "networkId must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!company.legalForm(params).logisticsNetwork()) {
            return Check.LEGAL_FORM_NOT_ALLOWED;
        }

        UUID current = company.logistics().networkId();
        if (current != null && !current.equals(networkId)) {
            return Check.COMPANY_HAS_OTHER_NETWORK;
        }

        for (Company other : registry.all()) {
            if (!other.id().equals(company.id())
                    && networkId.equals(other.logistics().networkId())) {
                return Check.NETWORK_USED_BY_OTHER_COMPANY;
            }
        }

        if (networkOwner != null && !company.isMember(networkOwner)) {
            return Check.NETWORK_OWNER_NOT_MEMBER;
        }
        return Check.OK;
    }

    public static Check connect(
            CompanyRegistry registry, Company company, UUID networkId, UUID networkOwner, CompanyParams params) {
        Check check = check(registry, company, networkId, networkOwner, params);
        if (check.isOk() && !networkId.equals(company.logistics().networkId())) {
            company.setLogistics(LogisticsState.connectedTo(networkId));
        }
        return check;
    }

    public static Money disconnect(Company company, long day, long nowTick, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!company.logistics().isConnected()) {
            return Money.ZERO;
        }
        company.setLogistics(LogisticsStorageRules.closeAt(company.logistics(), nowTick));
        Money booked = LogisticsStorageRules.bookUnbilled(company, day, params);
        company.setLogistics(LogisticsState.disconnected());
        return booked;
    }
}
