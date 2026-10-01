package com.timder.kontor.core.company.employee;

import com.timder.kontor.core.company.CompanyId;

import java.util.Objects;

public interface EmployeeEvent {

    CompanyId companyId();

    enum EndReason {
        ABSENT,
        NOT_REPORTING
    }

    /**
     * The contract of an employee ended and the employee was removed from the company.
     *
     * @param companyId The company
     * @param employee The employee
     * @param reason Why the contract ended
     */
    record ContractEnded(CompanyId companyId, Employee employee, EndReason reason) implements EmployeeEvent {
        public ContractEnded {
            Objects.requireNonNull(companyId, "companyId must not be null.");
            Objects.requireNonNull(employee, "employee must not be null.");
            Objects.requireNonNull(reason, "reason must not be null.");
        }
    }

}
