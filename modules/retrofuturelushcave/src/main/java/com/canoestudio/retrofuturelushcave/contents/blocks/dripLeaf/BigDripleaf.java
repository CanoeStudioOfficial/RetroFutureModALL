package com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.sounds.ModSoundHandler;
import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidloggableBlock;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class BigDripleaf extends BlockBush implements IGrowable, RetroFluidloggableBlock {
    public static final String name = "Big_Dripleaf";
    public static final SoundType DRIPLEAF = new SoundType(1.0F, 1.0F, ModSoundHandler.BLOCK_BIG_DRIPLEAF_BREAK, ModSoundHandler.BLOCK_BIG_DRIPLEAF_STEP, ModSoundHandler.BLOCK_BIG_DRIPLEAF_PLACE, ModSoundHandler.BLOCK_BIG_DRIPLEAF_HIT, ModSoundHandler.BLOCK_BIG_DRIPLEAF_FALL);
    public static final int MAX_GROWTH_HEIGHT = 5;
    private static final int UNSTABLE_TILT_DELAY = 10;
    private static final int PARTIAL_TILT_DELAY = 10;
    private static final int FULL_TILT_DELAY = 100;

    protected static final AxisAlignedBB HALF_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB DRIP_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);

    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<BigDripleaf.EnumTilt> TILT = PropertyEnum.<BigDripleaf.EnumTilt>create("tilt", BigDripleaf.EnumTilt.class);

    public BigDripleaf() {
        super(Material.VINE);

        setHardness(0.0F);

        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name);
        setCreativeTab(CREATIVE_TABS);
        setHardness(0.1F);
        setResistance(0.1F);
        setHarvestLevel("axe", 0);

        setSoundType(BigDripleaf.DRIPLEAF);

        this.setTickRandomly(true);

        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));

        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH).withProperty(TILT, EnumTilt.NONE));
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return FULL_BLOCK_AABB; }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        EnumTilt tilt = blockState.getValue(TILT);

        if(tilt == EnumTilt.FULL)
            return NULL_AABB;
        else if(tilt == EnumTilt.PARTIAL)
            return HALF_AABB;

        return DRIP_AABB;
    }

    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.isBlockPowered(pos))
        {
            resetTilt(worldIn, pos, state);
            return;
        }

        EnumTilt tilt = state.getValue(TILT);
        
        if (tilt == EnumTilt.UNSTABLE)
        {
            setTiltAndScheduleTick(worldIn, pos, state, EnumTilt.PARTIAL, true);
        }
        else if (tilt == EnumTilt.PARTIAL)
        {
            setTiltAndScheduleTick(worldIn, pos, state, EnumTilt.FULL, true);
        }
        else if (tilt == EnumTilt.FULL)
        {
            resetTilt(worldIn, pos, state);
        }
    }

    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        if (worldIn.isRemote) return;
        
        EnumTilt tilt = state.getValue(TILT);

        if (tilt == EnumTilt.NONE && canEntityTilt(pos, entityIn) && !worldIn.isBlockPowered(pos))
        {
            setTiltAndScheduleTick(worldIn, pos, state, EnumTilt.UNSTABLE, false);
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!canBlockStay(worldIn, pos, state))
        {
            if (!worldIn.isRemote)
            {
                dropBlockAsItem(worldIn, pos, state, 0);
                restoreContainedFluidOrAir(worldIn, pos, state, 3);
            }
            return;
        }

        if (!worldIn.isRemote)
        {
            if (worldIn.isBlockPowered(pos))
            {
                resetTilt(worldIn, pos, state);
            }
        }
        scheduleContainedFluidTick(worldIn, pos, state);
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    private boolean canEntityTilt(BlockPos pos, Entity entity)
    {
        return entity.onGround && entity.posY > (double)((float)pos.getY() + 0.6875F);
    }

    private void setTiltAndScheduleTick(World world, BlockPos pos, IBlockState state, EnumTilt tilt, boolean playSound)
    {
        if (state.getValue(TILT) == tilt)
        {
            return;
        }

        world.setBlockState(pos, state.withProperty(TILT, tilt), 2);
        if (playSound)
        {
            playTiltSound(world, pos);
        }

        int delay = getTiltDelay(tilt);
        if (delay > 0)
        {
            world.scheduleUpdate(pos, this, delay);
        }
    }

    private void resetTilt(World world, BlockPos pos, IBlockState state)
    {
        if (state.getValue(TILT) == EnumTilt.NONE)
        {
            return;
        }

        world.setBlockState(pos, state.withProperty(TILT, EnumTilt.NONE), 2);
        playTiltSound(world, pos);
    }

    private int getTiltDelay(EnumTilt tilt)
    {
        if (tilt == EnumTilt.UNSTABLE)
        {
            return UNSTABLE_TILT_DELAY;
        }
        if (tilt == EnumTilt.PARTIAL)
        {
            return PARTIAL_TILT_DELAY;
        }
        if (tilt == EnumTilt.FULL)
        {
            return FULL_TILT_DELAY;
        }
        return -1;
    }

    private void playTiltSound(World world, BlockPos pos)
    {
        world.playSound(null, pos, ModSoundHandler.BLOCK_BIG_DRIPLEAF_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        Block downBlock = downState.getBlock();
        
        if (downBlock == ModBlocks.DRIPLEAF_STEM)
        {
            return true;
        }
        
        if (downBlock == ModBlocks.BIG_DRIPLEAF && isTopDripleaf(worldIn, pos.down()))
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

    private boolean isTopDripleaf(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != ModBlocks.BIG_DRIPLEAF)
        {
            return false;
        }
        
        IBlockState upState = world.getBlockState(pos.up());
        return upState.getBlock() != ModBlocks.BIG_DRIPLEAF && upState.getBlock() != ModBlocks.DRIPLEAF_STEM;
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

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        
        IBlockState downState = worldIn.getBlockState(pos.down());
        if (downState.getBlock() == ModBlocks.BIG_DRIPLEAF)
        {
            EnumFacing facing = state.getValue(FACING);
            setFluidloggableBlock(worldIn, pos.down(), ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(FACING, facing), 2);
        }
    }

    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) { return false; }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        
        if (heldItem.getItem() == Items.DYE && heldItem.getMetadata() == 15)
        {
            if (canGrowWithBonemeal(worldIn, pos))
            {
                if (!worldIn.isRemote)
                {
                    growWithBonemeal(worldIn, pos, state);
                    if (!playerIn.capabilities.isCreativeMode)
                    {
                        heldItem.shrink(1);
                    }
                }
                return true;
            }
        }
        
        return false;
    }

    public static boolean canGrowWithBonemeal(World world, BlockPos pos)
    {
        BlockPos topPos = findTopPosition(world, pos);
        IBlockState topState = world.getBlockState(topPos);
        
        if (topState.getBlock() != ModBlocks.BIG_DRIPLEAF)
        {
            return false;
        }

        if (getPlantHeight(world, pos) >= MAX_GROWTH_HEIGHT)
        {
            return false;
        }
        
        BlockPos aboveTop = topPos.up();
        return canGrowInto(world, aboveTop);
    }

    public static BlockPos findTopPosition(World world, BlockPos pos)
    {
        BlockPos checkPos = pos;
        while (true)
        {
            IBlockState upState = world.getBlockState(checkPos.up());
            if (isBigDripleafPart(upState.getBlock()))
            {
                checkPos = checkPos.up();
            }
            else
            {
                break;
            }
        }
        return checkPos;
    }

    public static BlockPos findBottomPosition(World world, BlockPos pos)
    {
        BlockPos checkPos = pos;
        while (true)
        {
            IBlockState downState = world.getBlockState(checkPos.down());
            if (isBigDripleafPart(downState.getBlock()))
            {
                checkPos = checkPos.down();
            }
            else
            {
                break;
            }
        }
        return checkPos;
    }

    public static int getPlantHeight(World world, BlockPos pos)
    {
        BlockPos bottomPos = findBottomPosition(world, pos);
        BlockPos topPos = findTopPosition(world, pos);
        return topPos.getY() - bottomPos.getY() + 1;
    }

    public static boolean isBigDripleafPart(Block block)
    {
        return block == ModBlocks.BIG_DRIPLEAF || block == ModBlocks.DRIPLEAF_STEM;
    }

    private void growWithBonemeal(World world, BlockPos pos, IBlockState state)
    {
        BlockPos topPos = findTopPosition(world, pos);
        IBlockState topState = world.getBlockState(topPos);
        EnumFacing facing = topState.getValue(FACING);
        
        setFluidloggableBlock(world, topPos, ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(FACING, facing), 2);
        setFluidloggableBlock(world, topPos.up(), this.getDefaultState().withProperty(FACING, facing), 3);
    }

    public static boolean canGrowInto(World world, BlockPos pos)
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

    private void scheduleContainedFluidTick(World world, BlockPos pos, IBlockState state)
    {
        RetroWaterlogging.scheduleContainedFluidTick(world, pos, state);
    }

    private void restoreContainedFluidOrAir(World world, BlockPos pos, IBlockState state, int flags)
    {
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, flags);
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
        int i = meta / 4;
        IBlockState state = this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(i));

        int j = meta % 4;

        return state.withProperty(TILT, EnumTilt.byMetadata(j));
    }

    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TILT).getMetadata() + state.getValue(FACING).getHorizontalIndex() * 4;
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING, TILT});
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) { 
        return canGrowWithBonemeal(worldIn, pos); 
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) { 
        return canGrowWithBonemeal(worldIn, pos); 
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state)
    {
        if (canGrowWithBonemeal(worldIn, pos))
        {
            growWithBonemeal(worldIn, pos, state);
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) 
    { 
        return Item.getItemFromBlock(this); 
    }

    public static enum EnumTilt implements IStringSerializable
    {
        NONE(0, "none"),
        PARTIAL(1, "partial"),
        FULL(2, "full"),
        UNSTABLE(3, "unstable");

        private static final BigDripleaf.EnumTilt[] META_LOOKUP = new BigDripleaf.EnumTilt[values().length];
        private final int meta;
        private final String name;

        private EnumTilt(int metaIn, String nameIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        public String toString()
        {
            return this.name;
        }

        public static BigDripleaf.EnumTilt byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName()
        {
            return this.name;
        }

        static
        {
            for (BigDripleaf.EnumTilt bigdripleaf$enumtilt : values())
            {
                META_LOOKUP[bigdripleaf$enumtilt.getMetadata()] = bigdripleaf$enumtilt;
            }
        }
    }

}
