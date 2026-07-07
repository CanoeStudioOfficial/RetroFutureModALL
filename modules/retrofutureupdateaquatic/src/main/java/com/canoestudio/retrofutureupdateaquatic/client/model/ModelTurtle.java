package com.canoestudio.retrofutureupdateaquatic.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTurtle extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftFrontLeg;
    private final ModelRenderer rightFrontLeg;
    private final ModelRenderer leftHindLeg;
    private final ModelRenderer rightHindLeg;

    public ModelTurtle() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-6.0F, -2.0F, -8.0F, 12, 4, 16);
        this.body.setRotationPoint(0.0F, 21.0F, 0.0F);

        this.head = new ModelRenderer(this, 0, 20);
        this.head.addBox(-2.5F, -1.5F, -4.0F, 5, 3, 4);
        this.head.setRotationPoint(0.0F, 21.0F, -8.0F);

        this.leftFrontLeg = new ModelRenderer(this, 20, 20);
        this.leftFrontLeg.addBox(0.0F, -1.0F, -3.0F, 2, 2, 6);
        this.leftFrontLeg.setRotationPoint(5.0F, 22.0F, -4.0F);

        this.rightFrontLeg = new ModelRenderer(this, 20, 20);
        this.rightFrontLeg.addBox(-2.0F, -1.0F, -3.0F, 2, 2, 6);
        this.rightFrontLeg.setRotationPoint(-5.0F, 22.0F, -4.0F);

        this.leftHindLeg = new ModelRenderer(this, 36, 20);
        this.leftHindLeg.addBox(0.0F, -1.0F, -3.0F, 2, 2, 6);
        this.leftHindLeg.setRotationPoint(5.0F, 22.0F, 5.0F);

        this.rightHindLeg = new ModelRenderer(this, 36, 20);
        this.rightHindLeg.addBox(-2.0F, -1.0F, -3.0F, 2, 2, 6);
        this.rightHindLeg.setRotationPoint(-5.0F, 22.0F, 5.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.head.render(scale);
        this.leftFrontLeg.render(scale);
        this.rightFrontLeg.render(scale);
        this.leftHindLeg.render(scale);
        this.rightHindLeg.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        float swim = entityIn.isInWater() ? 0.75F : 0.35F;
        this.leftFrontLeg.rotateAngleY = MathHelper.cos(ageInTicks * swim) * 0.45F;
        this.rightFrontLeg.rotateAngleY = -this.leftFrontLeg.rotateAngleY;
        this.leftHindLeg.rotateAngleY = -this.leftFrontLeg.rotateAngleY * 0.6F;
        this.rightHindLeg.rotateAngleY = this.leftFrontLeg.rotateAngleY * 0.6F;
    }
}
