package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

public class BlockTurtleEgg extends Block {

    public static final PropertyInteger EGGS = PropertyInteger.create("eggs", 1, 4);
    public static final PropertyInteger HATCH = PropertyInteger.create("hatch", 0, 2);
    private static final AxisAlignedBB ONE_EGG_AABB =
        new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.4375D, 0.75D);
    private static final AxisAlignedBB MULTI_EGG_AABB =
        new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.4375D, 0.9375D);

    public BlockTurtleEgg() {
        super(Material.GOURD);
        this.setRegistryName(com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic.ID, "turtle_egg");
        this.setTranslationKey(com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic.ID + ".turtle_egg");
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
        this.setSoundType(SoundType.STONE);
        this.setHardness(0.5F);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(EGGS, 1).withProperty(HATCH, 0));
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (!worldIn.isRemote && canTrample(entityIn) && worldIn.rand.nextInt(entityIn instanceof EntityZombie ? 3 : 100) == 0) {
            breakOneEgg(worldIn, pos);
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!worldIn.isRemote && canTrample(entityIn) && worldIn.rand.nextInt(3) == 0) {
            breakOneEgg(worldIn, pos);
        }
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    private boolean canTrample(Entity entity) {
        return !(entity instanceof EntityTurtle) && !entity.isSneaking() && entity instanceof EntityLivingBase;
    }

    private void breakOneEgg(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return;
        }
        world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS,
            0.7F, 0.9F + world.rand.nextFloat() * 0.2F);
        if (world instanceof WorldServer) {
            ((WorldServer)world).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, 8,
                0.12D, 0.02D, 0.12D, 0.02D);
        }
        int eggs = state.getValue(EGGS);
        if (eggs <= 1) {
            world.setBlockToAir(pos);
        } else {
            world.setBlockState(pos, state.withProperty(EGGS, eggs - 1), 3);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!isOnSand(worldIn, pos)) {
            return;
        }
        int time = (int)(worldIn.getWorldTime() % 24000L);
        if ((time >= 21600 && time <= 22550) || rand.nextInt(500) == 0) {
            crackOrHatch(worldIn, pos, state);
        }
    }

    private void crackOrHatch(World world, BlockPos pos, IBlockState state) {
        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                0.7F, 0.9F + world.rand.nextFloat() * 0.2F);
            world.setBlockState(pos, state.withProperty(HATCH, hatch + 1), 3);
            return;
        }

        world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(state));
        int eggs = state.getValue(EGGS);
        for (int i = 0; i < eggs; i++) {
            EntityTurtle turtle = new EntityTurtle(world);
            turtle.setGrowingAge(-24000);
            turtle.setHomePos(pos);
            turtle.setLocationAndAngles(pos.getX() + 0.3D + world.rand.nextDouble() * 0.4D, pos.getY(),
                pos.getZ() + 0.3D + world.rand.nextDouble() * 0.4D, world.rand.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(turtle);
        }
        world.setBlockToAir(pos);
    }

    private boolean isOnSand(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() instanceof BlockSand;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(EGGS) == 1 ? ONE_EGG_AABB : MULTI_EGG_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(state, worldIn, pos);
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos,
            EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
            @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            spawnAsEntity(worldIn, pos, new ItemStack(this, state.getValue(EGGS), 0));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(EGGS, (meta & 3) + 1)
            .withProperty(HATCH, (meta >> 2) & 3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(EGGS) - 1) | (state.getValue(HATCH) << 2);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, EGGS, HATCH);
    }
}
