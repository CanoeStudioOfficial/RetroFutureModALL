package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.Fluid;

public class BlockSeaPickle extends BlockBush implements IGrowable, IFluidloggable {

    public static final PropertyInteger PICKLES = PropertyInteger.create("pickles", 1, 4);
    private static final AxisAlignedBB[] AABBS = {
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D),
        new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.4375D, 0.8125D),
        new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.875D, 0.375D, 0.8125D),
        new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.4375D, 0.875D)
    };

    public BlockSeaPickle() {
        super(Material.PLANTS);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "sea_pickle");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".sea_pickle");
        this.setSoundType(SoundType.SLIME);
        this.setHardness(0.1F);
        this.setResistance(10.0F);
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(PICKLES, 1));
    }

    @Override
    public int getLightValue(IBlockState state) {
        return 0;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AquaticWaterHelper.isWater(world, pos)
            ? state.getValue(PICKLES) * 3 + 3 : 0;
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return super.getMaterial(state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return AquaticWaterHelper.isWater(worldIn, pos)
            && AquaticWaterHelper.isSolidTop(worldIn, pos.down());
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        return AquaticWaterHelper.isWater(worldIn, pos)
            && AquaticWaterHelper.isSolidTop(worldIn, pos.down());
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
            float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn,
            BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            AquaticWaterHelper.restoreWater(worldIn, pos, state);
        }
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        AquaticWaterHelper.restoreWater(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABBS[state.getValue(PICKLES) - 1];
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return AABBS[blockState.getValue(PICKLES) - 1];
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return state.getValue(PICKLES);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(PICKLES) < 4
            && worldIn.getBlockState(pos.down()).getBlock() instanceof BlockCoralBlock;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return this.canGrow(worldIn, pos, state, true);
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (!this.canGrow(worldIn, pos, state, false)) {
            return;
        }

        IBlockState grown = state.withProperty(PICKLES,
            Math.min(4, state.getValue(PICKLES) + rand.nextInt(3) + 1));
        FluidloggedSupport.setFluidloggableBlock(worldIn, pos, grown, 3);
        spreadOnCoral(worldIn, pos, rand);
    }

    private void spreadOnCoral(World worldIn, BlockPos pos, Random rand) {
        for (int i = 0; i < 8; i++) {
            BlockPos target = pos.add(rand.nextInt(7) - 3, rand.nextInt(3) - 1, rand.nextInt(7) - 3);
            if (AquaticWaterHelper.isWater(worldIn, target)
                    && worldIn.getBlockState(target.down()).getBlock() instanceof BlockCoralBlock
                    && this.canPlaceBlockAt(worldIn, target)) {
                FluidloggedSupport.setFluidloggableBlock(worldIn, target, this.getDefaultState()
                    .withProperty(PICKLES, rand.nextInt(4) + 1), 3);
                return;
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(PICKLES, (meta & 3) + 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PICKLES) - 1;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PICKLES);
    }

    @Override
    public boolean isFluidValid(IBlockState state, World world, BlockPos pos, Fluid fluid) {
        return FluidloggedSupport.isWater(fluid);
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos,
            EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
