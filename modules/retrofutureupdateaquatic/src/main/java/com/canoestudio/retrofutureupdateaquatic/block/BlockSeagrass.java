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
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSeagrass extends BlockBush implements IGrowable {

    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 2);
    private static final AxisAlignedBB SEAGRASS_AABB =
        new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.75D, 0.875D);
    private static final AxisAlignedBB TALL_SEAGRASS_AABB =
        new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

    public BlockSeagrass() {
        super(Material.WATER);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "seagrass");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".seagrass");
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
        this.setDefaultState(RetroWaterlogging.withStillWaterLevel(this.blockState.getBaseState()
            .withProperty(TYPE, 0)));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(TYPE) == 0 ? SEAGRASS_AABB : TALL_SEAGRASS_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return AquaticWaterHelper.isWater(worldIn, pos)
            && AquaticWaterHelper.isSolidTop(worldIn, pos.down());
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (!AquaticWaterHelper.isWater(worldIn, pos) && worldIn.getBlockState(pos).getBlock() != this) {
            return false;
        }
        int type = state.getValue(TYPE);
        if (type == 0) {
            return AquaticWaterHelper.isSolidTop(worldIn, pos.down());
        }
        if (type == 1) {
            return AquaticWaterHelper.isSolidTop(worldIn, pos.down())
                && worldIn.getBlockState(pos.up()).getBlock() == this;
        }
        return worldIn.getBlockState(pos.down()).getBlock() == this
            && worldIn.getBlockState(pos.down()).getValue(TYPE) == 1;
    }

    public void placeTallAt(World worldIn, BlockPos lowerPos, int flags) {
        worldIn.setBlockState(lowerPos, this.getDefaultState().withProperty(TYPE, 1), flags);
        worldIn.setBlockState(lowerPos.up(), this.getDefaultState().withProperty(TYPE, 2), flags);
    }

    @Override
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            int type = state.getValue(TYPE);
            if (type == 1 && worldIn.getBlockState(pos.up()).getBlock() == this) {
                AquaticWaterHelper.restoreWater(worldIn, pos.up());
            }
            if (type == 2 && worldIn.getBlockState(pos.down()).getBlock() == this) {
                AquaticWaterHelper.restoreWater(worldIn, pos.down());
            }
            AquaticWaterHelper.restoreWater(worldIn, pos);
        }
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(TYPE) == 1 && worldIn.getBlockState(pos.up()).getBlock() == this) {
            AquaticWaterHelper.restoreWater(worldIn, pos.up());
        }
        if (state.getValue(TYPE) == 2 && worldIn.getBlockState(pos.down()).getBlock() == this) {
            AquaticWaterHelper.restoreWater(worldIn, pos.down());
        }
        AquaticWaterHelper.restoreWater(worldIn, pos);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
            TileEntity te, ItemStack stack) {
        if (!worldIn.isRemote && stack.getItem() instanceof ItemShears && state.getValue(TYPE) != 2) {
            spawnAsEntity(worldIn, pos, new ItemStack(this, state.getValue(TYPE) == 0 ? 1 : 2));
        } else {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(TYPE) == 0
            && AquaticWaterHelper.isWater(worldIn, pos.up())
            && AquaticWaterHelper.isSolidTop(worldIn, pos.down());
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (this.canGrow(worldIn, pos, state, false)) {
            this.placeTallAt(worldIn, pos, 3);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, Math.max(0, Math.min(2, meta)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, TYPE);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos,
            EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
