package com.canoestudio.retrofutureupdateaquatic.world;

import com.canoestudio.retrofuturemccore.api.world.RetroBiomeSpawnRegistry;
import com.canoestudio.retrofuturemccore.api.world.RetroWorldgenRegistry;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDolphin;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import java.util.Locale;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public final class ModAquaticSpawns {

    private static boolean initialized;

    private ModAquaticSpawns() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        RetroBiomeSpawnRegistry.addSpawn(EntityAquaticFish.Cod.class, EnumCreatureType.WATER_CREATURE, 15, 3, 7,
            ModAquaticSpawns::isCodSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityAquaticFish.Salmon.class, EnumCreatureType.WATER_CREATURE, 15, 1, 5,
            ModAquaticSpawns::isSalmonSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityAquaticFish.Pufferfish.class, EnumCreatureType.WATER_CREATURE, 5, 1, 3,
            ModAquaticSpawns::isPufferfishSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityAquaticFish.Tropical.class, EnumCreatureType.WATER_CREATURE, 25, 4, 8,
            ModAquaticSpawns::isTropicalFishSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityDolphin.class, EnumCreatureType.WATER_CREATURE, 2, 1, 2,
            ModAquaticSpawns::isDolphinSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityTurtle.class, EnumCreatureType.CREATURE, 5, 2, 5,
            ModAquaticSpawns::isTurtleSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityDrowned.class, EnumCreatureType.MONSTER, 20, 1, 1,
            ModAquaticSpawns::isDrownedSpawnBiome);
    }

    private static boolean isCodSpawnBiome(Biome biome) {
        return isOcean(biome) && !isWarmOceanLike(biome);
    }

    private static boolean isSalmonSpawnBiome(Biome biome) {
        return RetroWorldgenRegistry.hasAnyType(biome, BiomeDictionary.Type.RIVER, BiomeDictionary.Type.OCEAN);
    }

    private static boolean isPufferfishSpawnBiome(Biome biome) {
        return isOcean(biome) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD);
    }

    private static boolean isTropicalFishSpawnBiome(Biome biome) {
        return isOcean(biome) && isWarmOceanLike(biome);
    }

    private static boolean isDolphinSpawnBiome(Biome biome) {
        return isOcean(biome) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD);
    }

    private static boolean isTurtleSpawnBiome(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)
            && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD);
    }

    private static boolean isDrownedSpawnBiome(Biome biome) {
        return RetroWorldgenRegistry.hasAnyType(biome, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER);
    }

    private static boolean isOcean(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
    }

    private static boolean isWarmOceanLike(Biome biome) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return biome.getDefaultTemperature() >= 0.8F
            || BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)
            || name.contains("warm")
            || name.contains("lukewarm")
            || name.contains("tropical")
            || name.contains("coral");
    }
}
