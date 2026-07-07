package com.canoestudio.retrofutureupdateaquatic.client.model;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelAquaticFish extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer tail;
    private final ModelRenderer finTop;
    private final ModelRenderer finLeft;
    private final ModelRenderer finRight;

    public ModelAquaticFish() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.0F, -2.0F, -4.0F, 4, 4, 8);
        this.body.setRotationPoint(0.0F, 20.0F, 0.0F);

        this.tail = new ModelRenderer(this, 0, 13);
        this.tail.addBox(0.0F, -2.5F, 0.0F, 0, 5, 5);
        this.tail.setRotationPoint(0.0F, 20.0F, 4.0F);

        this.finTop = new ModelRenderer(this, 20, 0);
        this.finTop.addBox(0.0F, -4.0F, -1.0F, 0, 2, 4);
        this.finTop.setRotationPoint(0.0F, 20.0F, 0.0F);

        this.finLeft = new ModelRenderer(this, 20, 6);
        this.finLeft.addBox(0.0F, 0.0F, -1.0F, 0, 3, 3);
        this.finLeft.setRotationPoint(2.0F, 21.0F, -1.0F);
        this.finLeft.rotateAngleZ = -0.7F;

        this.finRight = new ModelRenderer(this, 20, 6);
        this.finRight.addBox(0.0F, 0.0F, -1.0F, 0, 3, 3);
        this.finRight.setRotationPoint(-2.0F, 21.0F, -1.0F);
        this.finRight.rotateAngleZ = 0.7F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
        this.tail.render(scale);
        this.finTop.render(scale);
        this.finLeft.render(scale);
        this.finRight.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn) {
        float speed = entityIn instanceof EntityAquaticFish && !entityIn.isInWater() ? 1.7F : 1.0F;
        this.tail.rotateAngleY = MathHelper.sin(ageInTicks * 0.6F * speed) * 0.45F;
    }
}
