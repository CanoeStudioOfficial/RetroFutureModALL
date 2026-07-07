package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class CandleCakeBlock extends Block {
    public static final PropertyBool LIT = PropertyBool.create("lit");
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);
    private static final Map<Block, CandleCakeBlock> BY_CANDLE = new HashMap<>();

    private final CandleBlock candle;

    public CandleCakeBlock(String name, CandleBlock candle) {
        super(Material.CAKE);
        this.candle = candle;
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setHardness(0.5F);
        setResistance(0.5F);
        setSoundType(SoundType.CLOTH);
        setCreativeTab(CREATIVE_TABS);
        setDefaultState(blockState.getBaseState().withProperty(LIT, false));

        BY_CANDLE.put(candle, this);
        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName(name.toLowerCase()));
    }

    public static CandleCakeBlock byCandle(Block candle) {
        return BY_CANDLE.get(candle);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
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
        return state.getValue(LIT) ? CandleBlock.LIGHT_PER_CANDLE : 0;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canPlaceBlockAt(worldIn, pos)) {
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (canLight(state) && CandleBlock.isLightingItem(stack)) {
            light(worldIn, pos, state, playerIn, stack, 3);
            return true;
        }

        if (stack.isEmpty() && state.getValue(LIT) && candleHit(hitY)) {
            if (!worldIn.isRemote) {
                extinguish(playerIn, worldIn, pos, state, 3);
            }
            return true;
        }

        if (eat(worldIn, pos, playerIn)) {
            return true;
        }
        return stack.isEmpty();
    }

    private static boolean candleHit(float hitY) {
        return hitY > 0.5F;
    }

    public static boolean isLit(IBlockState state) {
        return state.getBlock() instanceof CandleCakeBlock && state.getValue(LIT);
    }

    public static boolean canLight(IBlockState state) {
        return state.getBlock() instanceof CandleCakeBlock && !state.getValue(LIT);
    }

    public static void light(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack, int flags) {
        if (world.isRemote || !canLight(state)) {
            return;
        }

        world.setBlockState(pos, state.withProperty(LIT, true), flags);
        if (!stack.isEmpty()) {
            if (stack.getItem() == Items.FIRE_CHARGE) {
                world.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
            } else {
                world.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
            }

            if (player != null && !player.capabilities.isCreativeMode) {
                if (stack.getItem() == Items.FLINT_AND_STEEL) {
                    stack.damageItem(1, player);
                } else if (stack.getItem() == Items.FIRE_CHARGE) {
                    stack.shrink(1);
                }
            }
        }
    }

    public static void extinguish(EntityPlayer player, World world, BlockPos pos, IBlockState state, int flags) {
        world.setBlockState(pos, state.withProperty(LIT, false), flags);
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0D, 0.1D, 0.0D);
        world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.35F, 2.0F + world.rand.nextFloat() * 0.4F);
    }

    private boolean eat(World world, BlockPos pos, EntityPlayer player) {
        if (!player.canEat(false)) {
            return false;
        }

        if (!world.isRemote) {
            player.addStat(StatList.CAKE_SLICES_EATEN);
            player.getFoodStats().addStats(2, 0.1F);
            Block.spawnAsEntity(world, pos, new ItemStack(candle));
            world.setBlockState(pos, Blocks.CAKE.getDefaultState().withProperty(net.minecraft.block.BlockCake.BITES, 1), 3);
        }
        return true;
    }

    @Override
    public int quantityDropped(java.util.Random random) {
        return 0;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        IBlockState state = worldIn.getBlockState(pos);
        if (!worldIn.isRemote && entityIn.isBurning() && canLight(state)) {
            light(worldIn, pos, state, null, ItemStack.EMPTY, 3);
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public net.minecraft.item.Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(Items.CAKE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, java.util.Random rand) {
        if (stateIn.getValue(LIT)) {
            if (rand.nextFloat() < 0.3F) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            }
            worldIn.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {LIT});
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
