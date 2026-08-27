package com.canoestudio.retrofuturelushcave.utils;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidloggedCompat {
    public static boolean isWater(IBlockAccess world, BlockPos pos) {
        FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, world.getBlockState(pos));
        if (!fluidState.isEmpty()) {
            return FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluidState.getFluid());
        }
        return false;
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, state);
        if (fluidState.isEmpty() || world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }
        world.setBlockState(pos, fluidState.getState(), flags);
        scheduleFluidTick(world, pos, fluidState.getState());
    }

    public static void setFluidloggableBlock(World world, BlockPos pos, IBlockState newState, int flags) {
        FluidState original = FluidloggedUtils.getFluidState(world, pos);
        if (!original.isEmpty()) {
            FluidloggedUtils.setFluidState(world, pos, newState, original, false, flags);
            Block fluidBlock = original.getBlock();
            world.scheduleUpdate(pos, fluidBlock, fluidBlock.tickRate(world));
        }
    }

    public static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, state);
        if (!fluidState.isEmpty()) {
            Block fluidBlock = fluidState.getBlock();
            world.scheduleUpdate(pos, fluidBlock, fluidBlock.tickRate(world));
        }
    }
}
