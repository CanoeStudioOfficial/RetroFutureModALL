package com.canoestudio.retrofuturemccore.api.block;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

public final class RetroWoodSets {
    private static final Map<ResourceLocation, RetroWoodSet> SETS = new LinkedHashMap<ResourceLocation, RetroWoodSet>();

    private RetroWoodSets() {
    }

    public static RetroWoodSet register(RetroWoodSet set) {
        if (set == null) {
            throw new IllegalArgumentException("Cannot register a null wood set");
        }
        SETS.put(set.getId(), set);
        if (set.getFamily() != null) {
            RetroBlockFamilies.register(set.getFamily());
        }
        if (set.getSignSet() != null) {
            RetroSignRegistry.register(set.getSignSet());
        }
        if (set.getBoatSet() != null) {
            RetroBoatRegistry.register(set.getBoatSet());
        }
        return set;
    }

    public static RetroWoodSet get(ResourceLocation id) {
        return SETS.get(id);
    }

    public static Collection<RetroWoodSet> all() {
        return Collections.unmodifiableCollection(SETS.values());
    }

    public static void clear() {
        SETS.clear();
    }
}
