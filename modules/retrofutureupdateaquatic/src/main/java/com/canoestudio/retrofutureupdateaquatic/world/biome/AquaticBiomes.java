package com.canoestudio.retrofutureupdateaquatic.world.biome;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Biomes;
import net.minecraft.world.gen.layer.GenLayer;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class AquaticBiomes {

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
    private static boolean terrainHandlerRegistered;

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

        if (!terrainHandlerRegistered) {
            MinecraftForge.TERRAIN_GEN_BUS.register(new AquaticBiomeGenerationHandler());
            terrainHandlerRegistered = true;
        }
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

    /**
     * Forge 1.12 has no 1.13 ocean climate layer.  Decorate the vanilla ocean
     * layer at world creation time so these registered biomes actually occur
     * in newly generated Overworld chunks.
     */
    private static final class AquaticBiomeGenerationHandler {

        @SubscribeEvent
        public void onInitBiomeGens(WorldTypeEvent.InitBiomeGens event) {
            GenLayer[] original = event.getNewBiomeGens();
            if (original == null || original.length < 2) {
                return;
            }

            GenLayer[] replacement = original.clone();
            replacement[0] = new GenLayerAquaticOceans(1729L, replacement[0]);
            replacement[1] = new GenLayerAquaticOceans(1731L, replacement[1]);
            if (replacement.length > 2 && replacement[2] != null) {
                replacement[2] = new GenLayerAquaticOceans(1733L, replacement[2]);
            }
            for (GenLayer layer : replacement) {
                if (layer != null) {
                    layer.initWorldGenSeed(event.getSeed());
                }
            }
            event.setNewBiomeGens(replacement);
        }
    }

    private static final class GenLayerAquaticOceans extends GenLayer {

        private GenLayerAquaticOceans(long seed, GenLayer parent) {
            super(seed);
            this.parent = parent;
        }

        @Override
        public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
            int[] source = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
            int[] result = net.minecraft.world.gen.layer.IntCache.getIntCache(areaWidth * areaHeight);
            int oceanId = Biome.getIdForBiome(Biomes.OCEAN);
            int deepOceanId = Biome.getIdForBiome(Biomes.DEEP_OCEAN);
            int frozenOceanId = Biome.getIdForBiome(Biomes.FROZEN_OCEAN);

            for (int z = 0; z < areaHeight; z++) {
                for (int x = 0; x < areaWidth; x++) {
                    int index = x + z * areaWidth;
                    int biomeId = source[index];
                    this.initChunkSeed(x + areaX, z + areaY);
                    if (biomeId == oceanId) {
                        result[index] = Biome.getIdForBiome(shallowOceanVariant(this.nextInt(100)));
                    } else if (biomeId == deepOceanId) {
                        result[index] = Biome.getIdForBiome(deepOceanVariant(this.nextInt(100)));
                    } else if (biomeId == frozenOceanId) {
                        result[index] = Biome.getIdForBiome(FROZEN_OCEAN);
                    } else {
                        result[index] = biomeId;
                    }
                }
            }
            return result;
        }

        private Biome shallowOceanVariant(int roll) {
            if (roll < 30) {
                return WARM_OCEAN;
            }
            if (roll < 60) {
                return LUKEWARM_OCEAN;
            }
            if (roll < 88) {
                return COLD_OCEAN;
            }
            return FROZEN_OCEAN;
        }

        private Biome deepOceanVariant(int roll) {
            if (roll < 35) {
                return DEEP_LUKEWARM_OCEAN;
            }
            if (roll < 75) {
                return DEEP_COLD_OCEAN;
            }
            return DEEP_FROZEN_OCEAN;
        }
    }
}
