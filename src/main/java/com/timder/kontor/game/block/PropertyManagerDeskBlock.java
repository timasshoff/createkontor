package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.PropertyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.PropertyManagerUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.registry.KontorBlocks;
import java.util.Locale;
import net.minecraft.network.chat.Component;

public class PropertyManagerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("property_manager");

    public PropertyManagerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.PROPERTY_MANAGER_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "property_manager",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.property_manager"),
                this::buildWikiPage,
                KontorBlocks.PROPERTY_MANAGER_DESK.getId());
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return PropertyManagerUi.create(context, new PropertyManagerActions(context.desk()));
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.employeeScene(defaultBlockState()));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.property_manager.p1", PropertyConfig.MANAGER_RADIUS.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.property_manager.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.property_manager.p2")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.property_manager.p3",
                String.format(Locale.ROOT, "%.1f%%", PropertyConfig.DAILY_RENT_RATE.get() * 100.0),
                String.format(Locale.ROOT, "%.1f%%", PropertyConfig.RESALE_SHARE.get() * 100.0))));
    }
}
