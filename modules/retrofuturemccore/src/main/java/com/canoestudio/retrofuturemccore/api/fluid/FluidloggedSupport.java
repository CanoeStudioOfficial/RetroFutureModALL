package com.canoestudio.retrofuturemccore.api.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

/**
 * Compatibility facade for contained water.
 *
 * <p>Fluidlogged API is optional. Calls are routed to it when it is installed;
 * otherwise the Farmers-Future-Delight waterlogged block-state implementation
 * is used.</p>
 */
public final class FluidloggedSupport {

    private FluidloggedSupport() {
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

    public static boolean isWater(Fluid fluid) {
        return RetroFluidCompat.isWater(fluid);
    }

    /** Equivalent of Entity#isInWater() that also checks fluidlogged blocks. */
    public static boolean isEntityInWater(Entity entity) {
        if (entity == null || entity.world == null) {
            return false;
        }

        AxisAlignedBB box = entity.getEntityBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.floor(box.maxX + 0.999D);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.floor(box.maxY + 0.999D);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.floor(box.maxZ + 0.999D);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (isWater(entity.world, new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isVanillaWater(IBlockState state) {
        return RetroFluidCompat.isVanillaWater(state);
    }

    public static boolean isWaterBlock(IBlockState state) {
        return RetroFluidCompat.isWaterBlock(state);
    }

    public static boolean canReplaceWater(World world, BlockPos pos) {
        return RetroWaterlogging.canReplaceWater(world, pos);
    }

    public static boolean canPlaceIntoAirOrWater(World world, BlockPos pos) {
        return RetroWaterlogging.canPlaceIntoAirOrWater(world, pos);
    }

    public static void restoreWater(World world, BlockPos pos) {
        RetroWaterlogging.restoreWater(world, pos);
    }

    public static void restoreWater(World world, BlockPos pos, IBlockState replacedState) {
        RetroWaterlogging.restoreWater(world, pos, replacedState);
    }

    public static void restoreFluidOrAir(World world, BlockPos pos, IBlockState replacedState, int flags) {
        RetroWaterlogging.restoreFluidOrAir(world, pos, replacedState, flags);
    }

    public static void restoreContainedFluidOrAir(World world, BlockPos pos, IBlockState state, int flags) {
        RetroWaterlogging.restoreContainedFluidOrAir(world, pos, state, flags);
    }

    public static void setFluidloggableBlock(World world, BlockPos pos, IBlockState newState, int flags) {
        RetroWaterlogging.setFluidloggableBlock(world, pos, newState, flags);
    }

    public static boolean setFluidState(World world, BlockPos pos, IBlockState here,
            RetroFluidState fluidState, int flags) {
        return RetroWaterlogging.setFluidState(world, pos, here, fluidState, flags);
    }

    public static void scheduleFluidTick(World world, BlockPos pos, IBlockState state) {
        RetroWaterlogging.scheduleFluidTick(world, pos, state);
    }
}
