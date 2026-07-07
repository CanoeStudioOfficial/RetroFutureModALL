package com.canoestudio.retrofuturelushcave.utils;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PowderSnowHudHandler {
    private static final ResourceLocation POWDER_SNOW_OUTLINE = new ResourceLocation(Tags.MOD_ID, "textures/gui/powder_snow_outline.png");
    private static final int TICKS_TO_FREEZE = 140;
    private static int frozenTicks;
    private static int frozenTicksOld;

    private PowderSnowHudHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null || minecraft.world == null || player.isDead) {
            frozenTicksOld = frozenTicks;
            frozenTicks = 0;
            return;
        }

        frozenTicksOld = frozenTicks;
        if (isFreezing(player)) {
            frozenTicks = Math.min(TICKS_TO_FREEZE, frozenTicks + 1);
        } else {
            frozenTicks = Math.max(0, frozenTicks - 2);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || frozenTicks <= 0) {
            return;
        }

        float partialTicks = event.getPartialTicks();
        float ticks = frozenTicksOld + (frozenTicks - frozenTicksOld) * partialTicks;
        float alpha = Math.min(1.0F, ticks / (float)TICKS_TO_FREEZE);
        renderPowderSnowOverlay(Minecraft.getMinecraft(), event.getResolution(), alpha);
    }

    private static boolean isFreezing(EntityPlayer player) {
        World world = player.world;
        BlockPos feet = new BlockPos(player.posX, player.getEntityBoundingBox().minY + 0.05D, player.posZ);
        BlockPos belowFeet = new BlockPos(player.posX, player.getEntityBoundingBox().minY - 0.05D, player.posZ);
        BlockPos eyes = new BlockPos(player.posX, player.posY + player.getEyeHeight() * 0.6D, player.posZ);
        boolean inPowderSnow = world.getBlockState(feet).getBlock() == ModBlocks.POWDER_SNOW
                || world.getBlockState(belowFeet).getBlock() == ModBlocks.POWDER_SNOW
                || world.getBlockState(eyes).getBlock() == ModBlocks.POWDER_SNOW;

        return inPowderSnow && !wearsLeatherArmor(player);
    }

    private static boolean wearsLeatherArmor(EntityPlayer player) {
        return isLeather(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD))
                || isLeather(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST))
                || isLeather(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS))
                || isLeather(player.getItemStackFromSlot(EntityEquipmentSlot.FEET));
    }

    private static boolean isLeather(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() == Items.LEATHER_HELMET
                || stack.getItem() == Items.LEATHER_CHESTPLATE
                || stack.getItem() == Items.LEATHER_LEGGINGS
                || stack.getItem() == Items.LEATHER_BOOTS);
    }

    private static void renderPowderSnowOverlay(Minecraft minecraft, ScaledResolution resolution, float alpha) {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, Math.min(0.95F, 0.15F + alpha * 0.8F));
        minecraft.getTextureManager().bindTexture(POWDER_SNOW_OUTLINE);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, resolution.getScaledWidth(), resolution.getScaledHeight(), resolution.getScaledWidth(), resolution.getScaledHeight());
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
