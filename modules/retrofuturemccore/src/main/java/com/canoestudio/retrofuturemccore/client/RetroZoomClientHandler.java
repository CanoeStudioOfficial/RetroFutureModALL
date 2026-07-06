package com.canoestudio.retrofuturemccore.client;

import com.canoestudio.retrofuturemccore.api.item.zoom.RetroZoomOverlay;
import com.canoestudio.retrofuturemccore.api.item.zoom.RetroZoomRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RetroZoomClientHandler {

    @SubscribeEvent
    public void updateFov(FOVUpdateEvent event) {
        RetroZoomRegistry.ActiveZoom zoom = RetroZoomRegistry.getActiveZoom(event.getEntity(),
                Minecraft.getMinecraft().getRenderPartialTicks());
        if (zoom != null) {
            event.setNewfov(event.getFov() * zoom.getFovMultiplier());
        }
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return;
        }

        RetroZoomRegistry.ActiveZoom zoom = RetroZoomRegistry.getActiveZoom(mc.player, event.getPartialTicks());
        if (zoom != null) {
            RetroZoomOverlay overlay = zoom.getOverlay();
            if (overlay != null) {
                overlay.render(event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight(),
                        event.getPartialTicks());
            }
        }
    }
}
