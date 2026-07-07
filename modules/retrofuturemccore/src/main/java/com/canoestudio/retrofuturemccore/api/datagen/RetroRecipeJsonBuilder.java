package com.canoestudio.retrofuturemccore.api.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class RetroRecipeJsonBuilder {

    private RetroRecipeJsonBuilder() {
    }

    public static Ingredient item(Item item) {
        return id(requireRegistryName(item));
    }

    public static Ingredient block(Block block) {
        return id(requireRegistryName(block));
    }

    public static Ingredient id(ResourceLocation id) {
        JsonObject json = new JsonObject();
        json.addProperty("item", id.toString());
        return new Ingredient(json);
    }

    public static Ingredient stack(ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", requireRegistryName(stack.getItem()).toString());
        if (stack.getMetadata() != 0) {
            json.addProperty("data", stack.getMetadata());
        }
        return new Ingredient(json);
    }

    public static Ingredient ore(String oreName) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "forge:ore_dict");
        json.addProperty("ore", oreName);
        return new Ingredient(json);
    }

    public static Shaped shaped(ItemStack result) {
        return new Shaped(result);
    }

    public static Shapeless shapeless(ItemStack result) {
        return new Shapeless(result);
    }

    public static JsonObject button(Block baseBlock, Block button) {
        return shaped(new ItemStack(button))
                .pattern("#")
                .key('#', block(baseBlock))
                .build();
    }

    public static JsonObject door(Block planks, Item door) {
        return shaped(new ItemStack(door, 3))
                .pattern("##", "##", "##")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject fence(Block planks, Block fence) {
        return shaped(new ItemStack(fence, 3))
                .pattern("W#W", "W#W")
                .key('W', block(planks))
                .key('#', item(Items.STICK))
                .build();
    }

    public static JsonObject fenceGate(Block planks, Block fenceGate) {
        return shaped(new ItemStack(fenceGate))
                .pattern("#W#", "#W#")
                .key('W', block(planks))
                .key('#', item(Items.STICK))
                .build();
    }

    public static JsonObject pressurePlate(Block planks, Block pressurePlate) {
        return shaped(new ItemStack(pressurePlate))
                .pattern("##")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject sign(Block planks, Item sign) {
        return shaped(new ItemStack(sign, 3))
                .pattern("###", "###", " X ")
                .key('#', block(planks))
                .key('X', item(Items.STICK))
                .build();
    }

    public static JsonObject hangingSign(Block strippedLog, Item chainEquivalent, Item hangingSign) {
        return shaped(new ItemStack(hangingSign, 6))
                .pattern("X X", "###", "###")
                .key('X', item(chainEquivalent))
                .key('#', block(strippedLog))
                .build();
    }

    public static JsonObject slab(Block planks, Block slab) {
        return shaped(new ItemStack(slab, 6))
                .pattern("###")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject stairs(Block planks, Block stairs) {
        return shaped(new ItemStack(stairs, 4))
                .pattern("#  ", "## ", "###")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject trapdoor(Block planks, Block trapdoor) {
        return shaped(new ItemStack(trapdoor, 2))
                .pattern("###", "###")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject boat(Block planks, Item boat) {
        return shaped(new ItemStack(boat))
                .pattern("# #", "###")
                .key('#', block(planks))
                .build();
    }

    public static JsonObject chestBoat(Item boat, Item chest, Item chestBoat) {
        return shapeless(new ItemStack(chestBoat))
                .ingredient(item(boat))
                .ingredient(item(chest))
                .build();
    }

    private static JsonObject result(ItemStack stack) {
        JsonObject result = new JsonObject();
        result.addProperty("item", requireRegistryName(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            result.addProperty("count", stack.getCount());
        }
        if (stack.getMetadata() != 0) {
            result.addProperty("data", stack.getMetadata());
        }
        return result;
    }

    private static ResourceLocation requireRegistryName(Item item) {
        if (item == null || item.getRegistryName() == null) {
            throw new IllegalArgumentException("Recipe ingredient item must be registered first");
        }
        return item.getRegistryName();
    }

    private static ResourceLocation requireRegistryName(Block block) {
        if (block == null || block.getRegistryName() == null) {
            throw new IllegalArgumentException("Recipe ingredient block must be registered first");
        }
        return block.getRegistryName();
    }

    public static final class Ingredient {
        private final JsonObject json;

        private Ingredient(JsonObject json) {
            this.json = json;
        }

        public JsonObject toJson() {
            return this.json;
        }
    }

    public static final class Shaped {
        private final ItemStack result;
        private final JsonArray pattern = new JsonArray();
        private final Map<Character, Ingredient> keys = new LinkedHashMap<Character, Ingredient>();
        private String group;

        private Shaped(ItemStack result) {
            this.result = result;
        }

        public Shaped group(String group) {
            this.group = group;
            return this;
        }

        public Shaped pattern(String... rows) {
            if (rows != null) {
                for (String row : rows) {
                    this.pattern.add(row);
                }
            }
            return this;
        }

        public Shaped key(char symbol, Ingredient ingredient) {
            if (ingredient != null) {
                this.keys.put(Character.valueOf(symbol), ingredient);
            }
            return this;
        }

        public JsonObject build() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:crafting_shaped");
            if (this.group != null && !this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            json.add("pattern", this.pattern);
            JsonObject key = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.keys.entrySet()) {
                key.add(String.valueOf(entry.getKey().charValue()), entry.getValue().toJson());
            }
            json.add("key", key);
            json.add("result", result(this.result));
            return json;
        }
    }

    public static final class Shapeless {
        private final ItemStack result;
        private final JsonArray ingredients = new JsonArray();
        private String group;

        private Shapeless(ItemStack result) {
            this.result = result;
        }

        public Shapeless group(String group) {
            this.group = group;
            return this;
        }

        public Shapeless ingredient(Ingredient ingredient) {
            if (ingredient != null) {
                this.ingredients.add(ingredient.toJson());
            }
            return this;
        }

        public JsonObject build() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:crafting_shapeless");
            if (this.group != null && !this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            json.add("ingredients", this.ingredients);
            json.add("result", result(this.result));
            return json;
        }
    }
}
