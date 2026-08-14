package com.canoestudio.retrofuturethewildupdate.world.gen;

import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangrovePropagule;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class WorldGenMangroveTree extends WorldGenAbstractTree {

    private static final int MIN_HEIGHT = 5;
    private static final int HEIGHT_VARIATION = 4;

    public WorldGenMangroveTree(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        int height = MIN_HEIGHT + random.nextInt(HEIGHT_VARIATION);
        if (!canGenerateAt(world, pos, height)) {
            return false;
        }

        IBlockState log = ModBlocks.MANGROVE_LOG.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y);
        for (int y = 0; y < height; y++) {
            setBlockAndNotifyAdequately(world, pos.up(y), log);
        }

        BlockPos crown = pos.up(height);
        generateCanopy(world, random, crown);
        generateRoots(world, random, pos);
        hangPropagules(world, random, crown);
        return true;
    }

    private void generateCanopy(World world, Random random, BlockPos crown) {
        IBlockState leaves = ModBlocks.MANGROVE_LEAVES.getDefaultState()
            .withProperty(BlockLeaves.CHECK_DECAY, false)
            .withProperty(BlockLeaves.DECAYABLE, true);

        for (int y = -2; y <= 1; y++) {
            int radius = y <= -1 ? 3 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = Math.abs(x) + Math.abs(z);
                    if (distance <= radius + 1 && (distance <= radius || random.nextBoolean())) {
                        placeLeaf(world, crown.add(x, y, z), leaves);
                    }
                }
            }
        }

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos branch = crown.offset(facing, 2).down(random.nextInt(2));
            IBlockState branchLog = ModBlocks.MANGROVE_LOG.getDefaultState().withProperty(BlockRotatedPillar.AXIS, facing.getAxis());
            setReplaceable(world, crown.offset(facing), branchLog);
            setReplaceable(world, branch, branchLog);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 2) {
                        placeLeaf(world, branch.add(x, 0, z), leaves);
                    }
                }
            }
        }
    }

    private void generateRoots(World world, Random random, BlockPos pos) {
        IBlockState root = ModBlocks.MANGROVE_ROOTS.getDefaultState();
        IBlockState muddyRoot = ModBlocks.MUDDY_MANGROVE_ROOTS.getDefaultState();

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            int length = 2 + random.nextInt(3);
            for (int i = 1; i <= length; i++) {
                BlockPos rootPos = pos.offset(facing, i).up(Math.max(0, 2 - i));
                if (canReplaceMangrove(world, rootPos)) {
                    setBlockAndNotifyAdequately(world, rootPos, rootPos.getY() <= pos.getY() ? muddyRoot : root);
                }
                BlockPos down = rootPos.down();
                if (i > 1 && canReplaceMangrove(world, down)) {
                    setBlockAndNotifyAdequately(world, down, muddyRoot);
                }
            }
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos mudPos = pos.add(x, -1, z);
                if (Math.abs(x) + Math.abs(z) <= 3 && isSoftGround(world.getBlockState(mudPos))) {
                    setBlockAndNotifyAdequately(world, mudPos, ModBlocks.MUD.getDefaultState());
                }
            }
        }
    }

    private void hangPropagules(World world, Random random, BlockPos crown) {
        for (int i = 0; i < 5; i++) {
            BlockPos pos = crown.add(random.nextInt(7) - 3, -3 - random.nextInt(2), random.nextInt(7) - 3);
            if (world.isAirBlock(pos) && world.getBlockState(pos.up()).getBlock() == ModBlocks.MANGROVE_LEAVES) {
                setBlockAndNotifyAdequately(world, pos, ModBlocks.MANGROVE_PROPAGULE.getDefaultState().withProperty(BlockMangrovePropagule.HANGING, true));
            }
        }
    }

    private boolean canGenerateAt(World world, BlockPos pos, int height) {
        if (!isSoftGround(world.getBlockState(pos.down()))) {
            return false;
        }

        for (int y = 0; y <= height + 2; y++) {
            BlockPos check = pos.up(y);
            if (!canReplaceMangrove(world, check)) {
                return false;
            }
        }
        return true;
    }

    private void placeLeaf(World world, BlockPos pos, IBlockState state) {
        if (canReplaceMangrove(world, pos)) {
            setBlockAndNotifyAdequately(world, pos, state);
        }
    }

    private void setReplaceable(World world, BlockPos pos, IBlockState state) {
        if (canReplaceMangrove(world, pos)) {
            setBlockAndNotifyAdequately(world, pos, state);
        }
    }

    private static boolean isSoftGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS
            || block == Blocks.DIRT
            || block == Blocks.CLAY
            || block == ModBlocks.MUD
            || state.getMaterial() == Material.GROUND;
    }

    private static boolean canReplaceMangrove(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block == Blocks.AIR
            || block == Blocks.TALLGRASS
            || FluidloggedSupport.isWater(world, pos)
            || block.isLeaves(state, world, pos)
            || block.isReplaceable(world, pos);
    }
}
