package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;

public class RenderMangroveChestBoat extends RenderMangroveBoat {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/entity/chest_boat/mangrove.png");
    private static final ResourceLocation CHEST_TEXTURE =
        new ResourceLocation("minecraft", "textures/entity/chest/normal.png");
    private final ModelChest chestModel = new ModelChest();

    public RenderMangroveChestBoat(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityBoat entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        this.renderChest(entity, x, y, z, entityYaw);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoat entity) {
        return TEXTURE;
    }

    private void renderChest(EntityBoat entity, double x, double y, double z, float entityYaw) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate((float) x, (float) y + 0.18F, (float) z);
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.55F, 0.55F, 0.55F);
        GlStateManager.translate(-0.5F, -0.15F, -0.25F);
        this.bindTexture(CHEST_TEXTURE);
        this.chestModel.renderAll();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
