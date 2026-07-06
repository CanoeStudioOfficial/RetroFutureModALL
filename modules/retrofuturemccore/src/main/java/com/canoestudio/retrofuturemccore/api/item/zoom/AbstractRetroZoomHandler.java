package com.canoestudio.retrofuturemccore.api.item.zoom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class AbstractRetroZoomHandler implements RetroZoomHandler {

    @Override
    public float getFovMultiplier(EntityPlayer player, ItemStack stack, float partialTicks) {
        return 0.1F;
    }

    @Override
    public RetroZoomOverlay getOverlay(EntityPlayer player, ItemStack stack) {
        return null;
    }
}
