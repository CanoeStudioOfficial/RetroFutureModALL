package com.canoestudio.retrofuturemccore.api.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class RetroWorldgenRegistry {

    private RetroWorldgenRegistry() {
    }

    public static void registerGenerator(IWorldGenerator generator, int weight) {
        if (generator != null) {
            GameRegistry.registerWorldGenerator(generator, weight);
        }
    }

    public static List<Biome> findBiomes(Predicate<Biome> predicate) {
        List<Biome> biomes = new ArrayList<Biome>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (predicate == null || predicate.test(biome)) {
                biomes.add(biome);
            }
        }
        return biomes;
    }

    public static Biome[] findBiomeArray(Predicate<Biome> predicate) {
        List<Biome> biomes = findBiomes(predicate);
        return biomes.toArray(new Biome[0]);
    }

    public static Predicate<Biome> hasAllTypes(final BiomeDictionary.Type... types) {
        return new Predicate<Biome>() {
            @Override
            public boolean test(Biome biome) {
                return RetroWorldgenRegistry.hasAllTypes(biome, types);
            }
        };
    }

    public static Predicate<Biome> hasAnyType(final BiomeDictionary.Type... types) {
        return new Predicate<Biome>() {
            @Override
            public boolean test(Biome biome) {
                return RetroWorldgenRegistry.hasAnyType(biome, types);
            }
        };
    }

    public static boolean hasAllTypes(Biome biome, BiomeDictionary.Type... types) {
        if (biome == null || types == null) {
            return false;
        }
        for (BiomeDictionary.Type type : types) {
            if (!BiomeDictionary.hasType(biome, type)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyType(Biome biome, BiomeDictionary.Type... types) {
        if (biome == null || types == null) {
            return false;
        }
        for (BiomeDictionary.Type type : types) {
            if (BiomeDictionary.hasType(biome, type)) {
                return true;
            }
        }
        return false;
    }

    public static BlockPos chunkCenter(int chunkX, int chunkZ) {
        return RetroStructurePlacement.getChunkCenter(chunkX, chunkZ);
    }

    public static Biome chunkBiome(World world, int chunkX, int chunkZ) {
        return world.getBiome(chunkCenter(chunkX, chunkZ));
    }
}
