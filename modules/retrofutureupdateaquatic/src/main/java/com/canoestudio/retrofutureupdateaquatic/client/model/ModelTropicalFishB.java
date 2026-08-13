package com.canoestudio.retrofutureupdateaquatic.client.model;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTropicalFishB extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer tail;

    public ModelTropicalFishB() {
        this.textureWidth = 32;
        this.textureHeight = 32;
        this.body = new ModelRenderer(this, 0, 20);
        this.body.setRotationPoint(0.0F, 21.0F, -3.0F);
        this.body.addBox(-1.0F, -3.0F, 0.0F, 2, 6, 6);
        this.tail = new ModelRenderer(this, 14, 15);
        this.tail.setRotationPoint(0.0F, 0.0F, 6.0F);
        this.tail.addBox(0.0F, -3.0F, 0.0F, 0, 6, 5);
        this.body.addChild(this.tail);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.body.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale, Entity entity) {
        float flap = MathHelper.sin(ageInTicks * (((EntityAquaticFish)entity).isFlopping() ? 1.2F : 0.6F)) * 0.6F;
        this.body.rotateAngleY = flap * 0.2F;
        this.tail.rotateAngleY = flap * 0.6F;
    }
}
