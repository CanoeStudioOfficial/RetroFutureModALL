package com.canoestudio.retrofuturemccore.api.gameevent.vibration;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class VibrationData {

    private VibrationInfo currentVibration;
    private int travelTimeInTicks;

    public boolean hasPendingVibration() {
        return this.currentVibration != null;
    }

    public VibrationInfo getCurrentVibration() {
        return this.currentVibration;
    }

    public int getTravelTimeInTicks() {
        return this.travelTimeInTicks;
    }

    public void schedule(VibrationInfo vibration, int travelTimeInTicks) {
        this.currentVibration = vibration;
        this.travelTimeInTicks = Math.max(0, travelTimeInTicks);
    }

    public boolean shouldReplaceCurrent(VibrationInfo vibration) {
        if (this.currentVibration == null) {
            return true;
        }
        if (vibration.distance < this.currentVibration.distance) {
            return true;
        }
        return vibration.distance == this.currentVibration.distance
                && vibration.event.getVibrationFrequency() > this.currentVibration.event.getVibrationFrequency();
    }

    public void clear() {
        this.currentVibration = null;
        this.travelTimeInTicks = 0;
    }

    public void tickDelay() {
        if (this.travelTimeInTicks > 0) {
            --this.travelTimeInTicks;
        }
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setInteger("TravelTime", this.travelTimeInTicks);
        if (this.currentVibration != null) {
            tag.setTag("Vibration", this.currentVibration.writeToNbt(new NBTTagCompound()));
        }
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        this.travelTimeInTicks = tag.getInteger("TravelTime");
        if (tag.hasKey("Vibration", 10)) {
            this.currentVibration = VibrationInfo.readFromNbt(tag.getCompoundTag("Vibration"));
        } else {
            this.currentVibration = null;
        }
    }

    public static final class VibrationInfo {

        private final RetroGameEvent event;
        private final Vec3d sourcePosition;
        private final GameEventContext context;
        private final int distance;

        public VibrationInfo(RetroGameEvent event, Vec3d sourcePosition, GameEventContext context, int distance) {
            this.event = event;
            this.sourcePosition = sourcePosition;
            this.context = context;
            this.distance = distance;
        }

        public RetroGameEvent getEvent() {
            return this.event;
        }

        public Vec3d getSourcePosition() {
            return this.sourcePosition;
        }

        public GameEventContext getContext() {
            return this.context;
        }

        public int getDistance() {
            return this.distance;
        }

        public NBTTagCompound writeToNbt(NBTTagCompound tag) {
            tag.setString("Event", this.event.getId().toString());
            tag.setDouble("X", this.sourcePosition.x);
            tag.setDouble("Y", this.sourcePosition.y);
            tag.setDouble("Z", this.sourcePosition.z);
            tag.setInteger("Distance", this.distance);
            return tag;
        }

        public static VibrationInfo readFromNbt(NBTTagCompound tag) {
            RetroGameEvent event = RetroGameEvent.get(new ResourceLocation(tag.getString("Event")));
            if (event == null) {
                return null;
            }
            Vec3d sourcePosition = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
            return new VibrationInfo(event, sourcePosition, GameEventContext.empty(), tag.getInteger("Distance"));
        }
    }
}
