package com.canoestudio.retrofuturemccore.proxy;

import com.canoestudio.retrofuturemccore.internal.component.RetroComponentEventHandler;
import com.canoestudio.retrofuturemccore.internal.component.RetroEntityComponentsCapability;
import com.canoestudio.retrofuturemccore.internal.event.RetroEventBridge;
import com.canoestudio.retrofuturemccore.internal.gameevent.GameEventWorldCleanupHandler;
import com.canoestudio.retrofuturemccore.internal.item.RetroItemUseEventHandler;
import com.canoestudio.retrofuturemccore.network.RetroFutureCoreNetwork;
import com.canoestudio.retrofuturemccore.network.message.MessageSyncEntityComponent;
import com.canoestudio.retrofuturemccore.api.tag.RetroTagJsonLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        RetroEntityComponentsCapability.register();
        RetroFutureCoreNetwork.registerMessages();
        MinecraftForge.EVENT_BUS.register(new RetroComponentEventHandler());
        MinecraftForge.EVENT_BUS.register(new RetroEventBridge());
        MinecraftForge.EVENT_BUS.register(new GameEventWorldCleanupHandler());
        MinecraftForge.EVENT_BUS.register(new RetroItemUseEventHandler());
    }

    public void handleEntityComponentSync(MessageSyncEntityComponent message) {
    }

    public void postInit(FMLPostInitializationEvent event) {
        RetroTagJsonLoader.loadAllActiveModTags();
    }
}
