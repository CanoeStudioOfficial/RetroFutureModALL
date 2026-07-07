package com.canoestudio.retrofuturelushcave.utils;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.contents.items.spyglass.SpyglassHandler;
import com.canoestudio.retrofuturemccore.api.item.zoom.RetroZoomHandler;
import com.canoestudio.retrofuturemccore.api.item.zoom.RetroZoomOverlay;
import com.canoestudio.retrofuturemccore.api.item.zoom.RetroZoomRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RetroFutureClientCoreIntegration {
    private static boolean registered;

    private RetroFutureClientCoreIntegration() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        RetroZoomRegistry.register(ModItems.SPYGLASS, new RetroZoomHandler() {
            @Override
            public boolean isZooming(EntityPlayer player, ItemStack stack) {
                ItemStack activeStack = player.getActiveItemStack();
                return player.isHandActive() && !activeStack.isEmpty() && activeStack.getItem() == ModItems.SPYGLASS;
            }

            @Override
            public float getFovMultiplier(EntityPlayer player, ItemStack stack, float partialTicks) {
                return SpyglassHandler.getFovMultiplier(partialTicks);
            }

            @Override
            public RetroZoomOverlay getOverlay(EntityPlayer player, ItemStack stack) {
                return null;
            }
        });
    }
}
