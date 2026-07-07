package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.client.models.ModelAllay;
import com.canoestudio.retrofuturethewildupdate.entity.EntityAllay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAllay extends RenderLiving<EntityAllay> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/entity/allay/allay.png");

    public RenderAllay(RenderManager renderManager) {
        super(renderManager, new ModelAllay(), 0.25F);
        this.addLayer(new LayerAllayHeldItem());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAllay entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityAllay entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }
}
