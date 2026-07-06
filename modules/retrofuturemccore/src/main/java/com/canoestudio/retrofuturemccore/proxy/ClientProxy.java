package com.canoestudio.retrofuturemccore.proxy;

import com.canoestudio.retrofuturemccore.api.component.IRetroEntityComponents;
import com.canoestudio.retrofuturemccore.client.RetroZoomClientHandler;
import com.canoestudio.retrofuturemccore.internal.component.RetroEntityComponentsCapability;
import com.canoestudio.retrofuturemccore.network.message.MessageSyncEntityComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new RetroZoomClientHandler());
    }

    @Override
    public void handleEntityComponentSync(final MessageSyncEntityComponent message) {
        IThreadListener thread = Minecraft.getMinecraft();
        thread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                applyEntityComponentSync(message);
            }
        });
    }

    private static void applyEntityComponentSync(MessageSyncEntityComponent message) {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }

        Entity entity = world.getEntityByID(message.getEntityId());
        if (entity == null || !entity.hasCapability(RetroEntityComponentsCapability.CAPABILITY, null)) {
            return;
        }

        IRetroEntityComponents components = entity.getCapability(RetroEntityComponentsCapability.CAPABILITY, null);
        if (components != null) {
            components.readSyncNbt(message.getComponentId(), message.getTag());
        }
    }
}
