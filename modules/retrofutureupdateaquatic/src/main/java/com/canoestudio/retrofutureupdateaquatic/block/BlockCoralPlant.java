package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCoralPlant extends Block implements AquaticFluidloggable {

    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.9375D, 0.875D);
    private Block deadVersion;

    public BlockCoralPlant(String name, MapColor color) {
        super(Material.ROCK, color);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, name);
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name);
        this.setSoundType(SoundType.PLANT);
        this.setHardness(0.0F);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(WATERLOGGED, false));
    }

    public BlockCoralPlant deadVersion(Block block) {
        this.deadVersion = block;
        return this;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return AquaticWaterHelper.isSolidTop(worldIn, pos.down());
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        return this.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
            float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(WATERLOGGED, AquaticWaterHelper.isWater(worldIn, pos));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return AquaticWaterHelper.withActualWaterlogged(state, worldIn, pos, WATERLOGGED);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
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
        AquaticWaterHelper.ensureWaterlogged(worldIn, pos, state, WATERLOGGED);
        if (!hasWater(state, worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            AquaticWaterHelper.restoreWater(worldIn, pos, state);
        } else if (!hasWater(state, worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        } else {
            AquaticWaterHelper.scheduleFluidTick(worldIn, pos, state);
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return 60 + worldIn.rand.nextInt(40);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.deadVersion != null && !hasWater(state, worldIn, pos)) {
            worldIn.setBlockState(pos, this.deadVersion.getDefaultState()
                .withProperty(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
        }
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        if (AquaticWaterHelper.isWaterlogged(state, worldIn, pos, WATERLOGGED)) {
            AquaticWaterHelper.restoreWater(worldIn, pos, state);
        }
    }

    private boolean hasWater(IBlockState state, World worldIn, BlockPos pos) {
        if (AquaticWaterHelper.isWaterlogged(state, worldIn, pos, WATERLOGGED)) {
            return true;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (AquaticWaterHelper.isWater(worldIn, pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WATERLOGGED);
    }

    @Override
    public PropertyBool getWaterloggedProperty() {
        return WATERLOGGED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
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
