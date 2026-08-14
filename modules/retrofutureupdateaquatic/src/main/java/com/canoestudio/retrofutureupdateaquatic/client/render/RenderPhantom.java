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
        // 原版 1.13 / 参考 Phantoms 的模型原点位于身体中心，必须保留这个
        // 偏移，否则翅膀会贴近碰撞箱底部，飞行姿态看起来像僵硬地平移。
        net.minecraft.client.renderer.GlStateManager.translate(0.0F, 1.3125F, 0.1875F);
    }

    @Override
    protected void applyRotations(EntityPhantom entityLiving, float ageInTicks, float rotationYaw,
            float partialTicks) {
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
        net.minecraft.client.renderer.GlStateManager.rotate(entityLiving.rotationPitch, -1.0F, 0.0F, 0.0F);
    }
}
