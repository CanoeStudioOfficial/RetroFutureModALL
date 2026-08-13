package com.canoestudio.retrofutureupdateaquatic.client.model;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

/** Three visual sizes used by the 1.13 pufferfish puff state. */
public class ModelPufferfish extends ModelBase {

    private final ModelRenderer smallBody;
    private final ModelRenderer mediumBody;
    private final ModelRenderer largeBody;
    private final ModelRenderer smallTail;
    private final ModelRenderer mediumTail;
    private final ModelRenderer largeTail;
    private final ModelRenderer smallLeftFin;
    private final ModelRenderer smallRightFin;
    private final ModelRenderer mediumLeftFin;
    private final ModelRenderer mediumRightFin;
    private final ModelRenderer largeLeftFin;
    private final ModelRenderer largeRightFin;
    private final ModelRenderer largeSpines;

    public ModelPufferfish() {
        this.textureWidth = 32;
        this.textureHeight = 48;

        this.smallBody = new ModelRenderer(this, 0, 0);
        this.smallBody.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.smallBody.addBox(-1.5F, -1.0F, -2.0F, 3, 2, 4);
        this.smallTail = flatBox(17, 4, -1.5F, 0.0F, 0.0F, 3, 0, 3);
        this.smallTail.setRotationPoint(0.0F, 0.0F, 2.0F);
        this.smallBody.addChild(this.smallTail);
        this.smallLeftFin = fin(20, -2, 1.5F, 0.0F, 0.0F, -0.7853982F);
        this.smallRightFin = fin(20, 0, -1.5F, 0.0F, 0.0F, 0.7853982F);
        this.smallBody.addChild(this.smallLeftFin);
        this.smallBody.addChild(this.smallRightFin);

        this.mediumBody = new ModelRenderer(this, 0, 0);
        this.mediumBody.setRotationPoint(0.0F, 20.5F, 0.0F);
        this.mediumBody.addBox(-2.5F, -2.5F, -2.5F, 5, 5, 5);
        this.mediumTail = flatBox(17, 4, -1.5F, 0.0F, 0.0F, 3, 0, 3);
        this.mediumTail.setRotationPoint(0.0F, -0.5F, 2.5F);
        this.mediumBody.addChild(this.mediumTail);
        this.mediumLeftFin = fin(20, -2, 2.5F, -1.5F, -0.5F, -0.7853982F);
        this.mediumRightFin = fin(20, 0, -2.5F, -1.5F, -0.5F, 0.7853982F);
        this.mediumBody.addChild(this.mediumLeftFin);
        this.mediumBody.addChild(this.mediumRightFin);
        addMediumSpine(this.mediumBody, -0.7853982F, 0.0F, 0.0F);
        addMediumSpine(this.mediumBody, 0.7853982F, 0.0F, 0.0F);
        addMediumSpine(this.mediumBody, 0.0F, -1.5707963F, -0.7853982F);
        addMediumSpine(this.mediumBody, 0.0F, 1.5707963F, 0.7853982F);

        this.largeBody = new ModelRenderer(this, 0, 0);
        this.largeBody.setRotationPoint(0.0F, 19.0F, 0.0F);
        this.largeBody.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        this.largeTail = flatBox(21, 4, -1.5F, 0.0F, 0.0F, 3, 0, 3);
        this.largeTail.setRotationPoint(0.0F, -1.0F, 4.0F);
        this.largeBody.addChild(this.largeTail);
        this.largeLeftFin = fin(24, -2, 4.0F, -2.0F, -1.0F, -0.7853982F);
        this.largeRightFin = fin(24, 0, -4.0F, -2.0F, -1.0F, 0.7853982F);
        this.largeBody.addChild(this.largeLeftFin);
        this.largeBody.addChild(this.largeRightFin);
        this.largeSpines = new ModelRenderer(this, 0, 16);
        this.largeSpines.addBox(-4.0F, -6.5F, 0.0F, 8, 13, 0);
        this.largeBody.addChild(this.largeSpines);
        ModelRenderer spine2 = new ModelRenderer(this, 0, 16);
        spine2.addBox(-4.0F, -6.5F, 0.0F, 8, 13, 0);
        spine2.rotateAngleY = 1.5707963F;
        this.largeBody.addChild(spine2);
        ModelRenderer spine3 = new ModelRenderer(this, 0, 29);
        spine3.addBox(-6.5F, -4.0F, 0.0F, 13, 8, 0);
        spine3.rotateAngleY = 0.7853982F;
        this.largeBody.addChild(spine3);
        ModelRenderer spine4 = new ModelRenderer(this, 0, 29);
        spine4.addBox(-6.4F, -4.0F, 0.0F, 13, 8, 0);
        spine4.rotateAngleY = 2.3561945F;
        this.largeBody.addChild(spine4);
    }

    private ModelRenderer flatBox(int textureX, int textureY, float x, float y, float z,
            int width, int height, int depth) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.addBox(x, y, z, width, height, depth);
        return part;
    }

    private ModelRenderer fin(int textureX, int textureY, float x, float y, float z, float angle) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.setRotationPoint(x, y, z);
        part.addBox(0.0F, 0.0F, -1.0F, 0, 2, 2);
        part.rotateAngleZ = angle;
        return part;
    }

    private void addMediumSpine(ModelRenderer parent, float x, float y, float z) {
        ModelRenderer spine = new ModelRenderer(this, 0, 10);
        spine.addBox(-2.5F, -4.5F, 0.0F, 5, 9, 0);
        spine.rotateAngleX = x;
        spine.rotateAngleY = y;
        spine.rotateAngleZ = z;
        parent.addChild(spine);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        EntityAquaticFish.Pufferfish fish = (EntityAquaticFish.Pufferfish) entity;
        if (fish.getPuffState() == 0) {
            this.smallBody.render(scale);
        } else if (fish.getPuffState() == 1) {
            this.mediumBody.render(scale);
        } else {
            this.largeBody.render(scale);
        }
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float swing, float speed, float partialTicks) {
        EntityAquaticFish.Pufferfish fish = (EntityAquaticFish.Pufferfish) entity;
        float tail = MathHelper.sin(fish.ticksExisted * (fish.isFlopping() ? 1.2F : 0.6F)) * 0.4F;
        this.smallTail.rotateAngleY = tail;
        this.mediumTail.rotateAngleY = tail;
        this.largeTail.rotateAngleY = tail;
        float finAngle = MathHelper.sin(fish.ticksExisted * (fish.isFlopping() ? 0.4F : 0.1F)) * 0.3F + 1.0F;
        this.smallLeftFin.rotateAngleZ = -finAngle;
        this.smallRightFin.rotateAngleZ = finAngle;
        this.mediumLeftFin.rotateAngleZ = -finAngle;
        this.mediumRightFin.rotateAngleZ = finAngle;
        this.largeLeftFin.rotateAngleZ = -finAngle;
        this.largeRightFin.rotateAngleZ = finAngle;
    }
}
