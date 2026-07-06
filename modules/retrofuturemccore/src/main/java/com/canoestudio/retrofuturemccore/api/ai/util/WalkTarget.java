package com.canoestudio.retrofuturemccore.api.ai.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class WalkTarget {

    private final Vec3d target;
    private final double speed;
    private final int closeEnoughDistance;

    public WalkTarget(BlockPos target, double speed, int closeEnoughDistance) {
        this(new Vec3d(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D), speed, closeEnoughDistance);
    }

    public WalkTarget(Vec3d target, double speed, int closeEnoughDistance) {
        this.target = target;
        this.speed = speed;
        this.closeEnoughDistance = closeEnoughDistance;
    }

    public Vec3d getTarget() {
        return this.target;
    }

    public double getSpeed() {
        return this.speed;
    }

    public int getCloseEnoughDistance() {
        return this.closeEnoughDistance;
    }
}
