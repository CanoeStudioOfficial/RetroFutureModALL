package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public enum AquaticFishType {
    COD("cod", 0.5F, 0.32F, 3.0D, 0.075D) {
        @Override
        public EntityAquaticFish create(World world) {
            return new EntityAquaticFish.Cod(world);
        }

        @Override
        public Item getRawItem() {
            return ModItems.COD;
        }
    },
    SALMON("salmon", 0.7F, 0.4F, 3.0D, 0.085D) {
        @Override
        public EntityAquaticFish create(World world) {
            return new EntityAquaticFish.Salmon(world);
        }

        @Override
        public Item getRawItem() {
            return ModItems.SALMON;
        }
    },
    PUFFERFISH("pufferfish", 0.7F, 0.7F, 3.0D, 0.065D) {
        @Override
        public EntityAquaticFish create(World world) {
            return new EntityAquaticFish.Pufferfish(world);
        }

        @Override
        public Item getRawItem() {
            return ModItems.PUFFERFISH;
        }
    },
    TROPICAL_FISH("tropical_fish", 0.5F, 0.32F, 3.0D, 0.08D) {
        @Override
        public EntityAquaticFish create(World world) {
            return new EntityAquaticFish.Tropical(world);
        }

        @Override
        public Item getRawItem() {
            return ModItems.TROPICAL_FISH;
        }
    };

    private final String id;
    private final float width;
    private final float height;
    private final double health;
    private final double swimSpeed;

    AquaticFishType(String id, float width, float height, double health, double swimSpeed) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.health = health;
        this.swimSpeed = swimSpeed;
    }

    public String getId() {
        return this.id;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public double getHealth() {
        return this.health;
    }

    public double getSwimSpeed() {
        return this.swimSpeed;
    }

    public abstract EntityAquaticFish create(World world);

    public abstract Item getRawItem();
}
