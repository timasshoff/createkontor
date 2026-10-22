package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.FinancesManagerUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FinancesManagerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("finances_manager");

    public FinancesManagerDeskBlock(BlockBehaviour.Properties properties) {
        super(properties, ROLE, CompanyConfig.FINANCES_MANAGER_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "finances_manager",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.finances_manager"),
                this::buildWikiPage);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return FinancesManagerUi.create(context, new FinancesManagerActions(context.desk()));
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.employeeScene(defaultBlockState()));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances_manager.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances_manager.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances_manager.p2")));
        page.addChild(WikiUtils.bulletList(Component.translatable(
                "wiki.createkontor.page.finances_manager.loan_requirements",
                CompanyConfig.BANK_LOAN_CREDIT_LIMIT_REVENUE_DAYS.get())));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.finances_manager.p3",
                CompanyConfig.BANK_LOAN_CREDIT_LIMIT_REVENUE_MULTIPLE.get(),
                CompanyConfig.BANK_LOAN_CREDIT_LIMIT_REVENUE_DAYS.get())));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances_manager.p4")));
    }
}
