package com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf;


import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroFluidloggableBlock;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroWaterlogging;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class DripleafStem extends BlockBush implements IGrowable, RetroFluidloggableBlock
{
    public static final String name = "Big_Dripleaf_Stem";
    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;

    public DripleafStem()
    {
        super(Material.VINE);

        setHardness(0.0F);

        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name);
        setCreativeTab(CREATIVE_TABS);
        setSoundType(BigDripleaf.DRIPLEAF);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH));
        this.setTickRandomly(true);

        ModBlocks.BLOCKS.add(this);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return FULL_BLOCK_AABB; }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            if (!this.canBlockStay(worldIn, pos, state))
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                restoreContainedFluidOrAir(worldIn, pos, state, 3);
            }
            else if (!hasDripleafAbove(worldIn, pos))
            {
                setFluidloggableBlock(worldIn, pos, ModBlocks.BIG_DRIPLEAF.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
            }
        }
    }

    private boolean hasDripleafAbove(World world, BlockPos pos)
    {
        IBlockState upState = world.getBlockState(pos.up());
        return upState.getBlock() == ModBlocks.DRIPLEAF_STEM || upState.getBlock() == ModBlocks.BIG_DRIPLEAF;
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        scheduleContainedFluidTick(worldIn, pos, state);
        if (!worldIn.isRemote)
        {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        Block downBlock = downState.getBlock();
        
        if (downBlock == ModBlocks.DRIPLEAF_STEM)
        {
            return true;
        }
        
        if (downBlock == ModBlocks.BIG_DRIPLEAF)
        {
            return true;
        }

        if (canSustainDripleaf(downBlock))
        {
            return true;
        }
        
        if (downBlock.canSustainPlant(downState, worldIn, pos.down(), EnumFacing.UP, this))
        {
            return true;
        }
        
        return false;
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        Block downBlock = downState.getBlock();

        if (downBlock == ModBlocks.DRIPLEAF_STEM)
        {
            return true;
        }
        
        if (downBlock == ModBlocks.BIG_DRIPLEAF)
        {
            return true;
        }

        if (canSustainDripleaf(downBlock))
        {
            return true;
        }
        
        return downBlock.canSustainPlant(downState, worldIn, pos.down(), EnumFacing.UP, this);
    }

    private boolean canSustainDripleaf(Block block)
    {
        return block == Blocks.CLAY || block == ModBlocks.MOSS_BLOCK || block == ModBlocks.ROOTED_DIRT || block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.MYCELIUM || block == Blocks.FARMLAND;
    }

    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) { return false; }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) { return Item.getItemFromBlock(ModBlocks.BIG_DRIPLEAF); }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) { return new ItemStack(ModBlocks.BIG_DRIPLEAF); }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) { return BigDripleaf.canGrowWithBonemeal(worldIn, pos); }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) { return BigDripleaf.canGrowWithBonemeal(worldIn, pos); }

    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state)
    {
        if (!BigDripleaf.canGrowWithBonemeal(worldIn, pos))
        {
            return;
        }

        BlockPos topPos = BigDripleaf.findTopPosition(worldIn, pos);
        IBlockState topState = worldIn.getBlockState(topPos);
        EnumFacing facing = topState.getValue(FACING);
        
        BlockPos aboveTop = topPos.up();
        
        if (canGrowInto(worldIn, aboveTop))
        {
            if (topState.getBlock() == ModBlocks.BIG_DRIPLEAF)
            {
                setFluidloggableBlock(worldIn, topPos, this.getDefaultState().withProperty(FACING, facing), 2);
            }
            setFluidloggableBlock(worldIn, aboveTop, ModBlocks.BIG_DRIPLEAF.getDefaultState().withProperty(FACING, facing), 3);
        }
    }

    private boolean canGrowInto(World world, BlockPos pos)
    {
        return RetroWaterlogging.canPlaceIntoAirOrWater(world, pos);
    }

    private void setFluidloggableBlock(World world, BlockPos pos, IBlockState newState, int flags)
    {
        RetroWaterlogging.setFluidloggableBlock(world, pos, newState, flags);
    }

    private boolean hasWaterFluid(World world, BlockPos pos)
    {
        return RetroWaterlogging.hasWaterFluid(world, pos);
    }

    private RetroFluidState getWaterFluidState(World world, BlockPos pos)
    {
        return RetroWaterlogging.getWaterFluidState(world, pos);
    }

    private void restoreContainedFluidOrAir(World world, BlockPos pos, IBlockState state, int flags)
    {
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, flags);
    }

    private void scheduleContainedFluidTick(World world, BlockPos pos, IBlockState state)
    {
        RetroWaterlogging.scheduleContainedFluidTick(world, pos, state);
    }

    private void scheduleFluidTick(World world, BlockPos pos, RetroFluidState fluidState)
    {
        RetroWaterlogging.scheduleFluidTick(world, pos, fluidState);
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }
}
