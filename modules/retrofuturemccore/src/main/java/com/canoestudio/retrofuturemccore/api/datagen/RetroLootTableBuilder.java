package com.canoestudio.retrofuturemccore.api.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class RetroLootTableBuilder {

    private RetroLootTableBuilder() {
    }

    public static Builder table() {
        return new Builder();
    }

    public static JsonObject selfDrop(Block block) {
        if (block == null || block.getRegistryName() == null) {
            throw new IllegalArgumentException("Self drop block must be registered first");
        }
        return table().pool(Pool.main().item(block.getRegistryName())).build();
    }

    public static JsonObject singleItem(Item item) {
        if (item == null || item.getRegistryName() == null) {
            throw new IllegalArgumentException("Loot item must be registered first");
        }
        return table().pool(Pool.main().item(item.getRegistryName())).build();
    }

    public static final class Builder {
        private final JsonArray pools = new JsonArray();

        public Builder pool(Pool pool) {
            if (pool != null) {
                this.pools.add(pool.build());
            }
            return this;
        }

        public JsonObject build() {
            JsonObject json = new JsonObject();
            json.add("pools", this.pools);
            return json;
        }
    }

    public static final class Pool {
        private final String name;
        private int rolls = 1;
        private int bonusRolls;
        private final JsonArray entries = new JsonArray();

        private Pool(String name) {
            this.name = name;
        }

        public static Pool main() {
            return new Pool("main");
        }

        public Pool rolls(int rolls) {
            this.rolls = rolls;
            return this;
        }

        public Pool bonusRolls(int bonusRolls) {
            this.bonusRolls = bonusRolls;
            return this;
        }

        public Pool item(ResourceLocation id) {
            return this.item(id, 1);
        }

        public Pool item(ResourceLocation id, int weight) {
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "item");
            entry.addProperty("name", id.toString());
            if (weight != 1) {
                entry.addProperty("weight", weight);
            }
            this.entries.add(entry);
            return this;
        }

        public Pool itemWithCount(ResourceLocation id, int min, int max) {
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "item");
            entry.addProperty("name", id.toString());
            JsonArray functions = new JsonArray();
            JsonObject countFunction = new JsonObject();
            countFunction.addProperty("function", "set_count");
            JsonObject count = new JsonObject();
            count.addProperty("min", min);
            count.addProperty("max", max);
            countFunction.add("count", count);
            functions.add(countFunction);
            entry.add("functions", functions);
            this.entries.add(entry);
            return this;
        }

        public JsonObject build() {
            JsonObject pool = new JsonObject();
            pool.addProperty("name", this.name);
            pool.addProperty("rolls", this.rolls);
            if (this.bonusRolls != 0) {
                pool.addProperty("bonus_rolls", this.bonusRolls);
            }
            pool.add("entries", this.entries);
            return pool;
        }
    }
}
