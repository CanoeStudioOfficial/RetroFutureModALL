package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterloggedBlock;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import com.canoestudio.retrofuturemccore.api.fluid.WaterloggedPlantFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class HangingRootsBlock extends Block implements RetroWaterloggedBlock {
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB ROOTS_AABB = new AxisAlignedBB(0.125D, 0.625D, 0.125D, 0.875D, 1.0D, 0.875D);

    public HangingRootsBlock() {
        super(Material.VINE);
        setTranslationKey(Tags.MOD_ID + ".hanging_roots");
        setRegistryName("hanging_roots");
        setHardness(0.1F);
        setSoundType(SoundType.PLANT);
        setCreativeTab(CREATIVE_TABS);
        setLightOpacity(0);
        setDefaultState(RetroWaterlogging.withStillWaterLevel(blockState.getBaseState()
                .withProperty(WATERLOGGED, false)));

        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName("hanging_roots"));
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return (worldIn.isAirBlock(pos) || RetroWaterlogging.isWater(worldIn, pos))
                && worldIn.isSideSolid(pos.up(), EnumFacing.DOWN, true);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(WATERLOGGED,
                RetroWaterlogging.isWater(worldIn, pos));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        RetroWaterlogging.onBlockAdded(worldIn, pos, state, WATERLOGGED);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) {
            return;
        }
        if (!canPlaceBlockAt(worldIn, pos)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            RetroWaterlogging.restoreWater(worldIn, pos, state);
            return;
        }
        RetroWaterlogging.onNeighborChanged(worldIn, pos, state);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, java.util.Random random) {
        if (worldIn.isRemote || worldIn.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canPlaceBlockAt(worldIn, pos)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            RetroWaterlogging.restoreContainedFluidOrAir(worldIn, pos, state, 3);
        } else if (RetroWaterlogging.isWaterlogged(state, worldIn, pos, WATERLOGGED)) {
            WaterloggedPlantFluid.updateTick(worldIn, pos, worldIn.getBlockState(pos));
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
            net.minecraft.entity.player.EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, world.isRemote ? 11 : 3);
        if (!RetroWaterlogging.isWaterlogged(state, world, pos, WATERLOGGED)) {
            world.setBlockToAir(pos);
        }
        return true;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        boolean waterlogged = RetroWaterlogging.isWaterlogged(state, world, pos, WATERLOGGED);
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, 3);
        if (!waterlogged) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return ROOTS_AABB;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return RetroWaterlogging.extendedState(state, worldIn, pos);
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

    @Override
    public Material getMaterial(IBlockState state) {
        return getWaterloggedMaterial(state, super.getMaterial(state));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, WATERLOGGED);
    }

    @Override
    public PropertyBool getWaterloggedProperty() {
        return WATERLOGGED;
    }
}
