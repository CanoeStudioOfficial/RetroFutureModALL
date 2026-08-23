package com.canoestudio.retrofuturemccore.api.fluid;

import com.canoestudio.retrofuturemccore.RetroFutureMCCore;
import java.lang.reflect.Method;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

public final class RetroFluidCompat {

    private static boolean initialized;
    private static boolean fluidloggedAvailable;
    private static Method apiGetFluidState;
    private static Method apiSetFluidState;
    private static Method apiIsCompatibleWater;

    private RetroFluidCompat() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!Loader.isModLoaded("fluidlogged_api")) {
            fluidloggedAvailable = false;
            return;
        }

        try {
            Class.forName("git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils");
            Class.forName("git.jbredwards.fluidlogged_api.api.util.FluidState");
            Class<?> bridge = Class.forName(
                "com.canoestudio.retrofuturemccore.internal.fluid.FluidloggedApiBridge");
            apiGetFluidState = bridge.getMethod("getFluidState", IBlockAccess.class,
                BlockPos.class, IBlockState.class);
            apiSetFluidState = bridge.getMethod("setFluidState", World.class, BlockPos.class,
                IBlockState.class, RetroFluidState.class, int.class);
            apiIsCompatibleWater = bridge.getMethod("isCompatibleWater", Fluid.class);
            fluidloggedAvailable = true;
            RetroFutureMCCore.LOGGER.info("Fluidlogged API detected; RetroFuture fluid bridge is enabled.");
        } catch (ReflectiveOperationException | LinkageError e) {
            fluidloggedAvailable = false;
            RetroFutureMCCore.LOGGER.warn("Fluidlogged API was detected but its bridge could not initialize; "
                + "falling back to RetroFuture waterlogged simulation.", e);
        }
    }

    public static boolean isFluidloggedAvailable() {
        init();
        return fluidloggedAvailable;
    }

    public static boolean isWater(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos).isWater();
    }

    public static boolean isWater(RetroFluidState fluidState) {
        return fluidState != null && fluidState.isWater();
    }

    public static boolean isWater(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        if (isFluidloggedAvailable()) {
            try {
                return Boolean.TRUE.equals(apiIsCompatibleWater.invoke(null, fluid));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                RetroFutureMCCore.LOGGER.debug("Failed to query Fluidlogged API fluid compatibility.", e);
            }
        }
        return fluid == FluidRegistry.WATER;
    }

    public static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos) {
        return getFluidState(world, pos, world.getBlockState(pos));
    }

    public static RetroFluidState getFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        if (isFluidloggedAvailable()) {
            RetroFluidState fluidState = queryFluidloggedState(world, pos, state);
            if (fluidState != null) {
                return fluidState;
            }
        }

        if (isWaterBlock(state)) {
            return RetroFluidState.ofWater(state);
        }

        if (state.getBlock() instanceof RetroWaterloggedBlock) {
            PropertyBool property = ((RetroWaterloggedBlock) state.getBlock()).getWaterloggedProperty();
            if (state.getValue(property)) {
                return RetroFluidState.ofWater(Blocks.WATER.getDefaultState());
            }
        }

        if (state.getMaterial() == Material.WATER) {
            return RetroFluidState.ofWater(state);
        }

        return RetroFluidState.EMPTY;
    }

    public static boolean setFluidState(World world, BlockPos pos, IBlockState here, RetroFluidState fluidState,
            int flags) {
        if (fluidState == null || fluidState.isEmpty()) {
            return false;
        }

        if (isFluidloggedAvailable()) {
            Boolean result = setFluidloggedState(world, pos, here, fluidState, flags);
            if (result != null) {
                return result;
            }
        }

        IBlockState current = world.getBlockState(pos);
        IBlockState waterlogged = WaterloggedPlantFluid.withWaterlogged(current, fluidState.isWater());
        if (waterlogged != null) {
            world.setBlockState(pos, waterlogged, flags);
            return true;
        }
        return false;
    }

    public static void ensureWaterlogged(World world, BlockPos pos, IBlockState state, PropertyBool property,
            int flags) {
        if (!world.isRemote && state.getValue(property) && !world.provider.doesWaterVaporize()
                && !getFluidState(world, pos, state).isWater()) {
            setFluidState(world, pos, state, RetroFluidState.ofWater(Blocks.WATER.getDefaultState()), flags);
        }
    }

    public static boolean isWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos, PropertyBool property) {
        return state.getValue(property) || getFluidState(world, pos, state).isWater();
    }

    public static IBlockState withActualWaterlogged(IBlockState state, IBlockAccess world, BlockPos pos,
            PropertyBool property) {
        return state.withProperty(property, isWaterlogged(state, world, pos, property));
    }

    public static void setWaterloggedProperty(World world, BlockPos pos, IBlockState state, PropertyBool property,
            boolean waterlogged, int flags) {
        if (state.getValue(property) != waterlogged) {
            world.setBlockState(pos, state.withProperty(property, waterlogged), flags);
        }
    }

    public static void restoreWater(World world, BlockPos pos, IBlockState replacedState, int flags) {
        if (world.provider.doesWaterVaporize()) {
            world.setBlockToAir(pos);
            return;
        }

        if (isFluidloggedAvailable()) {
            RetroFluidState fluidState = getFluidState(world, pos, replacedState);
            world.setBlockState(pos, fluidState.isWater()
                ? fluidState.getState() : Blocks.AIR.getDefaultState(), flags);
            if (fluidState.isWater()) {
                world.scheduleUpdate(pos, fluidState.getState().getBlock(),
                    fluidState.getState().getBlock().tickRate(world));
            }
            return;
        }

        world.setBlockState(pos, WaterloggedPlantFluid.isWaterlogged(replacedState)
            ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState(), flags);
    }

    public static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroFluidState fluidState = getFluidState(world, pos, state);
        if (fluidState.isWater()) {
            if (isFluidloggedAvailable()) {
                BlockStateTick.schedule(world, pos, fluidState.getState().getBlock());
            } else {
                WaterloggedPlantFluid.onNeighborChanged(world, pos, fluidState.getState().getBlock());
            }
        }
    }

    public static boolean isVanillaWater(IBlockState state) {
        return isWaterBlock(state) || state.getMaterial() == Material.WATER;
    }

    public static boolean isWaterBlock(IBlockState state) {
        return state.getBlock() == Blocks.WATER
            || state.getBlock() == Blocks.FLOWING_WATER;
    }

    private static RetroFluidState queryFluidloggedState(IBlockAccess world, BlockPos pos,
            IBlockState state) {
        try {
            Object result = apiGetFluidState.invoke(null, world, pos, state);
            return result instanceof RetroFluidState ? (RetroFluidState) result : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            RetroFutureMCCore.LOGGER.debug("Failed to query Fluidlogged API state at {}", pos, e);
            return null;
        }
    }

    private static Boolean setFluidloggedState(World world, BlockPos pos, IBlockState here,
            RetroFluidState fluidState, int flags) {
        try {
            Object result = apiSetFluidState.invoke(null, world, pos, here, fluidState, flags);
            return result instanceof Boolean ? (Boolean) result : Boolean.FALSE;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            RetroFutureMCCore.LOGGER.debug("Failed to set Fluidlogged API state at {}", pos, e);
            return null;
        }
    }

    private static final class BlockStateTick {
        private BlockStateTick() {
        }

        private static void schedule(World world, BlockPos pos, net.minecraft.block.Block block) {
            world.scheduleUpdate(pos, block, block.tickRate(world));
        }
    }
}
