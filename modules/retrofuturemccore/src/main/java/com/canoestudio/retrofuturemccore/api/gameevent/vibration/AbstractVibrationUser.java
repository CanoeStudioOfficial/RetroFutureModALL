package com.canoestudio.retrofuturemccore.api.gameevent.vibration;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public abstract class AbstractVibrationUser implements VibrationUser {

    @Override
    public boolean canReceiveVibration(WorldServer world, Vec3d sourcePosition, RetroGameEvent event,
            GameEventContext context) {
        return true;
    }

    @Override
    public boolean requiresLineOfSight() {
        return true;
    }
}
