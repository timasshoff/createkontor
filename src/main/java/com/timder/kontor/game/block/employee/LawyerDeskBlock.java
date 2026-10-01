package com.timder.kontor.game.block.employee;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;

public class LawyerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("lawyer");

    public LawyerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.LAWYER_SALARY_IN_DOLLARS);
    }
}
