package com.canoestudio.retrofutureupdateaquatic.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelPhantom extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer leftWing;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWing;
    private final ModelRenderer rightWingTip;
    private final ModelRenderer tail;
    private final ModelRenderer tailTip;

    public ModelPhantom() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 8);
        this.body.rotateAngleX = -0.1F;
        this.body.addBox(-3.0F, -2.0F, -8.0F, 5, 3, 9);

        this.tail = new ModelRenderer(this, 3, 20);
        this.body.addChild(this.tail);
        this.tail.setRotationPoint(0.0F, -2.0F, 1.0F);
        this.tail.addBox(-2.0F, 0.0F, 0.0F, 3, 2, 6);

        this.tailTip = new ModelRenderer(this, 4, 29);
        this.tail.addChild(this.tailTip);
        this.tailTip.setRotationPoint(0.0F, 0.5F, 6.0F);
        this.tailTip.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 6);

        this.leftWing = new ModelRenderer(this, 23, 12);
        this.body.addChild(this.leftWing);
        this.leftWing.setRotationPoint(2.0F, -2.0F, -8.0F);
        this.leftWing.rotateAngleZ = 0.1F;
        this.leftWing.addBox(0.0F, 0.0F, 0.0F, 6, 2, 9);

        this.leftWingTip = new ModelRenderer(this, 16, 24);
        this.leftWing.addChild(this.leftWingTip);
        this.leftWingTip.setRotationPoint(6.0F, 0.0F, 0.0F);
        this.leftWingTip.rotateAngleZ = 0.1F;
        this.leftWingTip.addBox(0.0F, 0.0F, 0.0F, 13, 1, 9);

        this.rightWing = new ModelRenderer(this, 23, 12);
        this.body.addChild(this.rightWing);
        this.rightWing.mirror = true;
        this.rightWing.setRotationPoint(-3.0F, -2.0F, -8.0F);
        this.rightWing.rotateAngleZ = -0.1F;
        this.rightWing.addBox(-6.0F, 0.0F, 0.0F, 6, 2, 9);

        this.rightWingTip = new ModelRenderer(this, 16, 24);
        this.rightWing.addChild(this.rightWingTip);
        this.rightWingTip.mirror = true;
        this.rightWingTip.setRotationPoint(-6.0F, 0.0F, 0.0F);
        this.rightWingTip.rotateAngleZ = -0.1F;
        this.rightWingTip.addBox(-13.0F, 0.0F, 0.0F, 13, 1, 9);

        this.head = new ModelRenderer(this, 0, 0);
        this.body.addChild(this.head);
        this.head.setRotationPoint(0.0F, 1.0F, -7.0F);
        this.head.rotateAngleX = 0.2F;
        this.head.addBox(-4.0F, -2.0F, -5.0F, 7, 3, 5);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        float t = entityIn.getEntityId() * 3.0F + ageInTicks * 7.448451F * (float)Math.PI / 180.0F;
        float flap = (float)Math.cos(t) * 16.0F * (float)Math.PI / 180.0F;
        this.leftWing.rotateAngleZ = flap;
        this.leftWingTip.rotateAngleZ = flap;
        this.rightWing.rotateAngleZ = -flap;
        this.rightWingTip.rotateAngleZ = -flap;
        float tailSwing = -(5.0F + (float)Math.cos(t * 2.0F) * 5.0F) * (float)Math.PI / 180.0F;
        this.tail.rotateAngleX = tailSwing;
        this.tailTip.rotateAngleX = tailSwing;
    }
}
