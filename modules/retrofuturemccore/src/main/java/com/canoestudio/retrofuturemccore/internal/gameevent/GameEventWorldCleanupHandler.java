package com.canoestudio.retrofuturemccore.internal.gameevent;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GameEventWorldCleanupHandler {

    @SubscribeEvent
    public void unloadWorld(WorldEvent.Unload event) {
        RetroGameEventDispatcher.clear(event.getWorld());
    }
}
