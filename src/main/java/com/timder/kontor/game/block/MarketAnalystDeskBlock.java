package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.MarketAnalystUi;

public class MarketAnalystDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("market_analyst");

    public MarketAnalystDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.MARKET_ANALYST_SALARY_IN_DOLLARS);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return MarketAnalystUi.create(context, new MarketAnalystActions(context.desk()));
    }
}
