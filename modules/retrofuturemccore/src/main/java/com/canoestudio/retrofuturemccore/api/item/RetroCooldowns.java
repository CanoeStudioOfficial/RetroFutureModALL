package com.canoestudio.retrofuturemccore.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class RetroCooldowns {

    private RetroCooldowns() {
    }

    public static void setCooldown(EntityPlayer player, Item item, int ticks) {
        if (player != null && item != null && ticks > 0) {
            player.getCooldownTracker().setCooldown(item, ticks);
        }
    }

    public static void setCooldown(EntityPlayer player, ItemStack stack, int ticks) {
        if (stack != null && !stack.isEmpty()) {
            setCooldown(player, stack.getItem(), ticks);
        }
    }

    public static boolean hasCooldown(EntityPlayer player, Item item) {
        return player != null && item != null && player.getCooldownTracker().hasCooldown(item);
    }

    public static boolean hasCooldown(EntityPlayer player, ItemStack stack) {
        return stack != null && !stack.isEmpty() && hasCooldown(player, stack.getItem());
    }

    public static float getCooldown(EntityPlayer player, Item item, float partialTicks) {
        return player == null || item == null ? 0.0F : player.getCooldownTracker().getCooldown(item, partialTicks);
    }
}
