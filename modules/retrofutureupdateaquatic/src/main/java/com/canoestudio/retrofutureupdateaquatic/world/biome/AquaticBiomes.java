package com.canoestudio.retrofutureupdateaquatic.world.biome;

import java.util.Locale;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

/**
 * Compatibility helpers for aquatic systems.
 *
 * Oceanic Expanse deliberately does not register replacement warm/cold/deep
 * biomes. Keeping that rule here prevents this module from changing vanilla
 * biome selection through BiomeManager.oceanBiomes.
 */
public final class AquaticBiomes {

    /** Optional single-biome world type; it accepts existing registry IDs. */
    public static final net.minecraft.world.WorldType BUFFET = new BuffetWorldType();

    private AquaticBiomes() {
    }

    /** Source-compatible no-op retained for callers from earlier revisions. */
    public static void init() {
    }

    public static boolean isOceanOrBeach(Biome biome) {
        return biome != null && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
            || BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH));
    }

    public static boolean isWarm(Biome biome) {
        return biome != null && BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT);
    }

    public static boolean isLukewarm(Biome biome) {
        return isOceanOrBeach(biome) && !isCold(biome) && !isWarm(biome);
    }

    public static boolean isFrozen(Biome biome) {
        return biome != null && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
            || biome.getBiomeName().toLowerCase(Locale.ROOT).contains("frozen"));
    }

    public static boolean isCold(Biome biome) {
        return biome != null && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)
            || isFrozen(biome));
    }
}
