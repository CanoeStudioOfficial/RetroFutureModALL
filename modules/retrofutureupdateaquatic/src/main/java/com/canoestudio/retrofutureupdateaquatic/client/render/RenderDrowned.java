package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDrowned extends RenderLiving<EntityDrowned> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RetroFutureUpdateAquatic.ID, "textures/entity/zombie/drowned.png");

    public RenderDrowned(RenderManager renderManager) {
        super(renderManager, new ModelZombie(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDrowned entity) {
        return TEXTURE;
    }
}
