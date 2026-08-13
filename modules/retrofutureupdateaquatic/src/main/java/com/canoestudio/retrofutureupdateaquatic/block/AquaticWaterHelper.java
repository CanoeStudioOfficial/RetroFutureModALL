package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

final class AquaticWaterHelper {

    private AquaticWaterHelper() {
    }

    static boolean isWater(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return isWater(getFluidState(world, pos, state));
    }

    static boolean isWaterOrBubble(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return isWater(world, pos) || state.getBlock() == ModBlocks.BUBBLE_COLUMN;
    }

    static boolean canReplaceWater(World world, BlockPos pos) {
        return FluidloggedSupport.canReplaceWater(world, pos);
    }

    static void restoreWater(World world, BlockPos pos) {
        restoreWater(world, pos, world.getBlockState(pos));
    }

    static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        FluidloggedSupport.restoreFluidOrAir(world, pos, replacedState, 3);
    }

    static FluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos, world.getBlockState(pos));
    }

    static FluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return FluidloggedSupport.getFluidState(world, pos, state);
    }

    static boolean isWater(FluidState fluidState) {
        return FluidloggedSupport.isWater(fluidState);
    }

    static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        FluidloggedSupport.scheduleFluidTick(world, pos, state);
    }

    static boolean isSolidTop(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isSideSolid(world, pos, EnumFacing.UP) || block.canPlaceTorchOnTop(state, world, pos);
    }
}
