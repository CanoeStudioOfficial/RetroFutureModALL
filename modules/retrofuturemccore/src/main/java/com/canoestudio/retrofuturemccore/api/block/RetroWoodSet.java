package com.canoestudio.retrofuturemccore.api.block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class RetroWoodSet {
    private final ResourceLocation id;
    private final Block planks;
    private final Block log;
    private final Block strippedLog;
    private final Block wood;
    private final Block strippedWood;
    private final Block leaves;
    private final Block sapling;
    private final RetroBlockFamily family;
    private final RetroSignSet signSet;
    private final RetroBoatSet boatSet;
    private final Map<String, Block> extraBlocks;
    private final Map<String, Item> extraItems;

    private RetroWoodSet(Builder builder) {
        this.id = builder.id;
        this.planks = builder.planks;
        this.log = builder.log;
        this.strippedLog = builder.strippedLog;
        this.wood = builder.wood;
        this.strippedWood = builder.strippedWood;
        this.leaves = builder.leaves;
        this.sapling = builder.sapling;
        this.family = builder.family == null ? createDefaultFamily(builder) : builder.family;
        this.signSet = builder.signSet;
        this.boatSet = builder.boatSet;
        this.extraBlocks = Collections.unmodifiableMap(new LinkedHashMap<String, Block>(builder.extraBlocks));
        this.extraItems = Collections.unmodifiableMap(new LinkedHashMap<String, Item>(builder.extraItems));
    }

    public static Builder builder(ResourceLocation id, Block planks) {
        return new Builder(id, planks);
    }

    private static RetroBlockFamily createDefaultFamily(Builder builder) {
        RetroBlockFamily.Builder familyBuilder = RetroBlockFamily.builder(builder.planks);
        familyBuilder.log(builder.log).strippedLog(builder.strippedLog);
        return familyBuilder.build();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Block getPlanks() {
        return this.planks;
    }

    public Block getLog() {
        return this.log;
    }

    public Block getStrippedLog() {
        return this.strippedLog;
    }

    public Block getWood() {
        return this.wood;
    }

    public Block getStrippedWood() {
        return this.strippedWood;
    }

    public Block getLeaves() {
        return this.leaves;
    }

    public Block getSapling() {
        return this.sapling;
    }

    public RetroBlockFamily getFamily() {
        return this.family;
    }

    public RetroSignSet getSignSet() {
        return this.signSet;
    }

    public RetroBoatSet getBoatSet() {
        return this.boatSet;
    }

    public Map<String, Block> getExtraBlocks() {
        return this.extraBlocks;
    }

    public Map<String, Item> getExtraItems() {
        return this.extraItems;
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final Block planks;
        private Block log;
        private Block strippedLog;
        private Block wood;
        private Block strippedWood;
        private Block leaves;
        private Block sapling;
        private RetroBlockFamily family;
        private RetroSignSet signSet;
        private RetroBoatSet boatSet;
        private final Map<String, Block> extraBlocks = new LinkedHashMap<String, Block>();
        private final Map<String, Item> extraItems = new LinkedHashMap<String, Item>();

        private Builder(ResourceLocation id, Block planks) {
            if (id == null || planks == null) {
                throw new IllegalArgumentException("Wood set id and planks cannot be null");
            }
            this.id = id;
            this.planks = planks;
        }

        public Builder log(Block log) {
            this.log = log;
            return this;
        }

        public Builder strippedLog(Block strippedLog) {
            this.strippedLog = strippedLog;
            return this;
        }

        public Builder wood(Block wood) {
            this.wood = wood;
            return this;
        }

        public Builder strippedWood(Block strippedWood) {
            this.strippedWood = strippedWood;
            return this;
        }

        public Builder leaves(Block leaves) {
            this.leaves = leaves;
            return this;
        }

        public Builder sapling(Block sapling) {
            this.sapling = sapling;
            return this;
        }

        public Builder family(RetroBlockFamily family) {
            this.family = family;
            return this;
        }

        public Builder signs(RetroSignSet signSet) {
            this.signSet = signSet;
            return this;
        }

        public Builder boats(RetroBoatSet boatSet) {
            this.boatSet = boatSet;
            return this;
        }

        public Builder block(String key, Block block) {
            if (key != null && block != null) {
                this.extraBlocks.put(key, block);
            }
            return this;
        }

        public Builder item(String key, Item item) {
            if (key != null && item != null) {
                this.extraItems.put(key, item);
            }
            return this;
        }

        public RetroWoodSet build() {
            return new RetroWoodSet(this);
        }

        public RetroWoodSet register() {
            return RetroWoodSets.register(this.build());
        }
    }
}
