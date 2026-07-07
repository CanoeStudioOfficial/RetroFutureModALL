package com.canoestudio.retrofutureupdateaquatic.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelDolphin extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer nose;
    private final ModelRenderer tail;
    private final ModelRenderer fluke;
    private final ModelRenderer finTop;
    private final ModelRenderer finLeft;
    private final ModelRenderer finRight;

    public ModelDolphin() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-4.0F, -3.0F, -8.0F, 8, 6, 16);
        this.body.setRotationPoint(0.0F, 18.0F, 0.0F);

        this.nose = new ModelRenderer(this, 0, 22);
        this.nose.addBox(-2.0F, -1.0F, -12.0F, 4, 2, 4);
        this.nose.setRotationPoint(0.0F, 18.0F, 0.0F);

        this.tail = new ModelRenderer(this, 0, 28);
        this.tail.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 8);
        this.tail.setRotationPoint(0.0F, 18.0F, 8.0F);

        this.fluke = new ModelRenderer(this, 22, 28);
        this.fluke.addBox(0.0F, -4.0F, 7.0F, 0, 8, 6);
        this.fluke.setRotationPoint(0.0F, 18.0F, 8.0F);

        this.finTop = new ModelRenderer(this, 48, 0);
        this.finTop.addBox(0.0F, -6.0F, -1.0F, 0, 4, 7);
        this.finTop.setRotationPoint(0.0F, 18.0F, 0.0F);

        this.finLeft = new ModelRenderer(this, 48, 12);
        this.finLeft.addBox(0.0F, 0.0F, -2.0F, 0, 5, 6);
        this.finLeft.setRotationPoint(4.0F, 19.0F, -3.0F);
        this.finLeft.rotateAngleZ = -0.8F;

        this.finRight = new ModelRenderer(this, 48, 12);
        this.finRight.addBox(0.0F, 0.0F, -2.0F, 0, 5, 6);
        this.finRight.setRotationPoint(-4.0F, 19.0F, -3.0F);
        this.finRight.rotateAngleZ = 0.8F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.nose.render(scale);
        this.tail.render(scale);
        this.fluke.render(scale);
        this.finTop.render(scale);
        this.finLeft.render(scale);
        this.finRight.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        float swing = MathHelper.sin(ageInTicks * 0.35F) * 0.28F;
        this.tail.rotateAngleY = swing;
        this.fluke.rotateAngleY = swing * 1.3F;
    }
}
