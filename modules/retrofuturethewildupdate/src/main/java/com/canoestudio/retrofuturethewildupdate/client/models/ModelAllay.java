package com.canoestudio.retrofuturethewildupdate.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelAllay extends ModelBase {

    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer rightArm;
    private final ModelRenderer leftArm;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightLeg;
    private final ModelRenderer leftLeg;

    public ModelAllay() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.head = new ModelRenderer(this, 0, 0);
        this.head.setRotationPoint(0.0F, 12.0F, 0.0F);
        this.head.addBox(-2.5F, -5.0F, -2.5F, 5, 5, 5);

        this.body = new ModelRenderer(this, 0, 10);
        this.body.setRotationPoint(0.0F, 12.0F, 0.0F);
        this.body.addBox(-2.0F, 0.0F, -1.0F, 4, 6, 2);

        this.rightArm = new ModelRenderer(this, 16, 10);
        this.rightArm.setRotationPoint(-2.2F, 12.6F, 0.0F);
        this.rightArm.addBox(-1.0F, 0.0F, -1.0F, 1, 5, 2);

        this.leftArm = new ModelRenderer(this, 22, 10);
        this.leftArm.setRotationPoint(2.2F, 12.6F, 0.0F);
        this.leftArm.addBox(0.0F, 0.0F, -1.0F, 1, 5, 2);

        this.rightWing = new ModelRenderer(this, 0, 18);
        this.rightWing.setRotationPoint(-1.2F, 13.0F, 1.0F);
        this.rightWing.addBox(-5.0F, 0.0F, 0.0F, 5, 6, 0);

        this.leftWing = new ModelRenderer(this, 10, 18);
        this.leftWing.setRotationPoint(1.2F, 13.0F, 1.0F);
        this.leftWing.addBox(0.0F, 0.0F, 0.0F, 5, 6, 0);

        this.rightLeg = new ModelRenderer(this, 20, 18);
        this.rightLeg.setRotationPoint(-0.8F, 18.0F, 0.0F);
        this.rightLeg.addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1);

        this.leftLeg = new ModelRenderer(this, 24, 18);
        this.leftLeg.setRotationPoint(0.8F, 18.0F, 0.0F);
        this.leftLeg.addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.head.render(scale);
        this.body.render(scale);
        this.rightArm.render(scale);
        this.leftArm.render(scale);
        this.rightWing.render(scale);
        this.leftWing.render(scale);
        this.rightLeg.render(scale);
        this.leftLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        this.head.rotateAngleY = netHeadYaw * 0.017453292F * 0.35F;
        this.head.rotateAngleX = headPitch * 0.017453292F * 0.35F;
        float wing = MathHelper.sin(ageInTicks * 0.8F) * 0.65F + 0.4F;
        this.rightWing.rotateAngleY = -wing;
        this.leftWing.rotateAngleY = wing;
        this.rightWing.rotateAngleZ = 0.18F;
        this.leftWing.rotateAngleZ = -0.18F;
        float arm = MathHelper.sin(ageInTicks * 0.18F) * 0.08F;
        this.rightArm.rotateAngleX = -0.25F + arm;
        this.leftArm.rotateAngleX = -0.25F - arm;
        this.rightLeg.rotateAngleX = MathHelper.cos(ageInTicks * 0.18F) * 0.12F;
        this.leftLeg.rotateAngleX = MathHelper.cos(ageInTicks * 0.18F + (float) Math.PI) * 0.12F;
        this.body.rotateAngleX = MathHelper.sin(ageInTicks * 0.12F) * 0.04F;
    }
}
