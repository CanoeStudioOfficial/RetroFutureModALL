package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelTurtle;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTurtle extends RenderLiving<EntityTurtle> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RetroFutureUpdateAquatic.ID, "textures/entity/turtle/turtle.png");

    public RenderTurtle(RenderManager renderManager) {
        super(renderManager, new ModelTurtle(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTurtle entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityTurtle entity, float partialTickTime) {
        if (entity.isChild()) {
            GlStateManager.scale(0.35F, 0.35F, 0.35F);
        }
    }
}
