package com.timder.kontor.game.wiki;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public record WikiCategory(String id, Component title) {
    public static final WikiCategory GUIDES = new WikiCategory("guides", Component.translatable("wiki.createkontor.category.guides"));
    public static final WikiCategory BASICS = new WikiCategory("basics", Component.translatable("wiki.createkontor.category.basics"));
    public static final WikiCategory EMPLOYEES = new WikiCategory("employees", Component.translatable("wiki.createkontor.category.employees"));
    public static final WikiCategory BLOCKS = new WikiCategory("blocks", Component.translatable("wiki.createkontor.category.blocks"));
    public static final WikiCategory ITEMS = new WikiCategory("items", Component.translatable("wiki.createkontor.category.items"));

    public WikiCategory {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(title, "title must not be null.");
    }
}
