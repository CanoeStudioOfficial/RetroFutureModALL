package com.canoestudio.retrofutureupdateaquatic.world.biome;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

/** A 1.13 ocean climate variant represented using the 1.12.2 Biome API. */
public class AquaticBiome extends Biome {

    public enum Kind {
        WARM_OCEAN("warm_ocean", -1.0F, 0.1F, 0.8F, 0x45ADF2),
        LUKEWARM_OCEAN("lukewarm_ocean", -1.0F, 0.1F, 0.6F, 0x45ADF2),
        COLD_OCEAN("cold_ocean", -1.0F, 0.1F, 0.0F, 0x3F76E4),
        FROZEN_OCEAN("frozen_ocean", -1.0F, 0.1F, 0.0F, 0x3938C9),
        DEEP_LUKEWARM_OCEAN("deep_lukewarm_ocean", -1.8F, 0.1F, 0.6F, 0x45ADF2),
        DEEP_COLD_OCEAN("deep_cold_ocean", -1.8F, 0.1F, 0.0F, 0x3F76E4),
        DEEP_FROZEN_OCEAN("deep_frozen_ocean", -1.8F, 0.1F, 0.0F, 0x3938C9);

        private final String id;
        private final float baseHeight;
        private final float heightVariation;
        private final float temperature;
        private final int waterColor;

        Kind(String id, float baseHeight, float heightVariation, float temperature, int waterColor) {
            this.id = id;
            this.baseHeight = baseHeight;
            this.heightVariation = heightVariation;
            this.temperature = temperature;
            this.waterColor = waterColor;
        }
    }

    private final Kind kind;

    public AquaticBiome(Kind kind) {
        super(new BiomeProperties(kind.id)
            .setBaseHeight(kind.baseHeight)
            .setHeightVariation(kind.heightVariation)
            .setTemperature(kind.temperature)
            .setRainfall(0.5F)
            .setWaterColor(kind.waterColor));
        this.kind = kind;
        this.setRegistryName(RetroFutureUpdateAquatic.ID, kind.id);
        this.topBlock = Blocks.GRAVEL.getDefaultState();
        this.fillerBlock = Blocks.STONE.getDefaultState();
        this.decorator.treesPerChunk = -999;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
        this.decorator.mushroomsPerChunk = 0;
        this.decorator.reedsPerChunk = 0;
        this.decorator.clayPerChunk = 0;
        this.decorator.waterlilyPerChunk = 0;
    }

    public Kind getKind() {
        return this.kind;
    }
}
