package com.canoestudio.retrofutureupdateaquatic.world.biome;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.util.ResourceLocation;

/**
 * Forge 1.12.2 equivalent of the 1.13 Buffet world type.  The generator
 * option is a biome registry id (or "biome=<id>"); this keeps the selection
 * deterministic and allows dedicated servers to choose a single biome even
 * though vanilla 1.12's create-world screen has no Buffet selector widget.
 */
public class BuffetWorldType extends WorldType {

    public BuffetWorldType() {
        super("buffet");
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        Biome biome = resolveBiome(world.getWorldInfo().getGeneratorOptions());
        return new BiomeProviderSingle(biome);
    }

    private Biome resolveBiome(String options) {
        String name = options == null ? "" : options.trim();
        if (name.startsWith("biome=")) {
            name = name.substring("biome=".length()).trim();
        }
        if (name.isEmpty()) {
            return AquaticBiomes.WARM_OCEAN;
        }
        try {
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(name));
            return biome == null ? Biomes.OCEAN : biome;
        } catch (RuntimeException invalidId) {
            RetroFutureUpdateAquatic.LOGGER.warn("Invalid Buffet biome '{}'; using ocean", name);
            return Biomes.OCEAN;
        }
    }
}
