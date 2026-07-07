package com.canoestudio.retrofuturelushcave.contents.mobs.goat;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelGoat extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer nose;
    private final ModelRenderer leftHorn;
    private final ModelRenderer rightHorn;
    private final ModelRenderer body;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightHindLeg;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer babyHead;
    private final ModelRenderer babyLeftHorn;
    private final ModelRenderer babyRightHorn;
    private final ModelRenderer babyBody;
    private final ModelRenderer babyLeftHindLeg;
    private final ModelRenderer babyRightHindLeg;
    private final ModelRenderer babyLeftFrontLeg;
    private final ModelRenderer babyRightFrontLeg;

    public ModelGoat() {
        textureWidth = 64;
        textureHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(1.0F, 14.0F, 0.0F);
        head.setTextureOffset(2, 61).addBox(-6.0F, -11.0F, -10.0F, 3, 2, 1);
        head.setTextureOffset(2, 61).addBox(2.0F, -11.0F, -10.0F, 3, 2, 1);
        head.setTextureOffset(23, 52).addBox(-0.5F, -3.0F, -14.0F, 0, 7, 5);

        nose = new ModelRenderer(this, 34, 46);
        nose.setRotationPoint(0.0F, -8.0F, -8.0F);
        nose.rotateAngleX = 0.9599F;
        nose.addBox(-3.0F, -4.0F, -8.0F, 5, 7, 10);
        head.addChild(nose);

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

        babyLeftHindLeg = new ModelRenderer(this, 29, 12);
        babyLeftHindLeg.setRotationPoint(1.5F, 19.5F, 3.0F);
        babyLeftHindLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        babyRightHindLeg = new ModelRenderer(this, 21, 12);
        babyRightHindLeg.setRotationPoint(-1.5F, 19.5F, 3.0F);
        babyRightHindLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        babyRightFrontLeg = new ModelRenderer(this, 21, 5);
        babyRightFrontLeg.setRotationPoint(-1.5F, 19.5F, -2.0F);
        babyRightFrontLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        babyLeftFrontLeg = new ModelRenderer(this, 29, 5);
        babyLeftFrontLeg.setRotationPoint(1.5F, 19.5F, -2.0F);
        babyLeftFrontLeg.addBox(-1.0F, -0.5F, -1.0F, 2, 5, 2);

        babyBody = new ModelRenderer(this, 0, 10);
        babyBody.setRotationPoint(0.0F, 17.8F, 0.0F);
        babyBody.addBox(-3.0F, -2.3F, -4.5F, 6, 5, 9);
        babyBody.setTextureOffset(0, 24).addBox(-2.5F, -2.2F, -4.0F, 5, 4, 8);

        babyHead = new ModelRenderer(this, 0, 0);
        babyHead.setRotationPoint(0.0F, 15.5F, -3.0F);
        babyHead.addBox(-2.0F, -3.8126F, -5.1548F, 4, 4, 6);

        babyRightHorn = new ModelRenderer(this, 24, 0);
        babyRightHorn.mirror = true;
        babyRightHorn.setRotationPoint(-1.5F, -1.5F, -1.0F);
        babyRightHorn.rotateAngleX = -0.3926991F;
        babyRightHorn.addBox(0.0F, -4.5F, 0.0F, 1, 2, 1);
        babyHead.addChild(babyRightHorn);

        babyLeftHorn = new ModelRenderer(this, 24, 0);
        babyLeftHorn.mirror = true;
        babyLeftHorn.setRotationPoint(-1.5F, -1.5F, -1.0F);
        babyLeftHorn.rotateAngleX = -0.3926991F;
        babyLeftHorn.addBox(2.0F, -4.5F, 0.0F, 1, 2, 1);
        babyHead.addChild(babyLeftHorn);

        ModelRenderer babyRightEar = new ModelRenderer(this, 0, 12);
        babyRightEar.mirror = true;
        babyRightEar.setRotationPoint(-1.7F, -2.3126F, 0.1452F);
        babyRightEar.rotateAngleY = -0.5236F;
        babyRightEar.addBox(-2.0F, -0.5F, -0.5F, 2, 1, 1);
        babyHead.addChild(babyRightEar);

        ModelRenderer babyLeftEar = new ModelRenderer(this, 0, 12);
        babyLeftEar.setRotationPoint(1.7F, -2.3126F, 0.1452F);
        babyLeftEar.rotateAngleY = 0.5236F;
        babyLeftEar.addBox(0.0F, -0.5F, -0.5F, 2, 1, 1);
        babyHead.addChild(babyLeftEar);

        ModelRenderer babyHeadMain = new ModelRenderer(this, 0, 0);
        babyHeadMain.setRotationPoint(0.0F, -1.3126F, -1.1548F);
        babyHeadMain.addBox(-2.0F, -2.5F, -4.0F, 4, 4, 6);
        babyHead.addChild(babyHeadMain);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        EntityGoat goat = entityIn instanceof EntityGoat ? (EntityGoat)entityIn : null;
        if (goat != null && goat.isChild()) {
            renderBabyParts(scale);
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

    private void renderBabyParts(float scale) {
        babyHead.render(scale);
        babyBody.render(scale);
        babyLeftHindLeg.render(scale);
        babyRightHindLeg.render(scale);
        babyLeftFrontLeg.render(scale);
        babyRightFrontLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        resetPose();

        EntityGoat goat = entityIn instanceof EntityGoat ? (EntityGoat)entityIn : null;
        float partialTicks = MathHelper.clamp(ageInTicks - entityIn.ticksExisted, 0.0F, 1.0F);
        float ramHeadRot = goat == null ? 0.0F : goat.getRammingXHeadRot(partialTicks);
        if (goat != null && goat.isChild()) {
            setBabyRotationAngles(limbSwing, limbSwingAmount, netHeadYaw, headPitch, ramHeadRot, entityIn);
        } else {
            setAdultRotationAngles(limbSwing, limbSwingAmount, netHeadYaw, headPitch, ramHeadRot, entityIn);
        }

        leftHorn.isHidden = goat != null && !goat.hasLeftHorn();
        rightHorn.isHidden = goat != null && !goat.hasRightHorn();
        babyLeftHorn.isHidden = goat != null && !goat.hasLeftHorn();
        babyRightHorn.isHidden = goat != null && !goat.hasRightHorn();
    }

    private void setAdultRotationAngles(float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float ramHeadRot, Entity entityIn) {
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

        applyAdultAirborneAnimation(entityIn, walk);
    }

    private void setBabyRotationAngles(float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float ramHeadRot, Entity entityIn) {
        float walk = Math.min(limbSwingAmount, 1.0F);
        float walkSwing = limbSwing * 0.6662F;

        babyLeftHindLeg.rotateAngleX = MathHelper.cos(walkSwing) * 0.95F * walk;
        babyRightHindLeg.rotateAngleX = MathHelper.cos(walkSwing + (float)Math.PI) * 0.95F * walk;
        babyLeftFrontLeg.rotateAngleX = MathHelper.cos(walkSwing + (float)Math.PI) * 1.05F * walk;
        babyRightFrontLeg.rotateAngleX = MathHelper.cos(walkSwing) * 1.05F * walk;

        float bodyBob = MathHelper.sin(walkSwing * 2.0F) * 0.22F * walk;
        babyBody.rotationPointY = 17.8F + bodyBob;
        babyHead.rotationPointY = 15.5F + bodyBob * 0.35F;

        babyHead.rotateAngleY = netHeadYaw * 0.017453292F * 0.75F;
        babyHead.rotateAngleX = ramHeadRot != 0.0F ? ramHeadRot : headPitch * 0.017453292F + 0.3926991F;

        applyBabyAirborneAnimation(entityIn, walk);
    }

    private void applyAdultAirborneAnimation(Entity entityIn, float walk) {
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

    private void applyBabyAirborneAnimation(Entity entityIn, float walk) {
        if (entityIn.onGround || entityIn.motionY < 0.02D || walk > 0.8F) {
            return;
        }

        float airborne = MathHelper.clamp((float)entityIn.motionY * 2.0F, 0.0F, 1.0F);
        babyBody.rotateAngleX += 0.10F * airborne;
        babyHead.rotateAngleX -= 0.07F * airborne;
        babyLeftFrontLeg.rotateAngleX -= 0.35F * airborne;
        babyRightFrontLeg.rotateAngleX -= 0.35F * airborne;
        babyLeftHindLeg.rotateAngleX += 0.28F * airborne;
        babyRightHindLeg.rotateAngleX += 0.28F * airborne;
    }

    private void resetPose() {
        head.rotationPointX = 1.0F;
        head.rotationPointY = 14.0F;
        head.rotationPointZ = 0.0F;
        head.rotateAngleX = 0.0F;
        head.rotateAngleY = 0.0F;
        head.rotateAngleZ = 0.0F;
        nose.rotateAngleX = 0.9599F;
        nose.rotateAngleY = 0.0F;
        nose.rotateAngleZ = 0.0F;

        body.rotationPointY = 24.0F;
        body.rotateAngleX = 0.0F;
        body.rotateAngleY = 0.0F;
        body.rotateAngleZ = 0.0F;

        leftHindLeg.rotateAngleX = 0.0F;
        rightHindLeg.rotateAngleX = 0.0F;
        leftFrontLeg.rotateAngleX = 0.0F;
        rightFrontLeg.rotateAngleX = 0.0F;

        babyHead.rotationPointX = 0.0F;
        babyHead.rotationPointY = 15.5F;
        babyHead.rotationPointZ = -3.0F;
        babyHead.rotateAngleX = 0.4363F;
        babyHead.rotateAngleY = 0.0F;
        babyHead.rotateAngleZ = 0.0F;

        babyBody.rotationPointY = 17.8F;
        babyBody.rotateAngleX = 0.0F;
        babyBody.rotateAngleY = 0.0F;
        babyBody.rotateAngleZ = 0.0F;

        babyLeftHindLeg.rotateAngleX = 0.0F;
        babyRightHindLeg.rotateAngleX = 0.0F;
        babyLeftFrontLeg.rotateAngleX = 0.0F;
        babyRightFrontLeg.rotateAngleX = 0.0F;
    }
}
