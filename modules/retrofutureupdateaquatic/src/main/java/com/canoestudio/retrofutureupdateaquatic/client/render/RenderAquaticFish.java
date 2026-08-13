package com.canoestudio.retrofutureupdateaquatic.client.render;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelAquaticFish;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelPufferfish;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelTropicalFishA;
import com.canoestudio.retrofutureupdateaquatic.client.model.ModelTropicalFishB;
import com.canoestudio.retrofutureupdateaquatic.entity.AquaticFishType;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

public class RenderAquaticFish<T extends EntityAquaticFish> extends RenderLiving<T> {

    private static final ModelTropicalFishA TROPICAL_MODEL_A = new ModelTropicalFishA();
    private static final ModelTropicalFishB TROPICAL_MODEL_B = new ModelTropicalFishB();
    private static final ModelPufferfish PUFFERFISH_MODEL = new ModelPufferfish();
    private static final ResourceLocation PUFFERFISH_TEXTURE_1 = new ResourceLocation(
        RetroFutureUpdateAquatic.ID, "textures/entity/fish/pufferfish1.png");
    private static final ResourceLocation PUFFERFISH_TEXTURE_2 = new ResourceLocation(
        RetroFutureUpdateAquatic.ID, "textures/entity/fish/pufferfish2.png");
    private static final ResourceLocation PUFFERFISH_TEXTURE_3 = new ResourceLocation(
        RetroFutureUpdateAquatic.ID, "textures/entity/fish/pufferfish3.png");
    private final AquaticFishType fishType;

    public RenderAquaticFish(RenderManager renderManager, AquaticFishType fishType) {
        super(renderManager, fishType == AquaticFishType.TROPICAL_FISH
            ? TROPICAL_MODEL_A : fishType == AquaticFishType.PUFFERFISH
                ? PUFFERFISH_MODEL : new ModelAquaticFish(),
            fishType == AquaticFishType.PUFFERFISH ? 0.3F : 0.2F);
        this.fishType = fishType;
        if (fishType == AquaticFishType.TROPICAL_FISH) {
            this.addLayer(new LayerTropicalFish<T>(this));
        }
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        if (this.fishType == AquaticFishType.TROPICAL_FISH) {
            this.mainModel = (entity.getTropicalFishVariant() & 255) == 0
                ? TROPICAL_MODEL_A : TROPICAL_MODEL_B;
            float[] color = EnumDyeColor.byMetadata(entity.getTropicalFishVariant() >> 16 & 255)
                .getColorComponentValues();
            GlStateManager.color(color[0], color[1], color[2]);
        } else if (this.fishType == AquaticFishType.SALMON) {
            GlStateManager.scale(1.25F, 1.25F, 1.25F);
        } else if (this.fishType == AquaticFishType.PUFFERFISH) {
            GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        if (this.fishType == AquaticFishType.PUFFERFISH) {
            switch (((EntityAquaticFish.Pufferfish) entity).getPuffState()) {
                case 1:
                    return PUFFERFISH_TEXTURE_2;
                case 2:
                    return PUFFERFISH_TEXTURE_3;
                default:
                    return PUFFERFISH_TEXTURE_1;
            }
        }
        if (this.fishType == AquaticFishType.TROPICAL_FISH) {
            String model = (entity.getTropicalFishVariant() & 255) == 0 ? "a" : "b";
            return new ResourceLocation(RetroFutureUpdateAquatic.ID,
                "textures/entity/fish/tropical_fish_" + model + ".png");
        }
        return new ResourceLocation(RetroFutureUpdateAquatic.ID,
            "textures/entity/fish/" + this.fishType.getId() + ".png");
    }
}
