package com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.sounds.ModSoundHandler;
import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterloggedBlock;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import net.minecraft.block.*;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class BigDripleaf extends Block implements IGrowable, RetroWaterloggedBlock {
    public static final String name = "Big_Dripleaf";
    public static final SoundType DRIPLEAF = new SoundType(1.0F, 1.0F, ModSoundHandler.BLOCK_BIG_DRIPLEAF_BREAK, ModSoundHandler.BLOCK_BIG_DRIPLEAF_STEP, ModSoundHandler.BLOCK_BIG_DRIPLEAF_PLACE, ModSoundHandler.BLOCK_BIG_DRIPLEAF_HIT, ModSoundHandler.BLOCK_BIG_DRIPLEAF_FALL);
    public static final int MAX_GROWTH_HEIGHT = 5;
    private static final int UNSTABLE_TILT_DELAY = 10;
    private static final int PARTIAL_TILT_DELAY = 10;
    private static final int FULL_TILT_DELAY = 100;

    private static final AxisAlignedBB NORMAL_AABB =
            new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.9375D, 1.0D);
    private static final AxisAlignedBB PARTIAL_AABB =
            new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.8125D, 1.0D);
    private static final AxisAlignedBB NORTH_STEM_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.5625D, 0.6875D, 0.8125D, 0.9375D);
    private static final AxisAlignedBB SOUTH_STEM_AABB =
            new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.6875D, 0.8125D, 0.4375D);
    private static final AxisAlignedBB EAST_STEM_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.3125D, 0.4375D, 0.8125D, 0.6875D);
    private static final AxisAlignedBB WEST_STEM_AABB =
            new AxisAlignedBB(0.5625D, 0.0D, 0.3125D, 0.9375D, 0.8125D, 0.6875D);

    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<BigDripleaf.EnumTilt> TILT = PropertyEnum.<BigDripleaf.EnumTilt>create("tilt", BigDripleaf.EnumTilt.class);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private final boolean waterloggedVariant;

    public BigDripleaf() {
        this(false);
    }

    public BigDripleaf(boolean waterloggedVariant) {
        super(waterloggedVariant ? Material.WATER : Material.VINE);
        this.waterloggedVariant = waterloggedVariant;

        setHardness(0.0F);

        String registryName = waterloggedVariant ? name + "_Waterlogged" : name;
        setRegistryName(registryName);
        setCreativeTab(waterloggedVariant ? null : CREATIVE_TABS);
        setTranslationKey(Tags.MOD_ID + "." + registryName.toLowerCase());
        setHardness(0.1F);
        setResistance(0.1F);
        setHarvestLevel("axe", 0);

        setSoundType(BigDripleaf.DRIPLEAF);

        this.setTickRandomly(false);

        ModBlocks.BLOCKS.add(this);
        if (!waterloggedVariant) {
            ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        }

        setDefaultState(RetroWaterlogging.withStillWaterLevel(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(TILT, EnumTilt.NONE)
                .withProperty(WATERLOGGED, waterloggedVariant)));
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumTilt tilt = state.getValue(TILT);
        AxisAlignedBB stem = getStemBox(state.getValue(FACING));
        if (tilt == EnumTilt.FULL) {
            return stem;
        }
        AxisAlignedBB leaf = tilt == EnumTilt.PARTIAL ? PARTIAL_AABB : NORMAL_AABB;
        return leaf.union(stem);
    }

    private static AxisAlignedBB getStemBox(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                return SOUTH_STEM_AABB;
            case EAST:
                return EAST_STEM_AABB;
            case WEST:
                return WEST_STEM_AABB;
            default:
                return NORTH_STEM_AABB;
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return RetroWaterlogging.extendedState(state, worldIn, pos);
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        EnumTilt tilt = blockState.getValue(TILT);
        return tilt == EnumTilt.FULL ? NULL_AABB
                : tilt == EnumTilt.PARTIAL ? PARTIAL_AABB : NORMAL_AABB;
    }

    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote || worldIn.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canBlockStay(worldIn, pos, state)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            restoreContainedFluidOrAir(worldIn, pos, state, 3);
            return;
        }
        com.canoestudio.retrofuturemccore.api.fluid.WaterloggedPlantFluid.updateTick(
                worldIn, pos, state);
        if (worldIn.isBlockPowered(pos)) {
            if (state.getValue(TILT) != EnumTilt.NONE) {
                resetTilt(worldIn, pos, state);
            }
            return;
        }

        EnumTilt tilt = state.getValue(TILT);
        if (tilt == EnumTilt.UNSTABLE) {
            setTiltAndScheduleTick(worldIn, pos, state, EnumTilt.PARTIAL, true);
        } else if (tilt == EnumTilt.PARTIAL) {
            setTiltAndScheduleTick(worldIn, pos, state, EnumTilt.FULL, true);
        } else if (tilt == EnumTilt.FULL) {
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

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) {
            return;
        }
        if (isBigDripleafBlock(worldIn.getBlockState(pos.up()).getBlock())) {
            RetroWaterlogging.setFluidloggableBlock(worldIn, pos,
                    ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
            return;
        }
        if (!canBlockStay(worldIn, pos, state)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            restoreContainedFluidOrAir(worldIn, pos, state, 3);
            return;
        }
        if (worldIn.isBlockPowered(pos) && state.getValue(TILT) != EnumTilt.NONE) {
            resetTilt(worldIn, pos, state);
        }
        RetroWaterlogging.onNeighborChanged(worldIn, pos, state);
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
        return DripleafPlacement.canPlaceHead(worldIn, pos);
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState below = worldIn.getBlockState(pos.down());
        return isBigDripleafBlock(below.getBlock()) || below.getBlock() == ModBlocks.DRIPLEAF_STEM
                || DripleafPlacement.isBigDripleafGround(below);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        RetroWaterlogging.onBlockAdded(worldIn, pos, state, WATERLOGGED);
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
        
        if (!isBigDripleafBlock(topState.getBlock()))
        {
            return false;
        }

        BlockPos aboveTop = topPos.up();
        return DripleafPlacement.canReplace(world, aboveTop);
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
        return isBigDripleafBlock(block) || block == ModBlocks.DRIPLEAF_STEM;
    }

    public static boolean isBigDripleafBlock(Block block)
    {
        return block == ModBlocks.BIG_DRIPLEAF || block == ModBlocks.BIG_DRIPLEAF_WATERLOGGED;
    }

    private void growWithBonemeal(World world, BlockPos pos, IBlockState state)
    {
        BlockPos topPos = findTopPosition(world, pos);
        IBlockState topState = world.getBlockState(topPos);
        EnumFacing facing = topState.getValue(FACING);
        DripleafPlacement.growHead(world, topPos, facing);
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
        EnumFacing plantFacing = placer.getHorizontalFacing().getOpposite();
        IBlockState below = worldIn.getBlockState(pos.down());
        if (isBigDripleafBlock(below.getBlock())) {
            plantFacing = below.getValue(FACING);
        } else if (below.getBlock() == ModBlocks.DRIPLEAF_STEM) {
            plantFacing = below.getValue(DripleafStem.FACING);
        }
        return getWaterloggedState(this.getDefaultState().withProperty(FACING, plantFacing),
                RetroWaterlogging.isWater(worldIn, pos));
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
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(TILT, EnumTilt.byMetadata((meta >> 2) & 3));
    }

    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(TILT).getMetadata() << 2);
    }

    protected BlockStateContainer createBlockState()
    {
        return RetroWaterlogging.createWaterMaterialStateContainer(this, FACING, TILT, WATERLOGGED);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return getWaterloggedMaterial(state, super.getMaterial(state));
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state != null && (state.getBlock() == ModBlocks.BIG_DRIPLEAF_WATERLOGGED
                || state.getPropertyKeys().contains(WATERLOGGED) && state.getValue(WATERLOGGED));
    }

    @Override
    public PropertyBool getWaterloggedProperty() {
        return WATERLOGGED;
    }

    @Override
    public IBlockState getWaterloggedState(IBlockState state, boolean waterlogged) {
        if (RetroWaterlogging.isFluidloggedAvailable()) {
            return state.withProperty(WATERLOGGED, waterlogged);
        }
        Block target = waterlogged ? ModBlocks.BIG_DRIPLEAF_WATERLOGGED : ModBlocks.BIG_DRIPLEAF;
        if (target == this) {
            return state.withProperty(WATERLOGGED, waterlogged);
        }
        return target.getDefaultState()
                .withProperty(FACING, state.getValue(FACING))
                .withProperty(TILT, state.getValue(TILT));
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
        growWithBonemeal(worldIn, pos, state);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) 
    { 
        return Item.getItemFromBlock(ModBlocks.BIG_DRIPLEAF);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(ModBlocks.BIG_DRIPLEAF);
    }

    @Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return isWaterlogged(worldIn.getBlockState(pos))
                ? Blocks.WATER.modifyAcceleration(worldIn, pos, entityIn, motion) : motion;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterlogged(state) && layer == BlockRenderLayer.TRANSLUCENT;
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
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
            EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess worldIn, BlockPos pos,
            EnumFacing side) {
        if (isWaterlogged(state)
                && worldIn.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, worldIn, pos, side);
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
