package com.canoestudio.retrofutureupdateaquatic.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
        return state.getMaterial() == Material.WATER || state.getBlock() == Blocks.WATER
            || state.getBlock() == Blocks.FLOWING_WATER;
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
        if (!world.provider.doesWaterVaporize()) {
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
        } else {
            world.setBlockToAir(pos);
        }
    }

    static boolean isSolidTop(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isSideSolid(world, pos, EnumFacing.UP) || block.canPlaceTorchOnTop(state, world, pos);
    }
}
