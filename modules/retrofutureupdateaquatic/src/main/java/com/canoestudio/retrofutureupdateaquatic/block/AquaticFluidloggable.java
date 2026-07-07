package com.canoestudio.retrofutureupdateaquatic.block;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import git.jbredwards.fluidlogged_api.api.world.IWorldProvider;
import javax.annotation.Nonnull;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

interface AquaticFluidloggable extends IFluidloggable {

    PropertyBool getWaterloggedProperty();

    @Override
    default boolean isFluidValid(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
            @Nonnull Fluid fluid) {
        return FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluid);
    }

    @Override
    default boolean isFluidloggable(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
            @Nonnull FluidState fluidState) {
        return fluidState.isEmpty() || fluidState.isFluidloggable()
            && isFluidValid(state, IWorldProvider.getWorld(world), pos, fluidState.getFluid());
    }

    @Nonnull
    @Override
    default EnumActionResult onFluidFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here,
            @Nonnull FluidState newFluid, int blockFlags) {
        AquaticWaterHelper.setWaterloggedProperty(world, pos, here, getWaterloggedProperty(), true, blockFlags);
        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    default EnumActionResult onFluidDrain(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here,
            int blockFlags) {
        AquaticWaterHelper.setWaterloggedProperty(world, pos, here, getWaterloggedProperty(), false, blockFlags);
        return EnumActionResult.PASS;
    }

    @Override
    default boolean canFluidFlow(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState here,
            @Nonnull EnumFacing side) {
        return true;
    }

    @Override
    default boolean canFluidConnect(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState here,
            @Nonnull EnumFacing side) {
        return true;
    }

    @Override
    default boolean overrideApplyDefaultsSetting() {
        return true;
    }
}
