package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidCompat;
import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
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
        IBlockState state = world.getBlockState(pos);
        return isWater(world, pos) || state.getBlock().isReplaceable(world, pos);
    }

    static void restoreWater(World world, BlockPos pos) {
        restoreWater(world, pos, world.getBlockState(pos));
    }

    static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        RetroFluidCompat.restoreWater(world, pos, replacedState, 3);
    }

    static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos, world.getBlockState(pos));
    }

    static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return RetroFluidCompat.getFluidState(world, pos, state);
    }

    static boolean isWater(RetroFluidState fluidState) {
        return RetroFluidCompat.isWater(fluidState);
    }

    static boolean isWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos, PropertyBool property) {
        return RetroFluidCompat.isWaterlogged(state, world, pos, property);
    }

    static IBlockState withActualWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return RetroFluidCompat.withActualWaterlogged(state, world, pos, property);
    }

    static void ensureWaterlogged(World world, BlockPos pos, IBlockState state, PropertyBool property) {
        RetroFluidCompat.ensureWaterlogged(world, pos, state, property, 3);
    }

    static void setWaterloggedProperty(World world, BlockPos pos, IBlockState state, PropertyBool property,
            boolean waterlogged, int flags) {
        RetroFluidCompat.setWaterloggedProperty(world, pos, state, property, waterlogged, flags);
    }

    static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroFluidCompat.scheduleFluidTick(world, pos, state);
    }

    static boolean isSolidTop(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isSideSolid(world, pos, EnumFacing.UP) || block.canPlaceTorchOnTop(state, world, pos);
    }
}
