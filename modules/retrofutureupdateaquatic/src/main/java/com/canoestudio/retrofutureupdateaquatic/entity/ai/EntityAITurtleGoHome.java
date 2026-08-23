package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Returns an egg-carrying turtle to its remembered beach using navigation. */
public final class EntityAITurtleGoHome extends EntityAIBase {

    private final EntityTurtle turtle;
    private final double speed;

    public EntityAITurtleGoHome(EntityTurtle turtle, double speed) {
        this.turtle = turtle;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return !this.turtle.isChild() && this.turtle.hasEgg()
            && this.turtle.getDistanceSqToHome() > 4.0D;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.turtle.hasEgg() && this.turtle.getDistanceSqToHome() > 2.25D;
    }

    @Override
    public void startExecuting() {
        this.moveTowardHome();
    }

    @Override
    public void resetTask() {
        this.turtle.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (this.turtle.getNavigator().noPath() || this.turtle.getRNG().nextInt(20) == 0) {
            this.moveTowardHome();
        }
    }

    private void moveTowardHome() {
        BlockPos home = this.turtle.getHomePos();
        if (this.turtle.getDistanceSqToHome() <= 16.0D) {
            this.turtle.getNavigator().tryMoveToXYZ(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D,
                this.speed);
            return;
        }

        Vec3d target = RandomPositionGenerator.findRandomTargetBlockTowards(this.turtle, 16, 7,
            new Vec3d(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D));
        if (target != null) {
            this.turtle.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, this.speed);
        }
    }
}
