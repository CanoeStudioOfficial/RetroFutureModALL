package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelTropicalFishA;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelTropicalFishB;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

public class LayerTropicalFish<T extends EntityAquaticFish> implements LayerRenderer<T> {

    private final RenderAquaticFish<T> renderer;
    private final ModelTropicalFishA modelA = new ModelTropicalFishA();
    private final ModelTropicalFishB modelB = new ModelTropicalFishB();

    public LayerTropicalFish(RenderAquaticFish<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(T fish, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (fish.isInvisible()) {
            return;
        }
        int variant = fish.getTropicalFishVariant();
        String modelName = (variant & 255) == 0 ? "a" : "b";
        int pattern = (variant >> 8) & 255;
        if (pattern > 5) {
            return;
        }
        renderer.bindTexture(new ResourceLocation(RetroFutureUpdateAquatic.ID,
            "textures/entity/fish/tropical_fish_" + modelName + "_p" + pattern + ".png"));
        float[] color = EnumDyeColor.byMetadata(variant >> 24 & 255).getColorComponentValues();
        GlStateManager.color(color[0], color[1], color[2]);
        if ((variant & 255) == 0) {
            modelA.setModelAttributes(renderer.getMainModel());
            modelA.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, fish);
            modelA.render(fish, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        } else {
            modelB.setModelAttributes(renderer.getMainModel());
            modelB.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, fish);
            modelB.render(fish, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
