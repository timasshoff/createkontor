package com.timder.kontor.game.wiki;

import java.util.*;
import java.util.function.Function;

public final class WikiIndex {

    public record Group<E>(String categoryId, String categoryTitle, List<E> entries) { }

    public static <E> List<Group<E>> group(Collection<? extends E> entries,
                                           Function<E, String> categoryId,
                                           Function<E, String> categoryTitle,
                                           Function<E, String> entryId,
                                           Function<E, String> entryTitle) {
        Map<String, List<E>> byCategory = new LinkedHashMap<>();
        Map<String, String> titles = new LinkedHashMap<>();
        for (E entry : entries) {
            String id = categoryId.apply(entry);
            byCategory.computeIfAbsent(id, key -> new ArrayList<>()).add(entry);
            titles.putIfAbsent(id, categoryTitle.apply(entry));
        }

        Comparator<E> entryOrder = Comparator.comparing(entryTitle, String.CASE_INSENSITIVE_ORDER).thenComparing(entryId);
        List<Group<E>> groups = new ArrayList<>();
        for (Map.Entry<String, List<E>> category : byCategory.entrySet()) {
            List<E> sorted = new ArrayList<>(category.getValue());
            sorted.sort(entryOrder);
            groups.add(new Group<>(category.getKey(), titles.get(category.getKey()), List.copyOf(sorted)));
        }
        groups.sort(Comparator
                .comparing((Group<E> group) -> group.categoryTitle(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Group::categoryId));
        return List.copyOf(groups);
    }
}
