package com.canoestudio.retrofuturemccore.api.block;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class RetroBoatSet {
    private final ResourceLocation id;
    private final Item boatItem;
    private final Item chestBoatItem;
    private final Class<? extends Entity> boatEntityClass;
    private final Class<? extends Entity> chestBoatEntityClass;
    private final ResourceLocation texture;

    private RetroBoatSet(Builder builder) {
        this.id = builder.id;
        this.boatItem = builder.boatItem;
        this.chestBoatItem = builder.chestBoatItem;
        this.boatEntityClass = builder.boatEntityClass;
        this.chestBoatEntityClass = builder.chestBoatEntityClass;
        this.texture = builder.texture;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Item getBoatItem() {
        return this.boatItem;
    }

    public Item getChestBoatItem() {
        return this.chestBoatItem;
    }

    public Class<? extends Entity> getBoatEntityClass() {
        return this.boatEntityClass;
    }

    public Class<? extends Entity> getChestBoatEntityClass() {
        return this.chestBoatEntityClass;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public boolean hasChestBoat() {
        return this.chestBoatItem != null || this.chestBoatEntityClass != null;
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Item boatItem;
        private Item chestBoatItem;
        private Class<? extends Entity> boatEntityClass;
        private Class<? extends Entity> chestBoatEntityClass;
        private ResourceLocation texture;

        private Builder(ResourceLocation id) {
            if (id == null) {
                throw new IllegalArgumentException("Boat set id cannot be null");
            }
            this.id = id;
        }

        public Builder boat(Item boatItem, Class<? extends Entity> entityClass) {
            this.boatItem = boatItem;
            this.boatEntityClass = entityClass;
            return this;
        }

        public Builder chestBoat(Item chestBoatItem, Class<? extends Entity> entityClass) {
            this.chestBoatItem = chestBoatItem;
            this.chestBoatEntityClass = entityClass;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public RetroBoatSet build() {
            return new RetroBoatSet(this);
        }

        public RetroBoatSet register() {
            return RetroBoatRegistry.register(this.build());
        }
    }
}
