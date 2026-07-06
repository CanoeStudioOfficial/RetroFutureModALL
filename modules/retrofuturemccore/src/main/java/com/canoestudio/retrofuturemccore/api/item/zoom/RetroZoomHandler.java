package com.canoestudio.retrofuturemccore.api.item.zoom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface RetroZoomHandler {

    boolean isZooming(EntityPlayer player, ItemStack stack);

    float getFovMultiplier(EntityPlayer player, ItemStack stack, float partialTicks);

    RetroZoomOverlay getOverlay(EntityPlayer player, ItemStack stack);
}
