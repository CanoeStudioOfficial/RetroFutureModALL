package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityTadpole;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockFrogspawn extends Block {

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);
    private static final int MIN_HATCH_TICKS = 3600;
    private static final int MAX_HATCH_TICKS = 12000;

    public BlockFrogspawn() {
        super(Material.PLANTS);
        this.setRegistryName(RTWU.ID, "frogspawn");
        this.setTranslationKey(RTWU.ID + ".frogspawn");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(0.0f);
        this.setSoundType(SoundType.SLIME);
        this.setTickRandomly(true);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.isAirBlock(pos) && FluidloggedSupport.isWater(worldIn, pos.down());
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            worldIn.scheduleUpdate(pos, this, this.getHatchDelay(worldIn.rand));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    private boolean canBlockStay(World worldIn, BlockPos pos) {
        return FluidloggedSupport.isWater(worldIn, pos.down());
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }
        if (!this.canBlockStay(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            return;
        }

        int count = 2 + rand.nextInt(4);
        for (int i = 0; i < count; i++) {
            EntityTadpole tadpole = new EntityTadpole(worldIn);
            tadpole.setGrowingAgeTicks(rand.nextInt(1200));
            tadpole.setLocationAndAngles(pos.getX() + 0.2D + rand.nextDouble() * 0.6D,
                pos.getY() - 0.65D,
                pos.getZ() + 0.2D + rand.nextDouble() * 0.6D,
                rand.nextFloat() * 360.0F,
                0.0F);
            worldIn.spawnEntity(tadpole);
        }
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        worldIn.playEvent(2001, pos, Block.getStateId(state));
    }

    private int getHatchDelay(Random random) {
        return MIN_HATCH_TICKS + random.nextInt(MAX_HATCH_TICKS - MIN_HATCH_TICKS + 1);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
