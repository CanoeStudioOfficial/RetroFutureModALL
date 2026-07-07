package com.canoestudio.retrofuturelushcave.contents.mobs.glowsquid;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGlowSquid extends RenderLiving<EntityGlowSquid> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/squid/glow_squid.png");

    public RenderGlowSquid(RenderManager renderManager) {
        super(renderManager, new ModelSquid(), 0.7F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGlowSquid entity) {
        return TEXTURE;
    }

    @Override
    protected void applyRotations(EntityGlowSquid entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        float pitch = entityLiving.prevSquidPitch + (entityLiving.squidPitch - entityLiving.prevSquidPitch) * partialTicks;
        float yaw = entityLiving.prevSquidYaw + (entityLiving.squidYaw - entityLiving.prevSquidYaw) * partialTicks;
        GlStateManager.translate(0.0F, 0.5F, 0.0F);
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.2F, 0.0F);
    }

    @Override
    protected float handleRotationFloat(EntityGlowSquid livingBase, float partialTicks) {
        return livingBase.lastTentacleAngle + (livingBase.tentacleAngle - livingBase.lastTentacleAngle) * partialTicks;
    }

    @Override
    protected void preRenderCallback(EntityGlowSquid entitylivingbaseIn, float partialTickTime) {
        float brightness = entitylivingbaseIn.getGlowBrightness(partialTickTime);
        GlStateManager.color(brightness, brightness, brightness, 1.0F);
    }

}
