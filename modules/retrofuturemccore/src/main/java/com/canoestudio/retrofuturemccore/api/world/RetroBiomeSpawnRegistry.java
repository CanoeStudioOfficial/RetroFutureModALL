package com.canoestudio.retrofuturemccore.api.world;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntityRegistry;
import com.canoestudio.retrofuturemccore.api.entity.RetroEntitySpawn;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

public final class RetroBiomeSpawnRegistry {

    private RetroBiomeSpawnRegistry() {
    }

    public static void registerBiome(Biome biome, BiomeManager.BiomeType biomeType, int weight,
            boolean addSpawnBiome, BiomeDictionary.Type... dictionaryTypes) {
        if (biome == null) {
            return;
        }
        if (dictionaryTypes != null && dictionaryTypes.length > 0) {
            BiomeDictionary.addTypes(biome, dictionaryTypes);
        }
        if (biomeType != null && weight > 0) {
            BiomeManager.addBiome(biomeType, new BiomeManager.BiomeEntry(biome, weight));
        }
        if (addSpawnBiome) {
            BiomeManager.addSpawnBiome(biome);
        }
    }

    public static <E extends EntityLiving> void addSpawn(Class<E> entityClass, EnumCreatureType creatureType,
            int weight, int min, int max, Predicate<Biome> biomePredicate) {
        List<Biome> biomes = RetroWorldgenRegistry.findBiomes(biomePredicate);
        RetroEntityRegistry.addSpawn(RetroEntitySpawn.of(entityClass, creatureType, weight, min, max, biomes));
    }

    public static <E extends EntityLiving> void addSpawnToTypes(Class<E> entityClass, EnumCreatureType creatureType,
            int weight, int min, int max, BiomeDictionary.Type... types) {
        addSpawn(entityClass, creatureType, weight, min, max, RetroWorldgenRegistry.hasAllTypes(types));
    }
}
