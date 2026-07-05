package com.canoestudio.retrofuturemc.contents.mobs.goat;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelGoat extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;
    private final ModelRenderer body;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;

    public ModelGoat() {
        textureWidth = 64;
        textureHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(1.0F, 14.0F, -6.0F);
        head.setTextureOffset(34, 46).addBox(-3.0F, -12.0F, -16.0F, 5, 7, 10);
        head.setTextureOffset(2, 61).addBox(-6.0F, -11.0F, -10.0F, 3, 2, 1);
        head.setTextureOffset(2, 61).addBox(2.0F, -11.0F, -10.0F, 3, 2, 1);
        head.setTextureOffset(23, 52).addBox(-0.5F, -3.0F, -14.0F, 0, 7, 5);

        leftHorn = new ModelRenderer(this, 12, 55);
        leftHorn.setRotationPoint(0.0F, 0.0F, 0.0F);
        leftHorn.addBox(-0.01F, -16.0F, -10.0F, 2, 7, 2);
        head.addChild(leftHorn);

        rightHorn = new ModelRenderer(this, 12, 55);
        rightHorn.setRotationPoint(0.0F, 0.0F, 0.0F);
        rightHorn.addBox(-2.99F, -16.0F, -10.0F, 2, 7, 2);
        head.addChild(rightHorn);

        body = new ModelRenderer(this, 1, 1);
        body.setRotationPoint(0.0F, 24.0F, 0.0F);
        body.addBox(-4.0F, -17.0F, -7.0F, 9, 11, 16);
        body.setTextureOffset(0, 28).addBox(-5.0F, -18.0F, -8.0F, 11, 14, 11);

        leftHindLeg = new ModelRenderer(this, 36, 29);
        leftHindLeg.setRotationPoint(1.0F, 14.0F, 4.0F);
        leftHindLeg.addBox(0.0F, 4.0F, 0.0F, 3, 6, 3);

        rightHindLeg = new ModelRenderer(this, 49, 29);
        rightHindLeg.setRotationPoint(-3.0F, 14.0F, 4.0F);
        rightHindLeg.addBox(0.0F, 4.0F, 0.0F, 3, 6, 3);

        leftFrontLeg = new ModelRenderer(this, 49, 2);
        leftFrontLeg.setRotationPoint(1.0F, 14.0F, -6.0F);
        leftFrontLeg.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);

        rightFrontLeg = new ModelRenderer(this, 35, 2);
        rightFrontLeg.setRotationPoint(-3.0F, 14.0F, -6.0F);
        rightFrontLeg.addBox(0.0F, 0.0F, 0.0F, 3, 10, 3);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        if (isChild) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.55F, 0.55F, 0.55F);
            GlStateManager.translate(0.0F, 20.0F * scale, 0.0F);
            renderParts(scale);
            GlStateManager.popMatrix();
        } else {
            renderParts(scale);
        }
    }

    private void renderParts(float scale) {
        head.render(scale);
        body.render(scale);
        leftHindLeg.render(scale);
        rightHindLeg.render(scale);
        leftFrontLeg.render(scale);
        rightFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        resetPose();

        EntityGoat goat = entityIn instanceof EntityGoat ? (EntityGoat)entityIn : null;
        float partialTicks = MathHelper.clamp(ageInTicks - entityIn.ticksExisted, 0.0F, 1.0F);
        float ramHeadRot = goat == null ? 0.0F : goat.getRammingXHeadRot(partialTicks);
        float walk = Math.min(limbSwingAmount, 1.0F);
        float walkSwing = limbSwing * 0.6662F;

        leftHindLeg.rotateAngleX = MathHelper.cos(walkSwing) * 0.95F * walk;
        rightHindLeg.rotateAngleX = MathHelper.cos(walkSwing + (float)Math.PI) * 0.95F * walk;
        leftFrontLeg.rotateAngleX = MathHelper.cos(walkSwing + (float)Math.PI) * 1.05F * walk;
        rightFrontLeg.rotateAngleX = MathHelper.cos(walkSwing) * 1.05F * walk;

        float bodyBob = MathHelper.sin(walkSwing * 2.0F) * 0.35F * walk;
        body.rotationPointY = 24.0F + bodyBob;
        head.rotationPointY = 14.0F + bodyBob * 0.45F;

        head.rotateAngleY = netHeadYaw * 0.017453292F * 0.75F;
        head.rotateAngleX = headPitch * 0.017453292F + ramHeadRot;

        if (isChild) {
            head.rotateAngleX += 0.3926991F;
        }

        applyAirborneAnimation(entityIn, walk);
        leftHorn.isHidden = goat != null && !goat.hasLeftHorn();
        rightHorn.isHidden = goat != null && !goat.hasRightHorn();
    }

    private void applyAirborneAnimation(Entity entityIn, float walk) {
        if (entityIn.onGround || entityIn.motionY < 0.02D || walk > 0.8F) {
            return;
        }

        float airborne = MathHelper.clamp((float)entityIn.motionY * 2.0F, 0.0F, 1.0F);
        body.rotateAngleX += 0.12F * airborne;
        head.rotateAngleX -= 0.08F * airborne;
        leftFrontLeg.rotateAngleX -= 0.45F * airborne;
        rightFrontLeg.rotateAngleX -= 0.45F * airborne;
        leftHindLeg.rotateAngleX += 0.35F * airborne;
        rightHindLeg.rotateAngleX += 0.35F * airborne;
    }

    private void resetPose() {
        head.rotationPointX = 1.0F;
        head.rotationPointY = 14.0F;
        head.rotationPointZ = -6.0F;
        head.rotateAngleX = 0.0F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;

        body.rotationPointY = 24.0F;
        body.rotateAngleX = 0.0F;
        body.rotateAngleY = 0.0F;
        body.rotateAngleZ = 0.0F;

        leftHindLeg.rotateAngleX = 0.0F;
        rightHindLeg.rotateAngleX = 0.0F;
        leftFrontLeg.rotateAngleX = 0.0F;
        rightFrontLeg.rotateAngleX = 0.0F;
    }
}
