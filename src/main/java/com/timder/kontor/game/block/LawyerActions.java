package com.timder.kontor.game.block;

import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;

import java.util.Objects;

public final class LawyerActions {

    private final EmployeeDeskBlockEntity desk;

    public LawyerActions(EmployeeDeskBlockEntity desk) {
        this.desk = Objects.requireNonNull(desk, "desk must not be null.");
    }

}
