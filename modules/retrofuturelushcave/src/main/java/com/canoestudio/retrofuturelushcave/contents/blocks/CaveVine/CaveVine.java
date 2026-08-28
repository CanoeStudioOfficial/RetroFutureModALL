package com.canoestudio.retrofuturelushcave.contents.blocks.CaveVine;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class CaveVine extends CaveVinePlant {

    /** 1.18 GrowingPlantHeadBlock.AGE_25。世界生成使用23..25，25为停止自然生长的成熟头部。 */
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 25);
    private static final int MAX_AGE = 25;
    private static final float GROWTH_CHANCE = 0.10F;
    private static final float BERRY_CHANCE_ON_GROWTH = 0.11F;

    public CaveVine(String name) {
        super(name);
        this.setTickRandomly(true);

        this.setDefaultState(this.getDefaultState().withProperty(BERRIES, false).withProperty(AGE, 0));
    }

    /**
     * 对应1.18 GrowingPlantHeadBlock / CaveVinesBlock：年龄未满25时以0.1概率向下延伸，
     * 旧头部转为body，新头部年龄+1，浆果概率0.11。
     *
     * <p>1.12区块metadata只有4 bit，因此随机刻只在可无损持久化的兼容阶梯
     * 0 -> 1 -> 23 -> 24 -> 25 之间推进；世界生成写入的23..25与成熟停止语义完整保留。</p>
     */
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        if (!worldIn.isAreaLoaded(pos, 1)) return;

        int age = getAge(state);
        BlockPos below = pos.down();
        IBlockState belowState = worldIn.getBlockState(below);
        boolean mayGrow = age < MAX_AGE && belowState.getBlock().isAir(belowState, worldIn, below);
        if (!mayGrow || !net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, below, belowState,
                rand.nextFloat() < GROWTH_CHANCE)) return;

        worldIn.setBlockState(pos, ModBlocks.CAVE_VINE_PLANT.getDefaultState()
                .withProperty(CaveVinePlant.BERRIES, state.getValue(BERRIES)), 2);
        int nextAge = nextPersistableAge(age);
        worldIn.setBlockState(below, withAge(nextAge)
                .withProperty(BERRIES, Boolean.valueOf(rand.nextFloat() < BERRY_CHANCE_ON_GROWTH)), 2);
        net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, below, state, worldIn.getBlockState(below));
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, this.getDefaultState().withProperty(BERRIES, true).withProperty(AGE, state.getValue(AGE)), 2);
    }

    protected int getAge(IBlockState state)
    {
        return state.getValue(AGE);
    }

    public IBlockState withAge(int age)
    {
        return this.getDefaultState().withProperty(AGE, age);
    }

    /** 将旧版0/1生长开关映射到可持久化的1.18式成熟阶梯。 */
    private static int nextPersistableAge(int age) {
        if (age <= 0) return 1;
        if (age == 1) return 23;
        if (age == 23) return 24;
        return MAX_AGE;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {

        /* 原版CaveVinesBlock交互只处理浆果；不再把剪刀作为年龄重置开关。 */
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);

    }


    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {
        switch (meta & 15) {
            /* 保持旧存档0..3的精确含义。 */
            case 1: return state(false, 1);
            case 2: return state(true, 0);
            case 3: return state(true, 1);
            /* 新增：保存世界生成实际使用的23..25及其浆果状态。 */
            case 4: return state(false, 23);
            case 5: return state(false, 24);
            case 6: return state(false, 25);
            case 7: return state(true, 23);
            case 8: return state(true, 24);
            case 9: return state(true, 25);
            default: return state(false, 0);
        }
    }

    /**
     * 1.12 metadata为4 bit。编码保留旧0..3，同时无损保存生成器与随机刻使用的23..25状态；
     * 理论上的2..22不在兼容生长阶梯中出现，若由外部代码写入则规范化为25。
     */
    public int getMetaFromState(IBlockState state) {
        int age = state.getValue(AGE);
        boolean berries = state.getValue(BERRIES);
        if (age <= 0) return berries ? 2 : 0;
        if (age == 1) return berries ? 3 : 1;
        if (age == 23) return berries ? 7 : 4;
        if (age == 24) return berries ? 8 : 5;
        return berries ? 9 : 6;
    }

    private IBlockState state(boolean berries, int age) {
        return this.getDefaultState().withProperty(BERRIES, berries).withProperty(AGE, age);
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {BERRIES, AGE});
    }
}
