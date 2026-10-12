package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TreeList;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.gui.util.ITreeNode;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.game.wiki.WikiEntry;
import com.timder.kontor.game.wiki.WikiIndex;
import com.timder.kontor.game.wiki.WikiNode;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.*;

public final class ConsultantUi {

    private static final float TREE_WIDTH_PERCENT = 25;

    private ConsultantUi() {
    }

    public static ModularUI create(EmployeeDeskContext context) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.consultant_desk.wiki")), wikiTab());

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    private static UIElement wikiTab() {
        UIElement content = new UIElement().layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .flexDirection(FlexDirection.ROW)
                        .gapAll(4));

        List<WikiIndex.Group<WikiEntry>> groups = WikiIndex.<WikiEntry>group(
                ConsultantWiki.entries(),
                entry -> entry.category().id(),
                entry -> entry.category().title().getString(),
                WikiEntry::id,
                entry -> entry.title().getString());
        WikiNode root = WikiNode.root(groups);

        ScrollerView pageScroller = UiContainer.tabScroller();
        pageScroller.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).flexShrink(1).minWidth(0));

        pageScroller.addScrollViewChild(UiLabels.paragraphSecondary(Component.translatable(
                root.children().isEmpty()
                        ? "ui.createkontor.consultant_desk.no_pages"
                        : "ui.createkontor.consultant_desk.select_page"
        ), Horizontal.CENTER));

        TreeList<WikiNode> tree = new TreeList<>(root, true);
        tree.setFlattenRoot(true);
        tree.setNodeUISupplier(TreeList.textTemplate(WikiNode::title));
        tree.setSelectableNodeFilter(WikiNode::isLeaf);
        tree.setClickToExpand(true);
        tree.setOnSelectedChanged(selected -> showPage(pageScroller, selected));
        for (WikiNode category : root.children()) {
            tree.expandNode(category);
        }
        ScrollerView treeScroller = UiContainer.tabScroller();
        treeScroller.layout(layout -> layout.widthPercent(TREE_WIDTH_PERCENT).flexShrink(0).paddingAll(4));
        treeScroller.style(style -> style.background(Sprites.BORDER_DARK));
        treeScroller.addScrollViewChild(tree);

        content.addChildren(treeScroller, pageScroller);
        return content;
    }

    private static void showPage(ScrollerView pageScroller, Set<WikiNode> selected) {
        pageScroller.clearAllScrollViewChildren();
        WikiNode node = selected.stream().findFirst().orElse(null);
        if (node == null || node.entry() == null) {
            pageScroller.addScrollViewChild(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.consultant_desk.select_page"), Horizontal.CENTER));
            return;
        }

        WikiEntry entry = node.entry();
        UIElement page;
        try {
            page = new UIElement().layout(layout -> layout.widthPercent(100).gapAll(8));
            page.addChild(UiLabels.h1(entry.title(), Horizontal.LEFT));
            entry.page().accept(page);
        } catch (RuntimeException e) {
            CreateKontor.LOGGER.error("The wiki page {} could not be built.", entry.id(), e);
            pageScroller.addScrollViewChild(UiLabels.paragraphError(Component.translatable("ui.createkontor.consultant_desk.page_error"), Horizontal.LEFT));
            return;
        }
        pageScroller.addScrollViewChild(page);
    }
}
