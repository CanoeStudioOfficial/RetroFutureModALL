package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCoralBlock extends Block {

    private Block deadVersion;

    public BlockCoralBlock(String name, MapColor color) {
        super(Material.ROCK, color);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, name);
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name);
        this.setSoundType(SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public BlockCoralBlock deadVersion(Block block) {
        this.deadVersion = block;
        return this;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return this.deadVersion == null ? super.getItemDropped(state, rand, fortune) : Item.getItemFromBlock(this.deadVersion);
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!touchesWater(worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!touchesWater(worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return 60 + worldIn.rand.nextInt(40);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.deadVersion != null && !touchesWater(worldIn, pos)) {
            worldIn.setBlockState(pos, this.deadVersion.getDefaultState(), 3);
        }
    }

    private boolean touchesWater(World worldIn, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (AquaticWaterHelper.isWater(worldIn, pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }
}
