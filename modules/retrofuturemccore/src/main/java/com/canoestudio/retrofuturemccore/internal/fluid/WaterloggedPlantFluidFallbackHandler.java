package com.canoestudio.retrofuturemccore.internal.fluid;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidCompat;
import com.canoestudio.retrofuturemccore.api.fluid.WaterloggedPlantFluid;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Supplies the vanilla entry points needed by the local water fallback. */
public final class WaterloggedPlantFluidFallbackHandler {

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (RetroFluidCompat.isFluidloggedAvailable()) {
            return;
        }
        wakeNearbyWater(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (RetroFluidCompat.isFluidloggedAvailable()) {
            return;
        }
        wakeNearbyWater(event.getWorld(), event.getPos());
    }

    private static void wakeNearbyWater(World world, BlockPos changedPos) {
        if (world.isRemote) {
            return;
        }
        allowFlowingWaterToWaterlog(world, changedPos);
        wakeWater(world, changedPos);
        for (EnumFacing facing : EnumFacing.values()) {
            allowFlowingWaterToWaterlog(world, changedPos.offset(facing));
            wakeWater(world, changedPos.offset(facing));
        }
    }

    /** Allows flowing water to fill a compatible adjacent state. */
    private static void allowFlowingWaterToWaterlog(World world, BlockPos flowingPos) {
        IBlockState flowing = world.getBlockState(flowingPos);
        if (flowing.getBlock() != Blocks.FLOWING_WATER) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos targetPos = flowingPos.offset(facing);
            IBlockState target = world.getBlockState(targetPos);
            IBlockState waterlogged = WaterloggedPlantFluid.withWaterlogged(target, true);
            if (waterlogged != null && !WaterloggedPlantFluid.isWaterlogged(target)) {
                world.setBlockState(targetPos, waterlogged, 3);
                world.scheduleUpdate(targetPos, waterlogged.getBlock(), 5);
            }
        }
    }

    private static void wakeWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (WaterloggedPlantFluid.isSourceWater(state)) {
            WaterloggedPlantFluid.onNeighborChanged(world, pos, state.getBlock());
        }
    }
}
