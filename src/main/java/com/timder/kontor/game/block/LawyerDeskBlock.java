package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.LawyerUi;

public class LawyerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("lawyer");

    public LawyerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.LAWYER_SALARY_IN_DOLLARS);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return LawyerUi.create(context, new LawyerActions(context.desk()));
    }
}
