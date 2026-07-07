package com.canoestudio.retrofutureupdateaquatic.block;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

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
        IBlockState state = world.getBlockState(pos);
        return isWater(world, pos) || state.getBlock().isReplaceable(world, pos);
    }

    static void restoreWater(World world, BlockPos pos) {
        restoreWater(world, pos, world.getBlockState(pos));
    }

    static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        if (!world.provider.doesWaterVaporize()) {
            FluidState fluidState = getFluidState(world, pos, replacedState);
            world.setBlockState(pos, isWater(fluidState) ? fluidState.getState() : Blocks.WATER.getDefaultState(), 3);
        } else {
            world.setBlockToAir(pos);
        }
    }

    static FluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos, world.getBlockState(pos));
    }

    static FluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return state.getMaterial() == Material.WATER || state.getBlock() == Blocks.WATER
            || state.getBlock() == Blocks.FLOWING_WATER
            ? FluidState.of(state)
            : FluidloggedUtils.getFluidState(world, pos, state);
    }

    static boolean isWater(FluidState fluidState) {
        return !fluidState.isEmpty() && FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluidState.getFluid());
    }

    static boolean isWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos, PropertyBool property) {
        return state.getValue(property) || isWater(getFluidState(world, pos, state));
    }

    static IBlockState withActualWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return state.withProperty(property, isWaterlogged(state, world, pos, property));
    }

    static void ensureWaterlogged(World world, BlockPos pos, IBlockState state, PropertyBool property) {
        if (!world.isRemote && state.getValue(property) && !world.provider.doesWaterVaporize()
                && !isWater(getFluidState(world, pos, state))) {
            FluidloggedUtils.setFluidState(world, pos, state, FluidState.of(FluidRegistry.WATER), false, 3);
        }
    }

    static void setWaterloggedProperty(World world, BlockPos pos, IBlockState state, PropertyBool property,
            boolean waterlogged, int flags) {
        if (state.getValue(property) != waterlogged) {
            world.setBlockState(pos, state.withProperty(property, waterlogged), flags);
        }
    }

    static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        FluidState fluidState = getFluidState(world, pos, state);
        if (isWater(fluidState)) {
            world.scheduleUpdate(pos, fluidState.getState().getBlock(), fluidState.getState().getBlock().tickRate(world));
        }
    }

    static boolean isSolidTop(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isSideSolid(world, pos, EnumFacing.UP) || block.canPlaceTorchOnTop(state, world, pos);
    }
}
