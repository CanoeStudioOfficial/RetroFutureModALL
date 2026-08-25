package com.canoestudio.retrofuturemccore.api.block;

import java.util.HashSet;
import java.util.List;
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

    /**
     * Registers a prepared list of blocks. This is useful for modules that
     * collect their blocks while constructing content instead of using a
     * {@link RetroBlockFamily}.
     */
    public static void registerBlocks(IForgeRegistry<Block> registry, List<Block> blocks) {
        if (registry == null || blocks == null) {
            return;
        }
        Set<Block> registered = new HashSet<Block>();
        for (Block block : blocks) {
            registerBlock(registry, block, registered);
        }
    }

    public static void registerItems(IForgeRegistry<Item> registry, List<Item> items) {
        if (registry == null || items == null) {
            return;
        }
        Set<Item> registered = new HashSet<Item>();
        for (Item item : items) {
            registerItem(registry, item, registered);
        }
    }

    public static void registerBlockItems(IForgeRegistry<Item> registry, List<Item> items) {
        registerItems(registry, items);
    }

    private static void registerItem(IForgeRegistry<Item> registry, Item item, Set<Item> registered) {
        if (registry == null || item == null || registered == null) {
            return;
        }
        if (item.getRegistryName() != null && registered.add(item)) {
            registry.register(item);
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
