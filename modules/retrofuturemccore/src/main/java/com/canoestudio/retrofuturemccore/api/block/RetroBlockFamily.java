package com.canoestudio.retrofuturemccore.api.block;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.block.Block;

/**
 * Modern BlockFamily-style metadata for 1.12.2 content modules.
 */
public final class RetroBlockFamily {
    private final Block baseBlock;
    private final Map<Variant, Block> variants = new EnumMap<Variant, Block>(Variant.class);
    private boolean generateModel = true;
    private boolean generateCraftingRecipe = true;
    private boolean generateSmeltingRecipe = true;
    private boolean generateStonecutterRecipe;
    private String recipeGroupPrefix;
    private String recipeUnlockedBy;

    private RetroBlockFamily(Block baseBlock) {
        if (baseBlock == null) {
            throw new IllegalArgumentException("Block family base block cannot be null");
        }
        this.baseBlock = baseBlock;
    }

    public static Builder builder(Block baseBlock) {
        return new Builder(baseBlock);
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Map<Variant, Block> getVariants() {
        return Collections.unmodifiableMap(this.variants);
    }

    public Block get(Variant variant) {
        return this.variants.get(variant);
    }

    public boolean has(Variant variant) {
        return this.variants.containsKey(variant);
    }

    public Block getBaseBlockForCrafting(Variant variant) {
        if (variant == null || variant.getBaseVariantForCrafting() == null) {
            return this.baseBlock;
        }
        Block baseVariant = this.get(variant.getBaseVariantForCrafting());
        return baseVariant == null ? this.baseBlock : baseVariant;
    }

    public boolean shouldGenerateModel() {
        return this.generateModel;
    }

    public boolean shouldGenerateCraftingRecipe() {
        return this.generateCraftingRecipe;
    }

    public boolean shouldGenerateSmeltingRecipe() {
        return this.generateSmeltingRecipe;
    }

    public boolean shouldGenerateStonecutterRecipe() {
        return this.generateStonecutterRecipe;
    }

    public String getRecipeGroupPrefix() {
        return this.recipeGroupPrefix;
    }

    public String getRecipeUnlockedBy() {
        return this.recipeUnlockedBy;
    }

    public static final class Builder {
        private final RetroBlockFamily family;

        private Builder(Block baseBlock) {
            this.family = new RetroBlockFamily(baseBlock);
        }

        public RetroBlockFamily build() {
            return this.family;
        }

        public RetroBlockFamily getFamily() {
            return this.family;
        }

        public Builder variant(Variant variant, Block block) {
            if (variant != null && block != null) {
                this.family.variants.put(variant, block);
            }
            return this;
        }

        public Builder button(Block button) {
            return this.variant(Variant.BUTTON, button);
        }

        public Builder chiseled(Block chiseled) {
            return this.variant(Variant.CHISELED, chiseled);
        }

        public Builder mosaic(Block mosaic) {
            return this.variant(Variant.MOSAIC, mosaic);
        }

        public Builder cracked(Block cracked) {
            return this.variant(Variant.CRACKED, cracked);
        }

        public Builder tiles(Block tiles) {
            return this.variant(Variant.TILES, tiles);
        }

        public Builder pillar(Block pillar) {
            return this.variant(Variant.PILLAR, pillar);
        }

        public Builder cut(Block cut) {
            return this.variant(Variant.CUT, cut);
        }

        public Builder door(Block door) {
            return this.variant(Variant.DOOR, door);
        }

        public Builder customFence(Block fence) {
            return this.variant(Variant.CUSTOM_FENCE, fence);
        }

        public Builder fence(Block fence) {
            return this.variant(Variant.FENCE, fence);
        }

        public Builder customFenceGate(Block fenceGate) {
            return this.variant(Variant.CUSTOM_FENCE_GATE, fenceGate);
        }

        public Builder fenceGate(Block fenceGate) {
            return this.variant(Variant.FENCE_GATE, fenceGate);
        }

        public Builder sign(Block sign, Block wallSign) {
            return this.variant(Variant.SIGN, sign).variant(Variant.WALL_SIGN, wallSign);
        }

        public Builder customHangingSign(Block sign, Block wallSign) {
            return this.variant(Variant.CUSTOM_HANGING_SIGN, sign)
                    .variant(Variant.CUSTOM_WALL_HANGING_SIGN, wallSign);
        }

        public Builder hangingSign(Block sign, Block wallSign) {
            return this.variant(Variant.HANGING_SIGN, sign).variant(Variant.WALL_HANGING_SIGN, wallSign);
        }

        public Builder log(Block log) {
            return this.variant(Variant.LOG, log);
        }

        public Builder strippedLog(Block strippedLog) {
            return this.variant(Variant.STRIPPED_LOG, strippedLog);
        }

        public Builder slab(Block slab) {
            return this.variant(Variant.SLAB, slab);
        }

        public Builder stairs(Block stairs) {
            return this.variant(Variant.STAIRS, stairs);
        }

        public Builder pressurePlate(Block pressurePlate) {
            return this.variant(Variant.PRESSURE_PLATE, pressurePlate);
        }

        public Builder polished(Block polished) {
            return this.variant(Variant.POLISHED, polished);
        }

        public Builder trapdoor(Block trapdoor) {
            return this.variant(Variant.TRAPDOOR, trapdoor);
        }

        public Builder wall(Block wall) {
            return this.variant(Variant.WALL, wall);
        }

        public Builder cobbled(Block cobble) {
            return this.variant(Variant.COBBLED, cobble);
        }

        public Builder bricks(Block bricks) {
            return this.variant(Variant.BRICKS, bricks);
        }

        public Builder dontGenerateModel() {
            this.family.generateModel = false;
            return this;
        }

        public Builder dontGenerateCraftingRecipe() {
            this.family.generateCraftingRecipe = false;
            return this;
        }

        public Builder dontGenerateSmeltingRecipe() {
            this.family.generateSmeltingRecipe = false;
            return this;
        }

        public Builder generateStonecutterRecipe() {
            this.family.generateStonecutterRecipe = true;
            return this;
        }

        public Builder recipeGroupPrefix(String recipeGroupPrefix) {
            this.family.recipeGroupPrefix = recipeGroupPrefix;
            return this;
        }

        public Builder recipeUnlockedBy(String recipeUnlockedBy) {
            this.family.recipeUnlockedBy = recipeUnlockedBy;
            return this;
        }
    }

    public enum Variant {
        BUTTON("button"),
        CHISELED("chiseled"),
        CRACKED("cracked"),
        CUT("cut"),
        DOOR("door"),
        CUSTOM_FENCE("fence"),
        FENCE("fence"),
        CUSTOM_FENCE_GATE("fence_gate"),
        FENCE_GATE("fence_gate"),
        CUSTOM_HANGING_SIGN("hanging_sign"),
        HANGING_SIGN("hanging_sign"),
        LOG("log"),
        STRIPPED_LOG("stripped_log"),
        MOSAIC("mosaic"),
        SIGN("sign"),
        SLAB("slab"),
        STAIRS("stairs"),
        PRESSURE_PLATE("pressure_plate"),
        POLISHED("polished"),
        TRAPDOOR("trapdoor"),
        WALL("wall"),
        WALL_SIGN("wall_sign"),
        CUSTOM_WALL_HANGING_SIGN("wall_hanging_sign"),
        WALL_HANGING_SIGN("wall_hanging_sign"),
        BRICKS("bricks"),
        COBBLED("cobbled"),
        TILES("tiles"),
        PILLAR("pillar");

        private final String recipeGroup;
        Variant(String recipeGroup) {
            this.recipeGroup = recipeGroup;
        }

        public String getRecipeGroup() {
            return this.recipeGroup;
        }

        public Variant getBaseVariantForCrafting() {
            if (this == CHISELED) {
                return SLAB;
            }
            if (this == CUSTOM_HANGING_SIGN || this == HANGING_SIGN) {
                return STRIPPED_LOG;
            }
            return null;
        }

        public String getPrefixedRecipeGroup(String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                return this.recipeGroup;
            }
            if (this == CUT) {
                return prefix;
            }
            return prefix + "_" + this.recipeGroup;
        }
    }
}
