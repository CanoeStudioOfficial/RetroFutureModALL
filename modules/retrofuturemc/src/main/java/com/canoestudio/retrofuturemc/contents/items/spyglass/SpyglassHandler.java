package com.canoestudio.retrofuturemc.contents.items.spyglass;

import com.canoestudio.retrofuturemc.contents.items.ModItems;
import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpyglassHandler {
    public static final float FOV_MULTIPLIER = 0.1F;
    private static final float SCOPE_TARGET_SCALE = 1.125F;
    private static final float SCOPE_RESET_SCALE = 0.5F;
    private static final ResourceLocation OVERLAY = new ResourceLocation(Tags.MOD_ID, "textures/gui/spyglass_scope.png");

    private static float fovScale = 1.0F;
    private static float previousFovScale = 1.0F;
    private static float scopeScale = SCOPE_RESET_SCALE;
    private static boolean sensitivityReduced;
    private static float originalMouseSensitivity;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        previousFovScale = fovScale;

        boolean usingSpyglass = isUsingSpyglass(mc, player);
        if (usingSpyglass) {
            fovScale = Math.max(fovScale + (FOV_MULTIPLIER - fovScale) * 0.5F, FOV_MULTIPLIER);
            reduceMouseSensitivity(mc);
        } else {
            fovScale = Math.min(fovScale + (1.0F - fovScale) * 0.5F, 1.0F);
            restoreMouseSensitivity(mc);
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (isUsingSpyglass(mc, mc.player)) {
            scopeScale += (SCOPE_TARGET_SCALE - scopeScale) * event.renderTickTime * 0.5F;
        } else {
            scopeScale = SCOPE_RESET_SCALE;
        }
    }

    public static float getFovMultiplier(float partialTicks) {
        return previousFovScale + (fovScale - previousFovScale) * MathHelper.clamp(partialTicks, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (!isUsingSpyglass(mc, mc.player)) {
            return;
        }

        renderSpyglassOverlay(mc, event.getResolution());
    }

    private static boolean isUsingSpyglass(Minecraft mc, EntityPlayerSP player) {
        if (player == null || mc.gameSettings.thirdPersonView != 0 || !player.isHandActive()) {
            return false;
        }

        ItemStack activeStack = player.getActiveItemStack();
        return !activeStack.isEmpty() && activeStack.getItem() == ModItems.SPYGLASS;
    }

    private static void reduceMouseSensitivity(Minecraft mc) {
        if (!sensitivityReduced) {
            originalMouseSensitivity = mc.gameSettings.mouseSensitivity;
            sensitivityReduced = true;
        }

        mc.gameSettings.mouseSensitivity = originalMouseSensitivity * 0.1F;
    }

    private static void restoreMouseSensitivity(Minecraft mc) {
        if (sensitivityReduced) {
            mc.gameSettings.mouseSensitivity = originalMouseSensitivity;
            sensitivityReduced = false;
        }
    }

    private static void renderSpyglassOverlay(Minecraft mc, ScaledResolution resolution) {
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int size = MathHelper.ceil(Math.min(width, height) * scopeScale);
        int left = (width - size) / 2;
        int top = (height - size) / 2;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(OVERLAY);
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0.0F, 0.0F, size, size, size, size);
        drawMask(width, height, left, top, size);

        GlStateManager.disableBlend();
    }

    private static void drawMask(int width, int height, int left, int top, int size) {
        int black = 0xFF000000;
        int right = left + size;
        int bottom = top + size;

        Gui.drawRect(0, 0, width, Math.max(top, 0), black);
        Gui.drawRect(0, Math.min(bottom, height), width, height, black);
        Gui.drawRect(0, Math.max(top, 0), Math.max(left, 0), Math.min(bottom, height), black);
        Gui.drawRect(Math.min(right, width), Math.max(top, 0), width, Math.min(bottom, height), black);
    }
}
