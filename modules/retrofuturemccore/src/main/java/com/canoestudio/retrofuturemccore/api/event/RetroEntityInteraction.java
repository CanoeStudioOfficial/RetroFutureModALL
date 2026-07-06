package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RetroEntityInteraction {
    private final World world;
    private final Entity target;
    private final EntityPlayer player;
    private final EnumHand hand;
    private final ItemStack stack;
    private final Vec3d localPos;

    public RetroEntityInteraction(World world, Entity target, EntityPlayer player, EnumHand hand, ItemStack stack,
            Vec3d localPos) {
        this.world = world;
        this.target = target;
        this.player = player;
        this.hand = hand;
        this.stack = stack;
        this.localPos = localPos;
    }

    public World getWorld() {
        return this.world;
    }

    public Entity getTarget() {
        return this.target;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public EnumHand getHand() {
        return this.hand;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public Vec3d getLocalPos() {
        return this.localPos;
    }
}
