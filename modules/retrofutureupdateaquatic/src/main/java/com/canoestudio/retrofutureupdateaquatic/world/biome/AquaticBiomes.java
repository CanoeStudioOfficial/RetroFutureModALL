package com.canoestudio.retrofutureupdateaquatic.world.biome;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class AquaticBiomes {

    /** Registered through Forge's extensible WorldType array at class load. */
    public static final net.minecraft.world.WorldType BUFFET = new BuffetWorldType();

    public static final Biome WARM_OCEAN = new AquaticBiome(AquaticBiome.Kind.WARM_OCEAN);
    public static final Biome LUKEWARM_OCEAN = new AquaticBiome(AquaticBiome.Kind.LUKEWARM_OCEAN);
    public static final Biome COLD_OCEAN = new AquaticBiome(AquaticBiome.Kind.COLD_OCEAN);
    public static final Biome FROZEN_OCEAN = new AquaticBiome(AquaticBiome.Kind.FROZEN_OCEAN);
    public static final Biome DEEP_LUKEWARM_OCEAN = new AquaticBiome(AquaticBiome.Kind.DEEP_LUKEWARM_OCEAN);
    public static final Biome DEEP_COLD_OCEAN = new AquaticBiome(AquaticBiome.Kind.DEEP_COLD_OCEAN);
    public static final Biome DEEP_FROZEN_OCEAN = new AquaticBiome(AquaticBiome.Kind.DEEP_FROZEN_OCEAN);

    private static final Biome[] ALL = {
        WARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, FROZEN_OCEAN,
        DEEP_LUKEWARM_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN
    };

    private static boolean initialized;

    private AquaticBiomes() {
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(ALL);
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerAquaticBiome(WARM_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER, BiomeDictionary.Type.HOT);
        registerAquaticBiome(LUKEWARM_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER);
        registerAquaticBiome(COLD_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER, BiomeDictionary.Type.COLD);
        registerAquaticBiome(FROZEN_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER, BiomeDictionary.Type.COLD,
            BiomeDictionary.Type.SNOWY);
        registerAquaticBiome(DEEP_LUKEWARM_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER);
        registerAquaticBiome(DEEP_COLD_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER, BiomeDictionary.Type.COLD);
        registerAquaticBiome(DEEP_FROZEN_OCEAN,
            BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER, BiomeDictionary.Type.COLD,
            BiomeDictionary.Type.SNOWY);

        for (Biome biome : ALL) {
            if (!BiomeManager.oceanBiomes.contains(biome)) {
                BiomeManager.oceanBiomes.add(biome);
            }
        }
    }

    /** OE decorates vanilla OCEAN/BEACH biomes instead of rewriting GenLayer output. */
    public static boolean isOceanOrBeach(Biome biome) {
        return biome != null && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
            || BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH));
    }

    public static boolean isWarm(Biome biome) {
        return biome == WARM_OCEAN;
    }

    public static boolean isLukewarm(Biome biome) {
        return biome == LUKEWARM_OCEAN || biome == DEEP_LUKEWARM_OCEAN;
    }

    public static boolean isFrozen(Biome biome) {
        return biome == FROZEN_OCEAN || biome == DEEP_FROZEN_OCEAN
            || biome.getBiomeName().toLowerCase(java.util.Locale.ROOT).contains("frozen");
    }

    public static boolean isCold(Biome biome) {
        return biome == COLD_OCEAN || biome == DEEP_COLD_OCEAN || isFrozen(biome);
    }

    private static void registerAquaticBiome(Biome biome, BiomeDictionary.Type... types) {
        BiomeDictionary.addTypes(biome, types);
        BiomeManager.addSpawnBiome(biome);
    }
}
