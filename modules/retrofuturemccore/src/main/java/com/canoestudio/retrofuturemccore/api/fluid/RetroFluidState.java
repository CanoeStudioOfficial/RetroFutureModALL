package com.canoestudio.retrofuturemccore.api.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public final class RetroFluidState {

    public static final RetroFluidState EMPTY =
        new RetroFluidState(true, false, Blocks.AIR.getDefaultState(), null);

    private final boolean empty;
    private final boolean water;
    private final IBlockState state;
    private final Object nativeState;

    private RetroFluidState(boolean empty, boolean water, IBlockState state, Object nativeState) {
        this.empty = empty;
        this.water = water;
        this.state = state == null ? Blocks.AIR.getDefaultState() : state;
        this.nativeState = nativeState;
    }

    public static RetroFluidState ofWater(IBlockState state) {
        return new RetroFluidState(false, true, state, null);
    }

    public static RetroFluidState ofWater(IBlockState state, Object nativeState) {
        return new RetroFluidState(false, true, state, nativeState);
    }

    public static RetroFluidState ofNative(boolean empty, boolean water, IBlockState state, Object nativeState) {
        return empty ? EMPTY : new RetroFluidState(false, water, state, nativeState);
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean isWater() {
        return this.water;
    }

    public IBlockState getState() {
        return this.state;
    }

    public boolean hasNativeState() {
        return this.nativeState != null;
    }

    public Object getNativeState() {
        return this.nativeState;
    }
}
