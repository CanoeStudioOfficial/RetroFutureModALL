package com.canoestudio.retrofuturemccore.api.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;

public final class RetroBlockFamilies {
    private static final List<RetroBlockFamily> FAMILIES = new ArrayList<RetroBlockFamily>();
    private static final Map<Block, RetroBlockFamily> BY_BLOCK = new LinkedHashMap<Block, RetroBlockFamily>();

    private RetroBlockFamilies() {
    }

    public static RetroBlockFamily register(RetroBlockFamily family) {
        if (family == null) {
            throw new IllegalArgumentException("Cannot register a null block family");
        }
        FAMILIES.add(family);
        BY_BLOCK.put(family.getBaseBlock(), family);
        for (Block block : family.getVariants().values()) {
            BY_BLOCK.put(block, family);
        }
        return family;
    }

    public static List<RetroBlockFamily> all() {
        return Collections.unmodifiableList(FAMILIES);
    }

    public static RetroBlockFamily byBlock(Block block) {
        return BY_BLOCK.get(block);
    }

    public static boolean contains(Block block) {
        return BY_BLOCK.containsKey(block);
    }

    public static void clear() {
        FAMILIES.clear();
        BY_BLOCK.clear();
    }
}
