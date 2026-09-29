package com.timder.kontor.core.company.legalform;

import java.util.Objects;

public record ApplyResult(Status status, UpgradeChecklist checklist) {

    public enum Status {
        /** The requirements were met, the fee was paid, the application is running. */
        STARTED,
        /** The company already has an application. Nothing was paid. */
        ALREADY_PENDING,
        /** The company is in the highest legal form. Nothing was paid. */
        NO_NEXT_FORM,
        /** At least one requirement is not fulfilled. Nothing was paid. */
        REQUIREMENTS_NOT_MET
    }

    public ApplyResult {
        Objects.requireNonNull(status, "status must not be null.");
        boolean needsChecklist = status == Status.STARTED || status == Status.REQUIREMENTS_NOT_MET;
        if (needsChecklist && checklist == null) throw new IllegalArgumentException(status + " needs a checklist.");
        if (!needsChecklist && checklist != null) throw new IllegalArgumentException(status + " must not have a checklist.");
        if (status == Status.STARTED && !checklist.allMet()) throw new IllegalArgumentException("STARTED needs a checklist with every row met.");
        if (status == Status.REQUIREMENTS_NOT_MET && checklist.allMet()) throw new IllegalArgumentException("REQUIREMENTS_NOT_MET needs a checklist with an unmet row.");
    }

    public boolean started() {
        return status == Status.STARTED;
    }
}
