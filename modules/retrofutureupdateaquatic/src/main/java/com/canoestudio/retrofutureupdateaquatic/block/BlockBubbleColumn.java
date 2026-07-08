package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBubbleColumn extends Block {

    public static final PropertyBool DRAG = PropertyBool.create("drag");

    public BlockBubbleColumn() {
        super(Material.WATER);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "bubble_column");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".bubble_column");
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
        this.setLightOpacity(1);
        this.setDefaultState(RetroWaterlogging.withStillWaterLevel(this.blockState.getBaseState()
            .withProperty(DRAG, false)));
    }

    public static boolean isColumnBase(IBlockState state) {
        return state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.MAGMA;
    }

    public static boolean isDragBase(IBlockState state) {
        return state.getBlock() == Blocks.MAGMA;
    }

    public static boolean updateColumn(World world, BlockPos start) {
        IBlockState base = world.getBlockState(start.down());
        if (!isColumnBase(base) || !AquaticWaterHelper.isWaterOrBubble(world, start)) {
            return false;
        }

        boolean drag = isDragBase(base);
        BlockPos cursor = start;
        boolean placed = false;
        while (cursor.getY() < world.getHeight() - 1 && AquaticWaterHelper.isWaterOrBubble(world, cursor)) {
            world.setBlockState(cursor, ModBlocks.BUBBLE_COLUMN.getDefaultState().withProperty(DRAG, drag), 3);
            placed = true;
            cursor = cursor.up();
        }
        return placed;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && !updateColumn(worldIn, pos)) {
            AquaticWaterHelper.restoreWater(worldIn, pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (state.getValue(DRAG)) {
            entityIn.motionY = Math.max(entityIn.motionY - 0.045D, -0.28D);
            entityIn.fallDistance = 0.0F;
        } else {
            entityIn.motionY = Math.min(entityIn.motionY + 0.075D, 0.35D);
            entityIn.fallDistance = 0.0F;
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return AquaticWaterHelper.isWaterOrBubble(worldIn, pos) && isColumnBase(worldIn.getBlockState(pos.down()));
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DRAG) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DRAG, (meta & 1) == 1);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, DRAG);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double x = pos.getX() + 0.2D + rand.nextDouble() * 0.6D;
        double y = pos.getY() + rand.nextDouble();
        double z = pos.getZ() + 0.2D + rand.nextDouble() * 0.6D;
        double velocityY = stateIn.getValue(DRAG) ? -0.03D : 0.06D;
        worldIn.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y, z, 0.0D, velocityY, 0.0D);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
}
