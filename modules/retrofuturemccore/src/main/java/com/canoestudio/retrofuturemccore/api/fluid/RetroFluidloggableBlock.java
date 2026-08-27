package com.canoestudio.retrofuturemccore.api.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Marker and lightweight behavior contract for blocks that can keep a fluid layer
 * through RetroFuture's optional Fluidlogged API bridge.
 */
public interface RetroFluidloggableBlock {

    default boolean isRetroFluidloggable(IBlockState state, IBlockAccess world, BlockPos pos,
            RetroFluidState fluidState) {
        return fluidState == null || fluidState.isEmpty() || fluidState.isWater();
    }

    default boolean canRetroFluidFlow(IBlockAccess world, BlockPos pos, IBlockState state,
            EnumFacing side) {
        return true;
    }

    default boolean canRetroFluidConnect(IBlockAccess world, BlockPos pos, IBlockState state,
            EnumFacing side) {
        return this.canRetroFluidFlow(world, pos, state, side);
    }

    default EnumActionResult onRetroFluidChange(World world, BlockPos pos, IBlockState state,
            RetroFluidState newFluidState, int flags) {
        return newFluidState != null && !newFluidState.isEmpty()
            ? onRetroFluidFill(world, pos, state, newFluidState, flags)
            : onRetroFluidDrain(world, pos, state, flags);
    }

    default EnumActionResult onRetroFluidFill(World world, BlockPos pos, IBlockState state,
            RetroFluidState newFluidState, int flags) {
        return EnumActionResult.PASS;
    }

    default EnumActionResult onRetroFluidDrain(World world, BlockPos pos, IBlockState state, int flags) {
        return EnumActionResult.PASS;
    }
}
