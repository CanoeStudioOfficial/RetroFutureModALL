package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.utils.FluidloggedCompat;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

@Optional.Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api")
public class GlowLichenBlock extends Block implements IFluidloggable {

    @Override
    public boolean isFluidValid(IBlockState state, World world, BlockPos pos, Fluid fluid) {
        return FluidloggedSupport.isWater(fluid);
    }
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");

    private static final AxisAlignedBB FULL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public GlowLichenBlock() {
        super(Material.VINE);
        setTranslationKey(Tags.MOD_ID + ".glow_lichen");
        setRegistryName("glow_lichen");
        setHardness(0.2F);
        setResistance(0.2F);
        setSoundType(SoundType.PLANT);
        setCreativeTab(CREATIVE_TABS);
        setLightLevel(7.0F / 15.0F);
        setDefaultState(blockState.getBaseState()
                .withProperty(UP, false)
                .withProperty(DOWN, false)
                .withProperty(NORTH, true)
                .withProperty(EAST, false)
                .withProperty(SOUTH, false)
                .withProperty(WEST, false));

        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName("glow_lichen"));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing attachment = facing.getOpposite();
        if (canAttach(worldIn, pos, attachment)) {
            return getStateForFace(attachment);
        }

        for (EnumFacing fallback : EnumFacing.values()) {
            if (canAttach(worldIn, pos, fallback)) {
                return getStateForFace(fallback);
            }
        }

        return getDefaultState();
    }

    public IBlockState getStateForFace(EnumFacing face) {
        return getDefaultState()
                .withProperty(UP, face == EnumFacing.UP)
                .withProperty(DOWN, face == EnumFacing.DOWN)
                .withProperty(NORTH, face == EnumFacing.NORTH)
                .withProperty(EAST, face == EnumFacing.EAST)
                .withProperty(SOUTH, face == EnumFacing.SOUTH)
                .withProperty(WEST, face == EnumFacing.WEST);
    }

    /**
     * 供世界生成使用：若当前位置已有本方块则在保留既有面的基础上增加一面；
     * 若当前位置是空气或水，则创建只带该面的新状态。返回null表示该面已经存在或目标不可替换。
     */
    public IBlockState getStateWithFace(IBlockState current, EnumFacing face) {
        if (current.getBlock() == this) {
            return hasFace(current, face) ? null : setFace(current, face, true);
        }
        return current.getMaterial() == Material.AIR || current.getMaterial().isLiquid()
                ? getStateForFace(face) : null;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canAttach(worldIn, pos, side.getOpposite());
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

    private boolean canAttach(World world, BlockPos pos, EnumFacing face) {
        BlockPos supportPos = pos.offset(face);
        return world.getBlockState(supportPos).isSideSolid(world, supportPos, face.getOpposite());
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        IBlockState updated = removeUnsupportedFaces(worldIn, pos, state);
        if (!hasAnyFace(updated)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            if (Loader.isModLoaded("fluidlogged_api")) {
                FluidloggedCompat.restoreFluidOrAir(worldIn, pos, updated, 3);
            }
            return;
        }
        if (updated != state) {
            worldIn.setBlockState(pos, updated, 2);
        }
        if (Loader.isModLoaded("fluidlogged_api")) {
            FluidloggedCompat.scheduleFluidTick(worldIn, pos, updated);
        }
    }

    private IBlockState removeUnsupportedFaces(World world, BlockPos pos, IBlockState state) {
        IBlockState updated = state;
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(updated, face) && !canAttach(world, pos, face)) {
                updated = setFace(updated, face, false);
            }
        }
        return updated;
    }

    private boolean hasAnyFace(IBlockState state) {
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFace(IBlockState state, EnumFacing face) {
        return state.getValue(propertyFor(face));
    }

    private IBlockState setFace(IBlockState state, EnumFacing face, boolean value) {
        return state.withProperty(propertyFor(face), value);
    }

    private PropertyBool propertyFor(EnumFacing face) {
        switch (face) {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case EAST:
                return EAST;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case NORTH:
            default:
                return NORTH;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_AABB;
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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {UP, DOWN, NORTH, EAST, SOUTH, WEST});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                return face.getIndex();
            }
        }
        return EnumFacing.NORTH.getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getStateForFace(EnumFacing.byIndex(meta & 7));
    }
}
