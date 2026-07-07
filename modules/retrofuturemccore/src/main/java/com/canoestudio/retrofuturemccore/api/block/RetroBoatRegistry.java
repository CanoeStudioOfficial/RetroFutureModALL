package com.canoestudio.retrofuturemccore.api.block;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntityRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

public final class RetroBoatRegistry {
    public static final int DEFAULT_TRACKING_RANGE = 80;
    public static final int DEFAULT_UPDATE_FREQUENCY = 3;

    private static final Map<ResourceLocation, RetroBoatSet> SETS = new LinkedHashMap<ResourceLocation, RetroBoatSet>();

    private RetroBoatRegistry() {
    }

    public static RetroBoatSet register(RetroBoatSet set) {
        if (set == null) {
            throw new IllegalArgumentException("Cannot register a null boat set");
        }
        SETS.put(set.getId(), set);
        return set;
    }

    public static void registerItem(IForgeRegistry<Item> registry, Item item) {
        if (registry != null && item != null && item.getRegistryName() != null) {
            registry.register(item);
        }
    }

    public static <E extends Entity> EntityEntry registerBoatEntity(IForgeRegistry<EntityEntry> registry,
            Class<E> entityClass, ResourceLocation id, int networkId, Function<World, E> factory) {
        return RetroEntityRegistry.builder(entityClass, id, networkId)
                .factory(factory)
                .name(id.toString())
                .tracker(DEFAULT_TRACKING_RANGE, DEFAULT_UPDATE_FREQUENCY, true)
                .register(registry);
    }

    public static RetroBoatSet get(ResourceLocation id) {
        return SETS.get(id);
    }

    public static Collection<RetroBoatSet> all() {
        return Collections.unmodifiableCollection(SETS.values());
    }

    public static void clear() {
        SETS.clear();
    }
}
