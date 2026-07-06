package com.canoestudio.retrofuturemccore.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RetroEventRegistry {
    private static final List<RetroBlockInteractionHandler> BLOCK_INTERACTION_HANDLERS =
            new ArrayList<RetroBlockInteractionHandler>();
    private static final List<RetroEntityInteractionHandler> ENTITY_INTERACTION_HANDLERS =
            new ArrayList<RetroEntityInteractionHandler>();
    private static final List<RetroEntityLifecycleHandler> ENTITY_LIFECYCLE_HANDLERS =
            new ArrayList<RetroEntityLifecycleHandler>();
    private static final List<RetroDropHandler> DROP_HANDLERS = new ArrayList<RetroDropHandler>();

    private RetroEventRegistry() {
    }

    public static synchronized void registerBlockInteraction(RetroBlockInteractionHandler handler) {
        addMissing(BLOCK_INTERACTION_HANDLERS, handler);
    }

    public static synchronized void registerEntityInteraction(RetroEntityInteractionHandler handler) {
        addMissing(ENTITY_INTERACTION_HANDLERS, handler);
    }

    public static synchronized void registerEntityLifecycle(RetroEntityLifecycleHandler handler) {
        addMissing(ENTITY_LIFECYCLE_HANDLERS, handler);
    }

    public static synchronized void registerDrops(RetroDropHandler handler) {
        addMissing(DROP_HANDLERS, handler);
    }

    public static synchronized List<RetroBlockInteractionHandler> getBlockInteractionHandlers() {
        return snapshot(BLOCK_INTERACTION_HANDLERS);
    }

    public static synchronized List<RetroEntityInteractionHandler> getEntityInteractionHandlers() {
        return snapshot(ENTITY_INTERACTION_HANDLERS);
    }

    public static synchronized List<RetroEntityLifecycleHandler> getEntityLifecycleHandlers() {
        return snapshot(ENTITY_LIFECYCLE_HANDLERS);
    }

    public static synchronized List<RetroDropHandler> getDropHandlers() {
        return snapshot(DROP_HANDLERS);
    }

    private static <T> void addMissing(List<T> handlers, T handler) {
        if (handler != null && !handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    private static <T> List<T> snapshot(List<T> handlers) {
        return Collections.unmodifiableList(new ArrayList<T>(handlers));
    }
}
