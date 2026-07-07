package com.canoestudio.retrofuturemccore.internal.fluid;

import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterloggedBlock;
import git.jbredwards.fluidlogged_api.api.event.FluidloggableEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fluids.FluidRegistry;

public class RetroFluidloggedOptionalHandler {

    @SubscribeEvent
    public void onFluidloggable(FluidloggableEvent event) {
        IBlockState state = event.state;
        if (state.getBlock() instanceof RetroWaterloggedBlock
                && event.fluid != null
                && FluidRegistry.WATER == event.fluid) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getState().getBlock() instanceof RetroWaterloggedBlock) {
            event.getNotifiedSides().add(EnumFacing.UP);
        }
    }
}
