package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterloggedBlock;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Random;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class AmethystClusterBlock extends FluidloggableDirectionalBlock implements RetroWaterloggedBlock {
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private final int height;
    private final int offset;
    private final boolean dropsShard;

    public AmethystClusterBlock(String name, int height, int offset, boolean dropsShard) {
        super(Material.GLASS);
        this.height = height;
        this.offset = offset;
        this.dropsShard = dropsShard;
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setHardness(1.5F);
        setResistance(3.0F);
        setHarvestLevel("pickaxe", 0);
        setSoundType(SoundType.GLASS);
        setCreativeTab(CREATIVE_TABS);
        setLightLevel(dropsShard ? 5.0F / 15.0F : 1.0F / 15.0F);
        setDefaultState(RetroWaterlogging.withStillWaterLevel(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(WATERLOGGED, false)));

        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName(name.toLowerCase()));
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, facing)
                .withProperty(WATERLOGGED, RetroWaterlogging.isWater(worldIn, pos));
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return RetroWaterlogging.extendedState(state, worldIn, pos);
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof AmethystClusterBlock && state.getValue(WATERLOGGED);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        RetroWaterlogging.onBlockAdded(worldIn, pos, state, WATERLOGGED);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canBlockStay(worldIn, pos, state)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            restoreFluidOrAir(worldIn, pos, state, 3);
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && isWaterlogged(state)) {
            com.canoestudio.retrofuturemccore.api.fluid.WaterloggedPlantFluid.updateTick(
                    world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   net.minecraft.entity.player.EntityPlayer player,
                                   boolean willHarvest) {
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

    private boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canAttach(world, pos, state.getValue(FACING));
    }

    private boolean canAttach(World world, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState support = world.getBlockState(supportPos);
        return support.isSideSolid(world, supportPos, facing);
    }

    private void restoreFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, flags);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        double min = offset / 16.0D;
        double max = (16 - offset) / 16.0D;
        double h = height / 16.0D;

        switch (facing) {
            case DOWN:
                return new AxisAlignedBB(min, 1.0D - h, min, max, 1.0D, max);
            case NORTH:
                return new AxisAlignedBB(min, min, 1.0D - h, max, max, 1.0D);
            case SOUTH:
                return new AxisAlignedBB(min, min, 0.0D, max, max, h);
            case WEST:
                return new AxisAlignedBB(1.0D - h, min, min, 1.0D, max, max);
            case EAST:
                return new AxisAlignedBB(0.0D, min, min, h, max, max);
            case UP:
            default:
                return new AxisAlignedBB(min, 0.0D, min, max, h, max);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return dropsShard ? ModItems.AMETHYST_SHARD : Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random random) {
        return dropsShard ? 4 : 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (!dropsShard) {
            return 1;
        }
        return Math.min(16, quantityDropped(random) + random.nextInt(fortune + 1));
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7))
                .withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        return state.getValue(WATERLOGGED) ? meta | 8 : meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, FACING, WATERLOGGED);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return getWaterloggedMaterial(state, super.getMaterial(state));
    }

    @Override
    public PropertyBool getWaterloggedProperty() {
        return WATERLOGGED;
    }

    @Override
    public net.minecraft.util.math.Vec3d modifyAcceleration(World world, BlockPos pos,
                                                               net.minecraft.entity.Entity entity,
                                                               net.minecraft.util.math.Vec3d motion) {
        return isWaterlogged(world.getBlockState(pos))
                ? net.minecraft.init.Blocks.WATER.modifyAcceleration(world, pos, entity, motion)
                : motion;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterlogged(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (isWaterlogged(state)
                && world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }
}
