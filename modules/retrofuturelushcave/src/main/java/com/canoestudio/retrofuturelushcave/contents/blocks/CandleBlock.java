package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroFluidloggableBlock;
import com.canoestudio.retrofuturelushcavecore.api.fluid.RetroWaterlogging;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class CandleBlock extends Block implements RetroFluidloggableBlock {
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyInteger CANDLES = PropertyInteger.create("candles", 1, 4);
    public static final int LIGHT_PER_CANDLE = 3;
    private static final AxisAlignedBB[] AABBS = {
            new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 0.4375D, 0.5625D),
            new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.4375D, 0.5625D),
            new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.625D, 0.4375D, 0.6875D),
            new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.4375D, 0.625D)
    };
    private static final Vec3d[][] WICK_OFFSETS = {
            {new Vec3d(0.5D, 0.5D, 0.5D)},
            {new Vec3d(0.375D, 0.4375D, 0.5D), new Vec3d(0.625D, 0.5D, 0.4375D)},
            {new Vec3d(0.5D, 0.3125D, 0.625D), new Vec3d(0.375D, 0.4375D, 0.5D), new Vec3d(0.5625D, 0.5D, 0.4375D)},
            {new Vec3d(0.4375D, 0.3125D, 0.5625D), new Vec3d(0.625D, 0.4375D, 0.5625D), new Vec3d(0.375D, 0.4375D, 0.375D), new Vec3d(0.5625D, 0.5D, 0.375D)}
    };

    public CandleBlock(String name) {
        super(Material.CLOTH);
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setHardness(0.1F);
        setResistance(0.1F);
        setSoundType(SoundType.CLOTH);
        setCreativeTab(CREATIVE_TABS);
        setDefaultState(blockState.getBaseState().withProperty(CANDLES, 1).withProperty(LIT, false));

        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName(name.toLowerCase()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABBS[state.getValue(CANDLES) - 1];
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
    public int getLightValue(IBlockState state) {
        return state.getValue(LIT) ? state.getValue(CANDLES) * LIGHT_PER_CANDLE : 0;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState down = worldIn.getBlockState(pos.down());
        return down.getBlock().canPlaceTorchOnTop(down, worldIn, pos.down());
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canPlaceBlockAt(worldIn, pos)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            restoreFluidOrAir(worldIn, pos, state, 3);
            return;
        }
        scheduleContainedFluidTick(worldIn, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (!playerIn.isSneaking() && !stack.isEmpty() && stack.getItem() == net.minecraft.item.Item.getItemFromBlock(this) && state.getValue(CANDLES) < 4) {
            if (!worldIn.isRemote) {
                worldIn.setBlockState(pos, state.withProperty(CANDLES, state.getValue(CANDLES) + 1), 3);
                worldIn.playSound(null, pos, blockSoundType.getPlaceSound(), SoundCategory.BLOCKS, (blockSoundType.getVolume() + 1.0F) / 2.0F, blockSoundType.getPitch() * 0.8F);
                if (!playerIn.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            return true;
        }
        if (!playerIn.isSneaking() && stack.isEmpty() && state.getValue(LIT)) {
            if (!worldIn.isRemote) {
                extinguish(playerIn, worldIn, pos, state, 3);
            }
            return true;
        }
        if (canLight(worldIn, pos, state) && isLightingItem(stack)) {
            light(worldIn, pos, state, playerIn, stack, 3);
            return true;
        }
        return false;
    }

    public static void extinguish(World world, BlockPos pos, IBlockState state, int flags) {
        extinguish(null, world, pos, state, flags);
    }

    public static void extinguish(EntityPlayer player, World world, BlockPos pos, IBlockState state, int flags) {
        world.setBlockState(pos, state.withProperty(LIT, false), flags);
        for (Vec3d offset : WICK_OFFSETS[state.getValue(CANDLES) - 1]) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z, 0.0D, 0.1D, 0.0D);
        }
        world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.35F, 2.0F + world.rand.nextFloat() * 0.4F);
    }

    public static boolean isLit(IBlockState state) {
        return state.getBlock() instanceof CandleBlock && state.getValue(LIT);
    }

    public static boolean canLight(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() instanceof CandleBlock
                && !state.getValue(LIT)
                && !RetroWaterlogging.getFluidState(world, pos, state).isWater();
    }

    public static void light(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack, int flags) {
        if (world.isRemote || !canLight(world, pos, state)) {
            return;
        }

        world.setBlockState(pos, state.withProperty(LIT, true), flags);
        if (!stack.isEmpty()) {
            playLightSound(world, pos, stack);
        }
        consumeLightingItem(player, stack);
    }

    public static boolean isLightingItem(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE);
    }

    private static void playLightSound(World world, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == Items.FIRE_CHARGE) {
            world.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
        } else {
            world.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
        }
    }

    private static void consumeLightingItem(EntityPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty() || player.capabilities.isCreativeMode) {
            return;
        }

        if (stack.getItem() == Items.FLINT_AND_STEEL) {
            stack.damageItem(1, player);
        } else if (stack.getItem() == Items.FIRE_CHARGE) {
            stack.shrink(1);
        }
    }

    private void restoreFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, flags);
    }

    private void scheduleContainedFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroWaterlogging.scheduleContainedFluidTick(world, pos, state);
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
    public EnumActionResult onRetroFluidFill(World world, BlockPos pos, IBlockState here,
            RetroFluidState newFluidState, int blockFlags) {
        if (here.getValue(LIT)) {
            extinguish(null, world, pos, here, blockFlags);
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        IBlockState state = worldIn.getBlockState(pos);
        if (!worldIn.isRemote && entityIn.isBurning() && canLight(worldIn, pos, state)) {
            light(worldIn, pos, state, null, ItemStack.EMPTY, 3);
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(CANDLES, (meta & 3) + 1).withProperty(LIT, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CANDLES) - 1 + (state.getValue(LIT) ? 4 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {CANDLES, LIT});
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, java.util.Random random) {
        return state.getValue(CANDLES);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, java.util.Random rand) {
        if (!stateIn.getValue(LIT)) {
            return;
        }

        for (Vec3d offset : WICK_OFFSETS[stateIn.getValue(CANDLES) - 1]) {
            if (rand.nextFloat() < 0.3F) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z, 0.0D, 0.0D, 0.0D);
            }
            worldIn.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z, 0.0D, 0.0D, 0.0D);
        }
    }
}
