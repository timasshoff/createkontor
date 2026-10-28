package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.PropertyManagerUi;

public class PropertyManagerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("property_manager");

    public PropertyManagerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.PROPERTY_MANAGER_SALARY_IN_DOLLARS);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return PropertyManagerUi.create(context, new PropertyManagerActions(context.desk()));
    }
}
