package com.canoestudio.retrofuturemccore.api.gameevent.vibration;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class VibrationTicker {

    private VibrationTicker() {
    }

    public static void tick(World world, VibrationSystem system) {
        if (!(world instanceof WorldServer) || system == null) {
            return;
        }

        VibrationData data = system.getVibrationData();
        if (!data.hasPendingVibration()) {
            return;
        }

        if (data.getTravelTimeInTicks() > 0) {
            data.tickDelay();
            return;
        }

        VibrationData.VibrationInfo vibration = data.getCurrentVibration();
        data.clear();
        if (vibration != null) {
            system.getVibrationUser().onReceiveVibration((WorldServer) world, vibration.getSourcePosition(),
                    vibration.getEvent(),
                    vibration.getContext() == null ? GameEventContext.empty() : vibration.getContext(),
                    vibration.getDistance());
        }
    }
}
