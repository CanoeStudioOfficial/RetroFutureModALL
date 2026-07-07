package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelPhantom;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderPhantom extends RenderLiving<EntityPhantom> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RetroFutureUpdateAquatic.ID, "textures/entity/phantom/phantom.png");

    public RenderPhantom(RenderManager renderManager) {
        super(renderManager, new ModelPhantom(), 0.75F);
        this.addLayer(new LayerPhantomEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPhantom entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityPhantom entity, float partialTickTime) {
        float scale = 1.0F + 0.15F * entity.getPhantomSize();
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(EntityPhantom entityLiving, float ageInTicks, float rotationYaw,
            float partialTicks) {
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
        net.minecraft.client.renderer.GlStateManager.rotate(entityLiving.rotationPitch, -1.0F, 0.0F, 0.0F);
    }
}
