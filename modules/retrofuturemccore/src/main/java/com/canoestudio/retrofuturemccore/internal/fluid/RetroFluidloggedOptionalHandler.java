package com.canoestudio.retrofuturemccore.internal.fluid;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidState;
import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidloggableBlock;
import git.jbredwards.fluidlogged_api.api.event.FluidloggedEvent;
import git.jbredwards.fluidlogged_api.api.event.FluidloggableEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RetroFluidloggedOptionalHandler {

    @SubscribeEvent
    public void onFluidloggable(FluidloggableEvent event) {
        IBlockState state = event.state;
        if (!(state.getBlock() instanceof RetroFluidloggableBlock)
                || event.fluid == null
                || !FluidloggedApiBridge.isCompatibleWater(event.fluid)) {
            return;
        }

        RetroFluidState fluidState = FluidloggedApiBridge.fromNative(event.fluidState);
        if (((RetroFluidloggableBlock) state.getBlock()).isRetroFluidloggable(state, event.access, event.pos,
                fluidState)) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void onFluidlogged(FluidloggedEvent event) {
        if (!(event.here.getBlock() instanceof RetroFluidloggableBlock)) {
            return;
        }

        RetroFluidState fluidState = FluidloggedApiBridge.fromNative(event.fluidState);
        EnumActionResult result = ((RetroFluidloggableBlock) event.here.getBlock())
            .onRetroFluidChange(event.world, event.pos, event.here, fluidState, event.blockFlags);
        if (result == EnumActionResult.SUCCESS) {
            event.setResult(Event.Result.ALLOW);
        } else if (result == EnumActionResult.FAIL) {
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getState().getBlock() instanceof RetroFluidloggableBlock) {
            event.getNotifiedSides().add(EnumFacing.UP);
        }
    }
}
