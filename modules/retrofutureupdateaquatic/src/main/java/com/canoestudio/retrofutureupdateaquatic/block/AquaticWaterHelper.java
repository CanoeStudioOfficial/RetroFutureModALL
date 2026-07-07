package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
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
        return RetroWaterlogging.canReplaceWater(world, pos);
    }

    static void restoreWater(World world, BlockPos pos) {
        restoreWater(world, pos, world.getBlockState(pos));
    }

    static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        RetroWaterlogging.restoreWater(world, pos, replacedState);
    }

    static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos, world.getBlockState(pos));
    }

    static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return RetroWaterlogging.getFluidState(world, pos, state);
    }

    static boolean isWater(RetroFluidState fluidState) {
        return RetroWaterlogging.isWater(fluidState);
    }

    static boolean isWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos, PropertyBool property) {
        return RetroWaterlogging.isWaterlogged(state, world, pos, property);
    }

    static IBlockState withActualWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return RetroWaterlogging.actualState(state, world, pos, property);
    }

    static void ensureWaterlogged(World world, BlockPos pos, IBlockState state, PropertyBool property) {
        RetroWaterlogging.onBlockAdded(world, pos, state, property);
    }

    static void setWaterloggedProperty(World world, BlockPos pos, IBlockState state, PropertyBool property,
            boolean waterlogged, int flags) {
        if (state.getValue(property) != waterlogged) {
            world.setBlockState(pos, state.withProperty(property, waterlogged), flags);
        }
    }

    static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroWaterlogging.scheduleFluidTick(world, pos, state);
    }

    static boolean isSolidTop(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isSideSolid(world, pos, EnumFacing.UP) || block.canPlaceTorchOnTop(state, world, pos);
    }
}
