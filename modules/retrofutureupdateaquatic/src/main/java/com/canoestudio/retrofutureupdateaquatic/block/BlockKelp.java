package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockKelp extends BlockBush implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
    public static final PropertyBool TOP = PropertyBool.create("top");
    private static final AxisAlignedBB KELP_AABB =
        new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    private static final AxisAlignedBB KELP_TOP_AABB =
        new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.5625D, 0.875D);

    public BlockKelp() {
        super(Material.WATER);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "kelp");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".kelp");
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
        this.setDefaultState(RetroWaterlogging.withStillWaterLevel(this.blockState.getBaseState()
            .withProperty(AGE, 0)
            .withProperty(TOP, true)));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
            float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(AGE, worldIn.rand.nextInt(15)).withProperty(TOP, isTop(worldIn, pos));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(TOP, isTop(worldIn, pos));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return isTop(source, pos) ? KELP_TOP_AABB : KELP_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return AquaticWaterHelper.isWater(worldIn, pos) && this.canBlockStay(worldIn, pos, this.getDefaultState());
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState down = worldIn.getBlockState(pos.down());
        return AquaticWaterHelper.isSolidTop(worldIn, pos.down()) || down.getBlock() == this;
    }

    @Override
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            AquaticWaterHelper.restoreWater(worldIn, pos);
        }
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        AquaticWaterHelper.restoreWater(worldIn, pos);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isAreaLoaded(pos, 1) || state.getValue(AGE) >= 15 || rand.nextInt(3) != 0) {
            return;
        }
        grow(worldIn, rand, pos, state);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < 15 || findTop(worldIn, pos).getY() < worldIn.getSeaLevel() - 1;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos top = findTop(worldIn, pos);
        IBlockState topState = worldIn.getBlockState(top);
        int age = topState.getBlock() == this ? topState.getValue(AGE) : state.getValue(AGE);
        if (age >= 15) {
            return;
        }
        if (AquaticWaterHelper.isWater(worldIn, top.up())) {
            worldIn.setBlockState(top, this.getDefaultState().withProperty(AGE, age).withProperty(TOP, false), 2);
            worldIn.setBlockState(top.up(), this.getDefaultState().withProperty(AGE, age + 1).withProperty(TOP, true), 3);
        } else {
            worldIn.setBlockState(top, topState.withProperty(AGE, age + 1).withProperty(TOP, true), 2);
        }
    }

    public void growColumn(World worldIn, Random rand, BlockPos pos, int height) {
        BlockPos cursor = pos;
        for (int i = 0; i < height && this.canPlaceBlockAt(worldIn, cursor); i++) {
            int age = i == height - 1 ? Math.min(15, rand.nextInt(15)) : 0;
            worldIn.setBlockState(cursor, this.getDefaultState().withProperty(AGE, age).withProperty(TOP, i == height - 1), 2);
            cursor = cursor.up();
        }
    }

    private BlockPos findTop(World worldIn, BlockPos pos) {
        BlockPos cursor = pos;
        while (worldIn.getBlockState(cursor.up()).getBlock() == this) {
            cursor = cursor.up();
        }
        return cursor;
    }

    private boolean isTop(IBlockAccess worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.up()).getBlock() != this;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.KELP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, AGE, TOP);
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
