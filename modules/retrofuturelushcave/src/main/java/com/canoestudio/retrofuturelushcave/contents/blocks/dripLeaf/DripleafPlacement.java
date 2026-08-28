package com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public final class DripleafPlacement {
    private DripleafPlacement() {
    }

    public static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || isWater(world, pos)
                || state.getBlock() == ModBlocks.SMALL_DRIPLEAF;
    }

    public static boolean isSmallDripleafGround(IBlockState state) {
        return state.getBlock() == Blocks.CLAY || state.getBlock() == ModBlocks.MOSS_BLOCK;
    }

    public static boolean isBigDripleafGround(IBlockState state) {
        Block block = state.getBlock();
        return isSmallDripleafGround(state) || block == Blocks.GRASS || block == Blocks.DIRT
                || block == Blocks.MYCELIUM || block == Blocks.FARMLAND
                || block == ModBlocks.ROOTED_DIRT;
    }

    public static boolean canPlaceHead(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return headState(world, pos, EnumFacing.NORTH) != null && canReplace(world, pos)
                && (BigDripleaf.isBigDripleafBlock(below.getBlock())
                || below.getBlock() == ModBlocks.DRIPLEAF_STEM
                || isBigDripleafGround(below));
    }

    public static boolean placeHead(World world, BlockPos pos, EnumFacing facing) {
        if (!canPlaceHead(world, pos)) {
            return false;
        }
        return setFluidloggableBlock(world, pos, headState(world, pos, facing), 3);
    }

    public static boolean canPlaceBig(World world, BlockPos pos, int stemHeight) {
        if (stemHeight < 0 || headState(world, pos.up(stemHeight), EnumFacing.NORTH) == null
                || stemHeight > 0 && stemState(world, pos, EnumFacing.NORTH) == null
                || !isBigDripleafGround(world.getBlockState(pos.down()))) {
            return false;
        }
        for (int y = 0; y <= stemHeight; y++) {
            if (!canReplace(world, pos.up(y))) {
                return false;
            }
        }
        return true;
    }

    public static boolean placeBig(World world, BlockPos pos, EnumFacing facing, int stemHeight) {
        if (!canPlaceBig(world, pos, stemHeight)) {
            return false;
        }
        for (int y = 0; y < stemHeight; y++) {
            BlockPos stemPos = pos.up(y);
            setFluidloggableBlock(world, stemPos, stemState(world, stemPos, facing), 3);
        }
        BlockPos headPos = pos.up(stemHeight);
        setFluidloggableBlock(world, headPos, headState(world, headPos, facing), 3);
        return true;
    }

    public static boolean placeWithRandomHeight(World world, Random random, BlockPos pos,
                                                EnumFacing facing) {
        if (headState(world, pos, facing) == null || stemState(world, pos, facing) == null
                || !isBigDripleafGround(world.getBlockState(pos.down()))) {
            return false;
        }
        int desiredHeight = 2 + random.nextInt(4);
        int height = 0;
        while (height < desiredHeight && canReplace(world, pos.up(height))) {
            height++;
        }
        if (height == 0) {
            return false;
        }
        for (int y = 0; y < height - 1; y++) {
            BlockPos stemPos = pos.up(y);
            setFluidloggableBlock(world, stemPos, stemState(world, stemPos, facing), 3);
        }
        BlockPos headPos = pos.up(height - 1);
        setFluidloggableBlock(world, headPos, headState(world, headPos, facing), 3);
        return true;
    }

    public static boolean growHead(World world, BlockPos headPos, EnumFacing facing) {
        if (!BigDripleaf.isBigDripleafBlock(world.getBlockState(headPos).getBlock())
                || !canReplace(world, headPos.up())) {
            return false;
        }
        IBlockState stem = stemState(world, headPos, facing);
        IBlockState head = headState(world, headPos.up(), facing);
        if (stem == null || head == null) {
            return false;
        }
        setFluidloggableBlock(world, headPos, stem, 2);
        setFluidloggableBlock(world, headPos.up(), head, 3);
        return true;
    }

    public static IBlockState stemState(World world, BlockPos pos, EnumFacing facing) {
        return ModBlocks.DRIPLEAF_STEM.getDefaultState()
                .withProperty(DripleafStem.FACING, facing)
                .withProperty(DripleafStem.WATERLOGGED,
                        !RetroWaterlogging.isFluidloggedAvailable() && isWater(world, pos));
    }

    public static IBlockState headState(World world, BlockPos pos, EnumFacing facing) {
        BigDripleaf block = !RetroWaterlogging.isFluidloggedAvailable() && isWater(world, pos)
                ? ModBlocks.BIG_DRIPLEAF_WATERLOGGED : ModBlocks.BIG_DRIPLEAF;
        return block.getDefaultState().withProperty(BigDripleaf.FACING, facing);
    }

    private static boolean isWater(World world, BlockPos pos) {
        return RetroWaterlogging.isWater(world, pos);
    }

    private static boolean setFluidloggableBlock(World world, BlockPos pos, IBlockState state, int flags) {
        RetroWaterlogging.setFluidloggableBlock(world, pos, state, flags);
        return world.getBlockState(pos).getBlock() == state.getBlock();
    }
}
