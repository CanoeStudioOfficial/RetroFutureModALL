package com.canoestudio.retrofuturemccore.api.entity;

import java.util.Arrays;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;

public final class RetroEntitySpawn<E extends EntityLiving> {
    private final Class<E> entityClass;
    private final EnumCreatureType creatureType;
    private final int weight;
    private final int min;
    private final int max;
    private final Iterable<Biome> biomes;

    private RetroEntitySpawn(Class<E> entityClass, EnumCreatureType creatureType, int weight, int min, int max,
            Iterable<Biome> biomes) {
        this.entityClass = entityClass;
        this.creatureType = creatureType;
        this.weight = weight;
        this.min = min;
        this.max = max;
        this.biomes = biomes;
    }

    public static <E extends EntityLiving> RetroEntitySpawn<E> of(Class<E> entityClass, EnumCreatureType creatureType,
            int weight, int min, int max, Biome... biomes) {
        return new RetroEntitySpawn<E>(entityClass, creatureType, weight, min, max, Arrays.asList(biomes));
    }

    public static <E extends EntityLiving> RetroEntitySpawn<E> of(Class<E> entityClass, EnumCreatureType creatureType,
            int weight, int min, int max, Iterable<Biome> biomes) {
        return new RetroEntitySpawn<E>(entityClass, creatureType, weight, min, max, biomes);
    }

    public Class<E> getEntityClass() {
        return this.entityClass;
    }

    public EnumCreatureType getCreatureType() {
        return this.creatureType;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public Iterable<Biome> getBiomes() {
        return this.biomes;
    }
}
