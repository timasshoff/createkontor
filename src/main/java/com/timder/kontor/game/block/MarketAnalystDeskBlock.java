package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.MarketAnalystUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import net.minecraft.network.chat.Component;

public class MarketAnalystDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("market_analyst");

    public MarketAnalystDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.MARKET_ANALYST_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "market_analyst",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.market_analyst"),
                this::buildWikiPage);
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return MarketAnalystUi.create(context, new MarketAnalystActions(context.desk()));
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.employeeScene(defaultBlockState()));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.market_analyst.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.market_analyst.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.market_analyst.p2")));
        page.addChild(
                WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.market_analyst.market_infos")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.market_analyst.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.market_analyst.p3")));
        page.addChild(WikiUtils.bulletList(
                Component.translatable("wiki.createkontor.page.market_analyst.participation_infos")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.market_analyst.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.market_analyst.p4")));
    }
}
