package com.canoestudio.retrofuturemccore.internal.fluid;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public final class FluidloggedApiBridge {

    private FluidloggedApiBridge() {
    }

    public static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        FluidState fluidState = FluidloggedUtils.getFluidState(world, pos, state);
        return fromNative(fluidState);
    }

    public static RetroFluidState fromNative(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return RetroFluidState.EMPTY;
        }
        boolean water = isCompatibleWater(fluidState.getFluid());
        return RetroFluidState.ofNative(false, water, fluidState.getState(), fluidState);
    }

    public static boolean isCompatibleWater(Fluid fluid) {
        return fluid != null && FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluid);
    }

    public static boolean setFluidState(World world, BlockPos pos, IBlockState here, RetroFluidState fluidState,
            int flags) {
        Object nativeState = fluidState.hasNativeState() ? fluidState.getNativeState()
            : fluidState.isWater() ? FluidState.of(FluidRegistry.WATER) : null;
        return nativeState instanceof FluidState
            && FluidloggedUtils.setFluidState(world, pos, here, (FluidState) nativeState, false, flags);
    }
}
