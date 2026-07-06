package com.canoestudio.retrofuturemccore.api.gameevent;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public interface GameEventListener {

    Vec3d getListenerPosition();

    int getListenerRadius();

    boolean handleGameEvent(WorldServer world, RetroGameEvent event, GameEventContext context, Vec3d sourcePosition);
}
