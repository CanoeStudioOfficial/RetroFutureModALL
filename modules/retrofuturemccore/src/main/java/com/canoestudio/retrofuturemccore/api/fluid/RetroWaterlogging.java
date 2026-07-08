package com.canoestudio.retrofuturemccore.api.fluid;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class RetroWaterlogging {

    private RetroWaterlogging() {
    }

    public static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return RetroFluidCompat.getFluidState(world, pos);
    }

    public static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        return RetroFluidCompat.getFluidState(world, pos, state);
    }

    public static boolean isWater(IBlockAccess world, BlockPos pos) {
        return RetroFluidCompat.isWater(world, pos);
    }

    public static boolean isWater(RetroFluidState fluidState) {
        return RetroFluidCompat.isWater(fluidState);
    }

    public static boolean isVanillaWater(IBlockState state) {
        return RetroFluidCompat.isVanillaWater(state);
    }

    public static boolean isWaterBlock(IBlockState state) {
        return RetroFluidCompat.isWaterBlock(state);
    }

    public static PropertyInteger stillWaterLevelProperty() {
        return BlockLiquid.LEVEL;
    }

    public static IBlockState withStillWaterLevel(IBlockState state) {
        return state.withProperty(BlockLiquid.LEVEL, 0);
    }

    public static boolean hasStillWaterLevel(IBlockState state) {
        return state.getProperties().containsKey(BlockLiquid.LEVEL);
    }

    public static BlockStateContainer createWaterMaterialStateContainer(Block block, IProperty<?>... properties) {
        return new BlockStateContainer(block, appendStillWaterLevel(properties));
    }

    public static Material materialForWaterlogged(IBlockState state, Material fallback, PropertyBool property) {
        // 1.12 reads BlockLiquid.LEVEL from every state that reports Material.WATER.
        // Only expose a water material when the compatibility state can satisfy that vanilla assumption.
        return state.getValue(property) && !RetroFluidCompat.isFluidloggedAvailable() && hasStillWaterLevel(state)
            ? Material.WATER : fallback;
    }

    private static IProperty<?>[] appendStillWaterLevel(IProperty<?>... properties) {
        if (properties == null || properties.length == 0) {
            return new IProperty<?>[] {BlockLiquid.LEVEL};
        }
        for (IProperty<?> property : properties) {
            if (property == BlockLiquid.LEVEL) {
                return properties.clone();
            }
        }
        IProperty<?>[] appended = Arrays.copyOf(properties, properties.length + 1);
        appended[properties.length] = BlockLiquid.LEVEL;
        return appended;
    }

    public static boolean canReplaceWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return isWater(world, pos) || state.getBlock().isReplaceable(world, pos);
    }

    public static boolean canPlaceIntoAirOrWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceable(world, pos) || state.getBlock() == Blocks.AIR || isWater(world, pos);
    }

    public static IBlockState stateForPlacement(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return state.withProperty(property, isWater(world, pos));
    }

    public static IBlockState actualState(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return RetroFluidCompat.withActualWaterlogged(state, world, pos, property);
    }

    public static boolean isWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return RetroFluidCompat.isWaterlogged(state, world, pos, property);
    }

    public static void onBlockAdded(World world, BlockPos pos, IBlockState state, PropertyBool property) {
        RetroFluidCompat.ensureWaterlogged(world, pos, state, property, 3);
    }

    public static void updateWaterloggedProperty(World world, BlockPos pos, IBlockState state,
            PropertyBool property, int flags) {
        RetroFluidCompat.setWaterloggedProperty(world, pos, state, property,
            isWater(world, pos), flags);
    }

    public static void restoreWater(World world, BlockPos pos) {
        restoreWater(world, pos, world.getBlockState(pos), 3);
    }

    public static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        restoreWater(world, pos, replacedState, 3);
    }

    public static void restoreWater(World world, BlockPos pos, IBlockState replacedState, int flags) {
        RetroFluidCompat.restoreWater(world, pos, replacedState, flags);
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, IBlockState replacedState, int flags) {
        if (world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }

        RetroFluidState fluidState = getFluidState(world, pos, replacedState);
        world.setBlockState(pos, fluidState.isWater() ? fluidState.getState() : Blocks.AIR.getDefaultState(), flags);
        scheduleFluidTick(world, pos, fluidState);
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, RetroFluidState fluidState, int flags) {
        if (world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }

        world.setBlockState(pos, fluidState != null && fluidState.isWater()
            ? fluidState.getState() : Blocks.AIR.getDefaultState(), flags);
        scheduleFluidTick(world, pos, fluidState);
    }

    public static void restoreContainedFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        restoreFluidOrAir(world, pos, getFluidState(world, pos, state), flags);
    }

    public static void setFluidloggableBlock(World world, BlockPos pos, IBlockState newState, int flags) {
        RetroFluidState fluidState = getWaterFluidState(world, pos);
        world.setBlockState(pos, newState, flags);
        if (fluidState.isWater()) {
            setFluidState(world, pos, world.getBlockState(pos), fluidState, flags);
            scheduleFluidTick(world, pos, fluidState);
        }
    }

    public static boolean setFluidState(World world, BlockPos pos, IBlockState here,
            RetroFluidState fluidState, int flags) {
        return RetroFluidCompat.setFluidState(world, pos, here, fluidState, flags);
    }

    public static boolean hasWaterFluid(World world, BlockPos pos) {
        return getWaterFluidState(world, pos).isWater();
    }

    public static RetroFluidState getWaterFluidState(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return isWaterBlock(state) ? RetroFluidState.ofWater(state) : getFluidState(world, pos, state);
    }

    public static void onPlayerDestroy(World world, BlockPos pos, IBlockState state, PropertyBool property) {
        if (isWaterlogged(state, world, pos, property)) {
            restoreWater(world, pos, state);
        }
    }

    public static void afterRemovedByPlayer(boolean removed, World world, BlockPos pos, IBlockState removedState,
            boolean wasWaterlogged) {
        if (removed && !world.isRemote && wasWaterlogged) {
            restoreWater(world, pos, removedState);
        }
    }

    public static void breakUnsupportedOrRestore(World world, BlockPos pos, IBlockState state,
            PropertyBool property, Block selfBlock, boolean dropItem) {
        if (dropItem) {
            selfBlock.dropBlockAsItem(world, pos, state, 0);
        }
        if (isWaterlogged(state, world, pos, property)) {
            restoreWater(world, pos, state);
        } else {
            world.setBlockToAir(pos);
        }
    }

    public static boolean hasWaterOrAdjacent(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        if (isWaterlogged(state, world, pos, property)) {
            return true;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (isWater(world, pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    public static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroFluidCompat.scheduleFluidTick(world, pos, state);
    }

    public static void scheduleFluidTick(World world, BlockPos pos, RetroFluidState fluidState) {
        if (fluidState != null && fluidState.isWater()) {
            world.scheduleUpdate(pos, fluidState.getState().getBlock(), fluidState.getState().getBlock().tickRate(world));
        }
    }

    public static void scheduleContainedFluidTick(World world, BlockPos pos, IBlockState state) {
        scheduleFluidTick(world, pos, getFluidState(world, pos, state));
    }
}
