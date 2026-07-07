package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelAquaticFish;
import com.canoestudio.retrofutureupdateaquatic.entity.AquaticFishType;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAquaticFish<T extends EntityAquaticFish> extends RenderLiving<T> {

    private final AquaticFishType fishType;

    public RenderAquaticFish(RenderManager renderManager, AquaticFishType fishType) {
        super(renderManager, new ModelAquaticFish(), 0.2F);
        this.fishType = fishType;
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return new ResourceLocation(RetroFutureUpdateAquatic.ID,
            "textures/entity/fish/" + this.fishType.getId() + ".png");
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        if (this.fishType == AquaticFishType.SALMON) {
            GlStateManager.scale(1.25F, 1.25F, 1.25F);
        } else if (this.fishType == AquaticFishType.PUFFERFISH) {
            GlStateManager.scale(1.05F, 1.05F, 1.05F);
        }
    }
}
