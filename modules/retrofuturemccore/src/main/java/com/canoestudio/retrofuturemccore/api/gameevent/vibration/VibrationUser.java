package com.canoestudio.retrofuturemccore.api.gameevent.vibration;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public interface VibrationUser {

    Vec3d getPosition();

    int getListenerRadius();

    boolean canReceiveVibration(WorldServer world, Vec3d sourcePosition, RetroGameEvent event, GameEventContext context);

    void onReceiveVibration(WorldServer world, Vec3d sourcePosition, RetroGameEvent event, GameEventContext context,
            int distance);

    boolean requiresLineOfSight();
}
