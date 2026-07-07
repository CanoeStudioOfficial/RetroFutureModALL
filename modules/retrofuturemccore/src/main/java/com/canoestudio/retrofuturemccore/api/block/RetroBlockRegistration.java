package com.canoestudio.retrofuturemccore.api.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public final class RetroBlockRegistration {

    private RetroBlockRegistration() {
    }

    public static void registerBlocks(IForgeRegistry<Block> registry, RetroBlockFamily family) {
        if (registry == null || family == null) {
            return;
        }
        Set<Block> registered = new HashSet<Block>();
        registerBlock(registry, family.getBaseBlock(), registered);
        for (Block block : family.getVariants().values()) {
            registerBlock(registry, block, registered);
        }
    }

    public static void registerSimpleBlockItems(IForgeRegistry<Item> registry, RetroBlockFamily family) {
        registerBlockItems(registry, family, new ItemFactory() {
            @Override
            public Item create(Block block, RetroBlockFamily.Variant variant) {
                if (variant != null && !isSimpleItemBlockVariant(variant)) {
                    return null;
                }
                return new ItemBlock(block).setRegistryName(block.getRegistryName());
            }
        });
    }

    public static void registerBlockItems(IForgeRegistry<Item> registry, RetroBlockFamily family, ItemFactory factory) {
        if (registry == null || family == null || factory == null) {
            return;
        }
        Set<Block> registered = new HashSet<Block>();
        registerItem(registry, family.getBaseBlock(), null, factory, registered);
        for (Map.Entry<RetroBlockFamily.Variant, Block> entry : family.getVariants().entrySet()) {
            registerItem(registry, entry.getValue(), entry.getKey(), factory, registered);
        }
    }

    public static boolean isSimpleItemBlockVariant(RetroBlockFamily.Variant variant) {
        return variant != RetroBlockFamily.Variant.DOOR
                && variant != RetroBlockFamily.Variant.SIGN
                && variant != RetroBlockFamily.Variant.WALL_SIGN
                && variant != RetroBlockFamily.Variant.HANGING_SIGN
                && variant != RetroBlockFamily.Variant.WALL_HANGING_SIGN
                && variant != RetroBlockFamily.Variant.CUSTOM_HANGING_SIGN
                && variant != RetroBlockFamily.Variant.CUSTOM_WALL_HANGING_SIGN;
    }

    private static void registerBlock(IForgeRegistry<Block> registry, Block block, Set<Block> registered) {
        if (block != null && block.getRegistryName() != null && registered.add(block)) {
            registry.register(block);
        }
    }

    private static void registerItem(IForgeRegistry<Item> registry, Block block, RetroBlockFamily.Variant variant,
            ItemFactory factory, Set<Block> registered) {
        if (block == null || block.getRegistryName() == null || !registered.add(block)) {
            return;
        }
        Item item = factory.create(block, variant);
        if (item != null && item.getRegistryName() != null) {
            registry.register(item);
        }
    }

    public interface ItemFactory {
        Item create(Block block, RetroBlockFamily.Variant variant);
    }
}
