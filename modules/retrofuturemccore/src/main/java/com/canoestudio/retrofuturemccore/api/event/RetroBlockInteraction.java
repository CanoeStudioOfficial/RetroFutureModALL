package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RetroBlockInteraction {
    private final World world;
    private final BlockPos pos;
    private final IBlockState state;
    private final EntityPlayer player;
    private final EnumHand hand;
    private final ItemStack stack;
    private final EnumFacing face;
    private final Vec3d hitVec;

    public RetroBlockInteraction(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            ItemStack stack, EnumFacing face, Vec3d hitVec) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.player = player;
        this.hand = hand;
        this.stack = stack;
        this.face = face;
        this.hitVec = hitVec;
    }

    public World getWorld() {
        return this.world;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public IBlockState getState() {
        return this.state;
    }

    public Block getBlock() {
        return this.state.getBlock();
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

    public EnumFacing getFace() {
        return this.face;
    }

    public Vec3d getHitVec() {
        return this.hitVec;
    }
}
