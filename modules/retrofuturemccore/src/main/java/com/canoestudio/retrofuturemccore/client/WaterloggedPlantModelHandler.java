package com.canoestudio.retrofuturemccore.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidCompat;
import com.canoestudio.retrofuturemccore.client.model.WaterloggedPlantBakedModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Installs the variable-height water model only for the local fallback path. */
public final class WaterloggedPlantModelHandler {
    private static final Set<String> MODEL_PATHS = new HashSet<>(Arrays.asList(
            "kelp", "seagrass", "sea_pickle", "small_dripleaf", "big_dripleaf",
            "big_dripleaf_waterlogged",
            "big_dripleaf_stem", "hanging_roots", "glow_lichen", "glow_lichen_1",
            "glow_lichen_2", "glow_lichen_3", "glow_lichen_4", "glow_lichen_5",
            "glow_lichen_6", "glow_lichen_7", "small_amethyst_bud", "medium_amethyst_bud",
            "large_amethyst_bud", "amethyst_cluster", "conduit"));

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        if (RetroFluidCompat.isFluidloggedAvailable()) {
            return;
        }
        for (ModelResourceLocation location : event.getModelRegistry().getKeys()) {
            if (!location.getNamespace().startsWith("retrofuture")
                    || !MODEL_PATHS.contains(location.getPath())) {
                continue;
            }
            IBakedModel model = event.getModelRegistry().getObject(location);
            if (model != null && !(model instanceof WaterloggedPlantBakedModel)) {
                event.getModelRegistry().putObject(location, new WaterloggedPlantBakedModel(model));
            }
        }
    }
}
