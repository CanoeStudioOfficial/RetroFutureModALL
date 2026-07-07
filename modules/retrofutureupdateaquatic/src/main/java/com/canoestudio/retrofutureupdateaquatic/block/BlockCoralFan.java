package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterloggedBlock;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCoralFan extends Block implements RetroWaterloggedBlock {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.125D, 0.75D, 0.125D, 0.875D, 1.0D, 0.875D);
    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0625D, 0.3125D, 0.5D, 0.9375D, 0.6875D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0625D, 0.3125D, 0.0D, 0.9375D, 0.6875D, 0.5D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.5D, 0.3125D, 0.0625D, 1.0D, 0.6875D, 0.9375D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.3125D, 0.0625D, 0.5D, 0.6875D, 0.9375D);
    private Block deadVersion;

    public BlockCoralFan(String name, MapColor color) {
        super(Material.ROCK, color);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, name);
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name);
        this.setSoundType(SoundType.PLANT);
        this.setHardness(0.0F);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(FACING, EnumFacing.UP)
            .withProperty(WATERLOGGED, false));
    }

    public BlockCoralFan deadVersion(Block block) {
        this.deadVersion = block;
        return this;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canAttach(worldIn, pos, facing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canAttach(worldIn, pos, side);
    }

    private static boolean canAttach(World worldIn, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState support = worldIn.getBlockState(supportPos);
        return support.getBlockFaceShape(worldIn, supportPos, facing) == BlockFaceShape.SOLID;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
            float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState()
            .withProperty(FACING, canAttach(worldIn, pos, facing) ? facing : EnumFacing.UP)
            .withProperty(WATERLOGGED, AquaticWaterHelper.isWater(worldIn, pos));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return AquaticWaterHelper.withActualWaterlogged(state, worldIn, pos, WATERLOGGED);
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        return canAttach(worldIn, pos, state.getValue(FACING));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case DOWN:
                return AABB_DOWN;
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
            case SOUTH:
                return AABB_SOUTH;
            case NORTH:
                return AABB_NORTH;
            case UP:
            default:
                return AABB_UP;
        }
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
    public Material getMaterial(IBlockState state) {
        return this.getWaterloggedMaterial(state, super.getMaterial(state));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        AquaticWaterHelper.ensureWaterlogged(worldIn, pos, state, WATERLOGGED);
        this.syncWaterloggedAfterNeighborChanged(worldIn, pos, state);
        if (!hasWater(state, worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            AquaticWaterHelper.restoreWater(worldIn, pos, state);
            return;
        }

        this.syncWaterloggedAfterNeighborChanged(worldIn, pos, state);
        IBlockState updatedState = worldIn.getBlockState(pos);
        if (!hasWater(updatedState, worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        } else {
            AquaticWaterHelper.scheduleFluidTick(worldIn, pos, updatedState);
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
                .withProperty(FACING, state.getValue(FACING))
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
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        if (facing == null) {
            facing = EnumFacing.UP;
        }
        return this.getDefaultState().withProperty(FACING, facing).withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        if (state.getValue(WATERLOGGED)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, WATERLOGGED);
    }

    @Override
    public PropertyBool getWaterloggedProperty() {
        return WATERLOGGED;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
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
