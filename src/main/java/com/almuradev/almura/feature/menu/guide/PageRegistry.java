package com.almuradev.almura.feature.menu.guide;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PageRegistry {

    private static final Map<String, Page> PAGES = Maps.newHashMap();

    public static Optional<Page> getPage(String identifier) {
        return Optional.ofNullable(PAGES.get(identifier));
    }

    public static Page putPage(Page page) {
        return PAGES.put(page.getIdentifier(), page);
    }

    public static Optional<Page> removePage(String identifier) {
        return Optional.ofNullable(PAGES.remove(identifier));
    }

    public static Map<String, Page> getAll() {
        return Collections.unmodifiableMap(PAGES);
    }

    public static void clear() {
        PAGES.clear();
    }
}