package com.timder.kontor.game.block.consultant;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.ConsultantUi;

public class ConsultantDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("consultant");

    public ConsultantDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.CONSULTANT_SALARY_IN_DOLLARS);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return ConsultantUi.create(context);
    }
}
