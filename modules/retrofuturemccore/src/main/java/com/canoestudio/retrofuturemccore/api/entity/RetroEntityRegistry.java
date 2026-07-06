package com.canoestudio.retrofuturemccore.api.entity;

import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public final class RetroEntityRegistry {

    private RetroEntityRegistry() {
    }

    public static <E extends Entity> Builder<E> builder(Class<E> entityClass, ResourceLocation id, int networkId) {
        return new Builder<E>(entityClass, id, networkId);
    }

    public static <E extends EntityLiving> void addSpawn(Class<E> entityClass, EnumCreatureType creatureType,
            int weight, int min, int max, Biome... biomes) {
        EntityRegistry.addSpawn(entityClass, weight, min, max, creatureType, biomes);
    }

    public static <E extends EntityLiving> void addSpawn(RetroEntitySpawn<E> spawn) {
        EntityRegistry.addSpawn(spawn.getEntityClass(), spawn.getWeight(), spawn.getMin(), spawn.getMax(),
                spawn.getCreatureType(), toArray(spawn.getBiomes()));
    }

    private static Biome[] toArray(Iterable<Biome> biomes) {
        java.util.List<Biome> list = new java.util.ArrayList<Biome>();
        for (Biome biome : biomes) {
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }

    public static final class Builder<E extends Entity> {
        private final Class<E> entityClass;
        private final ResourceLocation id;
        private final int networkId;
        private String name;
        private int trackingRange = 80;
        private int updateFrequency = 3;
        private boolean sendVelocityUpdates = true;
        private boolean hasEgg;
        private int eggPrimary;
        private int eggSecondary;
        private Function<World, E> factory;

        private Builder(Class<E> entityClass, ResourceLocation id, int networkId) {
            this.entityClass = entityClass;
            this.id = id;
            this.networkId = networkId;
            this.name = id.getPath();
        }

        public Builder<E> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<E> tracker(int range, int updateFrequency, boolean sendVelocityUpdates) {
            this.trackingRange = range;
            this.updateFrequency = updateFrequency;
            this.sendVelocityUpdates = sendVelocityUpdates;
            return this;
        }

        public Builder<E> egg(int primary, int secondary) {
            this.hasEgg = true;
            this.eggPrimary = primary;
            this.eggSecondary = secondary;
            return this;
        }

        public Builder<E> factory(Function<World, E> factory) {
            this.factory = factory;
            return this;
        }

        public EntityEntry build() {
            EntityEntryBuilder<E> builder = EntityEntryBuilder.<E>create()
                    .entity(this.entityClass)
                    .id(this.id, this.networkId)
                    .name(this.name)
                    .tracker(this.trackingRange, this.updateFrequency, this.sendVelocityUpdates);
            if (this.factory != null) {
                builder.factory(this.factory);
            }
            if (this.hasEgg) {
                builder.egg(this.eggPrimary, this.eggSecondary);
            }
            return builder.build();
        }

        public EntityEntry register(IForgeRegistry<EntityEntry> registry) {
            EntityEntry entry = build();
            registry.register(entry);
            return entry;
        }
    }
}
