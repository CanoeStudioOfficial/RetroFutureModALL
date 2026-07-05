package com.canoestudio.retrofuturemc.contents.blocks;

import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import com.canoestudio.retrofuturemc.utils.LightningRodData;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import static com.canoestudio.retrofuturemc.contents.tab.CreativeTab.CREATIVE_TABS;

public class LightningRodBlock extends FluidloggableDirectionalBlock {
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    private static final int ACTIVATION_TICKS = 8;
    public static final int RANGE = 128;
    private static final int SPARK_CYCLE = 200;

    public LightningRodBlock(String name) {
        super(Material.IRON);
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 0);
        setSoundType(SoundType.METAL);
        setCreativeTab(CREATIVE_TABS);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(POWERED, false));

        ModBlocks.BLOCKS.add(this);
        ModBlocks.BLOCKITEMS.add(new ItemBlock(this).setRegistryName(name.toLowerCase()));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing.getAxis()) {
            case X:
                return new AxisAlignedBB(0.0D, 0.375D, 0.375D, 1.0D, 0.625D, 0.625D);
            case Z:
                return new AxisAlignedBB(0.375D, 0.375D, 0.0D, 0.625D, 0.625D, 1.0D);
            case Y:
            default:
                return new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);
        }
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
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(POWERED) && blockState.getValue(FACING) == side ? 15 : 0;
    }

    public void onLightningStrike(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }

        if (!state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(POWERED, true), 3);
        }

        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() == this) {
            updateNeighbours(current, world, pos);
            world.scheduleUpdate(pos, this, ACTIVATION_TICKS);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote || !state.getValue(POWERED)) {
            return;
        }

        worldIn.setBlockState(pos, state.withProperty(POWERED, false), 3);
        updateNeighbours(state, worldIn, pos);
    }

    @Override
    public int tickRate(World worldIn) {
        return ACTIVATION_TICKS;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn instanceof WorldServer) {
            LightningRodData.get((WorldServer) worldIn).add(pos);
            if (state.getValue(POWERED) && !worldIn.isUpdateScheduled(pos, this)) {
                worldIn.scheduleUpdate(pos, this, ACTIVATION_TICKS);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn instanceof WorldServer) {
            LightningRodData.get((WorldServer) worldIn).remove(pos);
            if (state.getValue(POWERED)) {
                updateNeighbours(state, worldIn, pos);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!worldIn.isThundering() || !worldIn.canBlockSeeSky(pos)) {
            return;
        }

        if ((long) rand.nextInt(SPARK_CYCLE) > worldIn.getTotalWorldTime() % SPARK_CYCLE) {
            return;
        }

        spawnSparkParticles(stateIn.getValue(FACING), worldIn, pos, rand);
    }

    private void updateNeighbours(IBlockState state, World world, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        world.notifyNeighborsOfStateChange(pos, this, false);
        world.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);
    }

    @SideOnly(Side.CLIENT)
    private void spawnSparkParticles(EnumFacing facing, World world, BlockPos pos, Random rand) {
        int count = 1 + rand.nextInt(2);
        EnumFacing.Axis axis = facing.getAxis();

        for (int i = 0; i < count; ++i) {
            double along = 0.2D + rand.nextDouble() * 0.6D;
            double x = pos.getX() + 0.5D + (axis == EnumFacing.Axis.X ? along - 0.5D : (rand.nextDouble() - 0.5D) * 0.18D);
            double y = pos.getY() + 0.5D + (axis == EnumFacing.Axis.Y ? along - 0.5D : (rand.nextDouble() - 0.5D) * 0.18D);
            double z = pos.getZ() + 0.5D + (axis == EnumFacing.Axis.Z ? along - 0.5D : (rand.nextDouble() - 0.5D) * 0.18D);

            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0.0D, 0.02D, 0.0D);
            if (rand.nextBoolean()) {
                world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, x, y, z, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7)).withProperty(POWERED, (meta & 8) == 8);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(POWERED) ? 8 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {FACING, POWERED});
    }
}
