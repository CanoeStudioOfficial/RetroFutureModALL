package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import java.util.Random;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMud extends BlockWildSimple {

    public BlockMud() {
        super("mud", Material.GROUND, SoundType.GROUND, 0.5F, 0.5F, CreativeTabs.BUILDING_BLOCKS);
        this.setTickRandomly(true);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (!worldIn.isRemote && random.nextInt(3) == 0 && hasDryingDripstoneBelow(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.CLAY.getDefaultState(), 3);
        }
    }

    private static boolean hasDryingDripstoneBelow(World world, BlockPos pos) {
        for (int i = 1; i <= 4; i++) {
            IBlockState below = world.getBlockState(pos.down(i));
            if (below.getBlock() == com.canoestudio.retrofuturemc.contents.blocks.ModBlocks.POINTED_DRIPSTONE) {
                return true;
            }
            if (!below.getMaterial().isReplaceable() && below.getMaterial() != Material.WATER
                && below.getBlock() != com.canoestudio.retrofuturemc.contents.blocks.ModBlocks.DRIPSTONE_BLOCK) {
                return false;
            }
        }
        return false;
    }
}
