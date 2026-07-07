package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;

public class RenderMangroveBoat extends RenderBoat {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/entity/boat/mangrove.png");

    public RenderMangroveBoat(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoat entity) {
        return TEXTURE;
    }
}
