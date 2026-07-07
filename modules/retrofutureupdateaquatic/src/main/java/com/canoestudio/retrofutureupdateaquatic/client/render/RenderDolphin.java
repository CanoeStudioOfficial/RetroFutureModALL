package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelDolphin;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDolphin;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDolphin extends RenderLiving<EntityDolphin> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RetroFutureUpdateAquatic.ID, "textures/entity/dolphin/dolphin.png");

    public RenderDolphin(RenderManager renderManager) {
        super(renderManager, new ModelDolphin(), 0.6F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDolphin entity) {
        return TEXTURE;
    }
}
