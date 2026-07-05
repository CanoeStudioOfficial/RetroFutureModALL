package com.canoestudio.retrofuturemc.contents.mobs.goat;

import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGoat extends RenderLiving<EntityGoat> {
    private static final ResourceLocation GOAT_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/goat/goat.png");
    private static final ResourceLocation BABY_GOAT_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/goat/goat_baby.png");

    public RenderGoat(RenderManager renderManager) {
        super(renderManager, new ModelGoat(), 0.7F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGoat entity) {
        return entity.isChild() ? BABY_GOAT_TEXTURE : GOAT_TEXTURE;
    }
}
