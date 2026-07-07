package com.canoestudio.retrofuturethewildupdate.world.biome;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntitySpawn;
import com.canoestudio.retrofuturemccore.api.world.RetroBiomeSpawnRegistry;
import com.canoestudio.retrofuturemccore.api.world.RetroWorldgenRegistry;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityFrog;
import com.canoestudio.retrofuturethewildupdate.entity.EntityTadpole;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public final class ModBiomes {

    public static final Biome MANGROVE_SWAMP = new BiomeMangroveSwamp();

    private ModBiomes() {
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(MANGROVE_SWAMP);
    }

    public static void init() {
        RetroBiomeSpawnRegistry.registerBiome(MANGROVE_SWAMP, BiomeManager.BiomeType.WARM, 7, true,
            BiomeDictionary.Type.SWAMP,
            BiomeDictionary.Type.WET,
            BiomeDictionary.Type.HOT,
            BiomeDictionary.Type.DENSE);

        RetroBiomeSpawnRegistry.addSpawn(EntityFrog.class, EnumCreatureType.CREATURE, 12, 2, 5,
            ModBiomes::isFrogSpawnBiome);
        RetroBiomeSpawnRegistry.addSpawn(EntityTadpole.class, EnumCreatureType.WATER_CREATURE, 8, 2, 5,
            ModBiomes::isTadpoleSpawnBiome);
    }

    public static RetroEntitySpawn<EntityFrog> mangroveFrogSpawn() {
        return RetroEntitySpawn.of(EntityFrog.class, EnumCreatureType.CREATURE, 12, 2, 5,
            MANGROVE_SWAMP, Biomes.SWAMPLAND, Biomes.MUTATED_SWAMPLAND);
    }

    public static RetroEntitySpawn<EntityTadpole> mangroveTadpoleSpawn() {
        return RetroEntitySpawn.of(EntityTadpole.class, EnumCreatureType.WATER_CREATURE, 8, 2, 5,
            MANGROVE_SWAMP, Biomes.SWAMPLAND, Biomes.MUTATED_SWAMPLAND);
    }

    private static boolean isFrogSpawnBiome(Biome biome) {
        return isTadpoleSpawnBiome(biome)
            || (RetroWorldgenRegistry.hasAllTypes(biome, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WET)
                && biome.getDefaultTemperature() >= 0.65F);
    }

    private static boolean isTadpoleSpawnBiome(Biome biome) {
        return biome == MANGROVE_SWAMP || biome == Biomes.SWAMPLAND || biome == Biomes.MUTATED_SWAMPLAND;
    }
}
