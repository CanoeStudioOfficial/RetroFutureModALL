package com.canoestudio.retrofuturemccore.api.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface RetroWaterloggedBlock extends RetroFluidloggableBlock {

    PropertyBool getWaterloggedProperty();

    default void swapWaterProperty(World world, BlockPos pos, IBlockState state) {
        boolean underwater = checkSurroundingUnderwaterPosition(world, pos);
        PropertyBool property = getWaterloggedProperty();
        if (!underwater && state.getValue(property)) {
            world.setBlockState(pos, state.withProperty(property, false), 3);
        } else if (underwater && !state.getValue(property)) {
            world.setBlockState(pos, state.withProperty(property, true), 3);
        }
    }

    default boolean isPositionUnderwater(World world, BlockPos pos) {
        return isPositionUnderwater(world, pos, false);
    }

    default boolean isPositionUnderwater(World world, BlockPos pos, boolean onlyTrueWater) {
        if (onlyTrueWater) {
            return WaterloggedPlantFluid.isSourceWater(world, pos);
        }
        return isWaterHere(world, pos);
    }

    default boolean checkSurroundingUnderwaterPosition(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (!isAcceptableWaterNeighbor(world, pos.offset(facing), facing)) {
                return false;
            }
        }
        return true;
    }

    default boolean isAcceptableWaterNeighbor(World world, BlockPos pos, EnumFacing facing) {
        IBlockState state = world.getBlockState(pos);
        return isWaterHere(world, pos) || state.isSideSolid(world, pos, facing);
    }

    default boolean isAcceptableNeighbor(World world, BlockPos pos, EnumFacing facing) {
        return isAcceptableWaterNeighbor(world, pos, facing);
    }

    default boolean isWaterHere(World world, BlockPos pos) {
        return RetroWaterlogging.isWater(world, pos);
    }

    default Material getWaterloggedMaterial(IBlockState state, Material fallback) {
        return RetroWaterlogging.materialForWaterlogged(state, fallback, getWaterloggedProperty());
    }

    default void syncWaterloggedAfterNeighborChanged(World world, BlockPos pos, IBlockState state) {
        RetroWaterlogging.onNeighborChanged(world, pos, state);
    }
}
