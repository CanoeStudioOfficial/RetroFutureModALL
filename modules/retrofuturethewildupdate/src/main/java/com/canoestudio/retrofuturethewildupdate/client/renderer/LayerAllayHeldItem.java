package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.entity.EntityAllay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;

public class LayerAllayHeldItem implements LayerRenderer<EntityAllay> {

    @Override
    public void doRenderLayer(EntityAllay entity, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.72F, -0.28F);
        GlStateManager.rotate(-18.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.55F, 0.55F, 0.55F);
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack,
            ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
