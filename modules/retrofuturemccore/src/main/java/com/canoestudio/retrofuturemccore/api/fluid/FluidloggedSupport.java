package com.canoestudio.retrofuturemccore.api.fluid;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Shared accessors for the required Fluidlogged API integration.
 *
 * <p>The actual fluid is always stored in Fluidlogged API's capability and
 * never in a synthetic block-state property.</p>
 */
public final class FluidloggedSupport {

    private FluidloggedSupport() {
    }

    public static FluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return FluidloggedUtils.getFluidState(world, pos);
    }

    public static FluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return FluidloggedUtils.getFluidState(world, pos, state);
    }

    public static boolean isWater(IBlockAccess world, BlockPos pos) {
        return isWater(getFluidState(world, pos));
    }

    public static boolean isWater(FluidState fluidState) {
        return fluidState != null && !fluidState.isEmpty() && isWater(fluidState.getFluid());
    }

    public static boolean isWater(Fluid fluid) {
        return FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluid);
    }

    public static boolean isVanillaWater(IBlockState state) {
        return state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER;
    }

    public static boolean isWaterBlock(IBlockState state) {
        return isVanillaWater(state);
    }

    public static boolean canReplaceWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return isWater(world, pos) || state.getBlock().isReplaceable(world, pos);
    }

    public static boolean canPlaceIntoAirOrWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceable(world, pos)
            || state.getBlock() == Blocks.AIR
            || isWater(world, pos);
    }

    public static void restoreWater(World world, BlockPos pos) {
        restoreFluidOrAir(world, pos, world.getBlockState(pos), 3);
    }

    public static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        restoreFluidOrAir(world, pos, replacedState, 3);
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, IBlockState replacedState, int flags) {
        FluidState fluidState = getFluidState(world, pos, replacedState);
        if (fluidState.isEmpty() || world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }

        world.setBlockState(pos, fluidState.getState(), flags);
        scheduleFluidTick(world, pos, fluidState);
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, FluidState fluidState, int flags) {
        if (fluidState == null || fluidState.isEmpty() || world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }
        world.setBlockState(pos, fluidState.getState(), flags);
        scheduleFluidTick(world, pos, fluidState);
    }

    public static void restoreContainedFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        restoreFluidOrAir(world, pos, getFluidState(world, pos, state), flags);
    }

    public static void setFluidloggableBlock(World world, BlockPos pos, IBlockState newState, int flags) {
        FluidState fluidState = getFluidState(world, pos);
        world.setBlockState(pos, newState, flags);
        if (!fluidState.isEmpty()) {
            FluidloggedUtils.setFluidState(world, pos, world.getBlockState(pos), fluidState, false, flags);
            scheduleFluidTick(world, pos, fluidState);
        }
    }

    public static boolean setFluidState(World world, BlockPos pos, IBlockState here,
            FluidState fluidState, int flags) {
        return FluidloggedUtils.setFluidState(world, pos, here, fluidState, false, flags);
    }

    public static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        scheduleFluidTick(world, pos, getFluidState(world, pos, state));
    }

    public static void scheduleFluidTick(World world, BlockPos pos, FluidState fluidState) {
        if (fluidState != null && !fluidState.isEmpty()) {
            Block fluidBlock = fluidState.getBlock();
            world.scheduleUpdate(pos, fluidBlock, fluidBlock.tickRate(world));
        }
    }

}
