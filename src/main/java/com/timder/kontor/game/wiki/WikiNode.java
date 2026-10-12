package com.timder.kontor.game.wiki;

import com.lowdragmc.lowdraglib2.gui.util.ITreeNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WikiNode implements ITreeNode<String, WikiEntry> {

    private final String key;
    private final Component title;
    @Nullable
    private final WikiEntry entry;
    @Nullable
    private WikiNode parent;
    private final List<WikiNode> children = new ArrayList<>();
    private final List<WikiNode> childrenView = Collections.unmodifiableList(children);

    private WikiNode(String key, Component title, @Nullable WikiEntry entry) {
        this.key = key;
        this.title = title;
        this.entry = entry;
    }

    public static WikiNode root(List<WikiIndex.Group<WikiEntry>> groups) {
        WikiNode root = new WikiNode("root", Component.empty(), null);
        for (WikiIndex.Group<WikiEntry> group : groups) {
            WikiNode category = new WikiNode(group.categoryId(), group.entries().get(0).category().title(), null);
            category.parent = root;
            root.children.add(category);
            for (WikiEntry entry : group.entries()) {
                WikiNode page = new WikiNode(entry.id(), entry.title(), entry);
                page.parent = category;
                category.children.add(page);
            }
        }
        return root;
    }

    public Component title() {
        return title;
    }

    @Nullable
    public WikiEntry entry() {
        return entry;
    }

    public List<WikiNode> children() {
        return childrenView;
    }

    @Override
    public int getDimension() {
        return parent == null ? 0 : parent.getDimension() + 1;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Nullable
    @Override
    public WikiEntry getContent() {
        return entry;
    }

    @Nullable
    @Override
    public WikiNode getParent() {
        return parent;
    }

    @Override
    public List<WikiNode> getChildren() {
        return childrenView;
    }
}
