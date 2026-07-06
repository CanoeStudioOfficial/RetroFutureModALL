package com.canoestudio.retrofuturemccore.api.item.zoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class RetroZoomRegistry {

    private static final List<Entry> ENTRIES = new ArrayList<Entry>();

    private RetroZoomRegistry() {
    }

    public static synchronized void register(Item item, RetroZoomHandler handler) {
        ENTRIES.add(new Entry(item, handler));
    }

    public static synchronized List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(ENTRIES));
    }

    public static ActiveZoom getActiveZoom(EntityPlayer player, float partialTicks) {
        if (player == null) {
            return null;
        }

        ActiveZoom mainHand = getActiveZoom(player, player.getHeldItemMainhand(), partialTicks);
        if (mainHand != null) {
            return mainHand;
        }
        return getActiveZoom(player, player.getHeldItemOffhand(), partialTicks);
    }

    private static ActiveZoom getActiveZoom(EntityPlayer player, ItemStack stack, float partialTicks) {
        if (stack.isEmpty()) {
            return null;
        }
        for (Entry entry : getEntries()) {
            if (entry.item == stack.getItem() && entry.handler.isZooming(player, stack)) {
                return new ActiveZoom(stack, entry.handler.getFovMultiplier(player, stack, partialTicks),
                        entry.handler.getOverlay(player, stack));
            }
        }
        return null;
    }

    public static final class Entry {

        private final Item item;
        private final RetroZoomHandler handler;

        private Entry(Item item, RetroZoomHandler handler) {
            this.item = item;
            this.handler = handler;
        }

        public Item getItem() {
            return this.item;
        }

        public RetroZoomHandler getHandler() {
            return this.handler;
        }
    }

    public static final class ActiveZoom {

        private final ItemStack stack;
        private final float fovMultiplier;
        private final RetroZoomOverlay overlay;

        private ActiveZoom(ItemStack stack, float fovMultiplier, RetroZoomOverlay overlay) {
            this.stack = stack;
            this.fovMultiplier = fovMultiplier;
            this.overlay = overlay;
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public float getFovMultiplier() {
            return this.fovMultiplier;
        }

        public RetroZoomOverlay getOverlay() {
            return this.overlay;
        }
    }
}
