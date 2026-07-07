package com.canoestudio.retrofuturemccore.api.block;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class RetroSignRegistry {
    private static final Map<ResourceLocation, RetroSignSet> SETS = new LinkedHashMap<ResourceLocation, RetroSignSet>();
    private static final Map<Block, RetroSignSet> BY_BLOCK = new LinkedHashMap<Block, RetroSignSet>();

    private RetroSignRegistry() {
    }

    public static RetroSignSet register(RetroSignSet set) {
        if (set == null) {
            throw new IllegalArgumentException("Cannot register a null sign set");
        }
        SETS.put(set.getId(), set);
        for (Block block : set.getBlocks()) {
            BY_BLOCK.put(block, set);
        }
        return set;
    }

    public static <T extends TileEntity> void registerTileEntity(Class<T> tileEntityClass, ResourceLocation id) {
        if (tileEntityClass != null && id != null) {
            GameRegistry.registerTileEntity(tileEntityClass, id);
        }
    }

    public static void registerTileEntities(RetroSignSet set) {
        if (set == null) {
            return;
        }
        if (set.getSignTileEntityClass() != null) {
            registerTileEntity(set.getSignTileEntityClass(), suffix(set.getId(), "_sign"));
        }
        if (set.getHangingSignTileEntityClass() != null) {
            registerTileEntity(set.getHangingSignTileEntityClass(), suffix(set.getId(), "_hanging_sign"));
        }
    }

    public static RetroSignSet get(ResourceLocation id) {
        return SETS.get(id);
    }

    public static RetroSignSet get(Block block) {
        return BY_BLOCK.get(block);
    }

    public static boolean isSignBlock(Block block) {
        return BY_BLOCK.containsKey(block);
    }

    public static Collection<RetroSignSet> all() {
        return Collections.unmodifiableCollection(SETS.values());
    }

    public static void clear() {
        SETS.clear();
        BY_BLOCK.clear();
    }

    private static ResourceLocation suffix(ResourceLocation id, String suffix) {
        return new ResourceLocation(id.getNamespace(), id.getPath() + suffix);
    }
}
