package com.canoestudio.retrofuturemccore.api.gameevent.vibration;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import com.canoestudio.retrofuturemccore.api.gameevent.GameEventListener;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class VibrationListener implements GameEventListener {

    private final VibrationSystem system;

    public VibrationListener(VibrationSystem system) {
        this.system = system;
    }

    @Override
    public Vec3d getListenerPosition() {
        return this.system.getVibrationUser().getPosition();
    }

    @Override
    public int getListenerRadius() {
        return this.system.getVibrationUser().getListenerRadius();
    }

    @Override
    public boolean handleGameEvent(WorldServer world, RetroGameEvent event, GameEventContext context,
            Vec3d sourcePosition) {
        VibrationData data = this.system.getVibrationData();
        VibrationUser user = this.system.getVibrationUser();
        Vec3d listenerPosition = user.getPosition();
        if (listenerPosition == null || sourcePosition == null) {
            return false;
        }

        int distance = distanceBetweenInBlocks(sourcePosition, listenerPosition);
        if (distance > user.getListenerRadius()) {
            return false;
        }
        if (!user.canReceiveVibration(world, sourcePosition, event, context)) {
            return false;
        }
        if (user.requiresLineOfSight() && isOccluded(world, sourcePosition, listenerPosition)) {
            return false;
        }

        VibrationData.VibrationInfo vibration = new VibrationData.VibrationInfo(event, sourcePosition, context, distance);
        if (!data.shouldReplaceCurrent(vibration)) {
            return false;
        }
        data.schedule(vibration, distance);
        return true;
    }

    public void forceSchedule(WorldServer world, RetroGameEvent event, GameEventContext context, Vec3d sourcePosition) {
        VibrationUser user = this.system.getVibrationUser();
        Vec3d listenerPosition = user.getPosition();
        if (listenerPosition == null || sourcePosition == null) {
            return;
        }
        int distance = distanceBetweenInBlocks(sourcePosition, listenerPosition);
        this.system.getVibrationData().schedule(
                new VibrationData.VibrationInfo(event, sourcePosition, context, distance), distance);
    }

    public static int distanceBetweenInBlocks(Vec3d first, Vec3d second) {
        return (int) Math.floor(first.distanceTo(second));
    }

    private static boolean isOccluded(WorldServer world, Vec3d sourcePosition, Vec3d listenerPosition) {
        RayTraceResult result = world.rayTraceBlocks(sourcePosition, listenerPosition, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK;
    }
}
