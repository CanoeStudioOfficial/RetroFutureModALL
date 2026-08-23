package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import java.util.List;
import net.minecraft.entity.ai.EntityAIBase;

/** Converts a completed turtle breeding interaction into one egg carrier. */
public final class EntityAITurtleMate extends EntityAIBase {

    private final EntityTurtle turtle;
    private EntityTurtle mate;

    public EntityAITurtleMate(EntityTurtle turtle) {
        this.turtle = turtle;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.turtle.isChild() || this.turtle.hasEgg() || !this.turtle.isInLove()) {
            return false;
        }
        List<EntityTurtle> turtles = this.turtle.world.getEntitiesWithinAABB(EntityTurtle.class,
            this.turtle.getEntityBoundingBox().grow(8.0D));
        for (EntityTurtle candidate : turtles) {
            if (candidate != this.turtle && !candidate.isChild() && !candidate.hasEgg()
                    && candidate.isInLove() && this.turtle.canMateWith(candidate)) {
                this.mate = candidate;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.mate != null && this.mate.isEntityAlive() && !this.mate.hasEgg()
            && this.turtle.isInLove();
    }

    @Override
    public void startExecuting() {
        this.turtle.getNavigator().tryMoveToEntityLiving(this.mate, 1.0D);
    }

    @Override
    public void resetTask() {
        this.mate = null;
        this.turtle.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        this.turtle.getLookHelper().setLookPositionWithEntity(this.mate,
            this.turtle.getHorizontalFaceSpeed() + 20.0F, this.turtle.getVerticalFaceSpeed());
        if (this.turtle.getDistanceSq(this.mate) > 6.25D) {
            this.turtle.getNavigator().tryMoveToEntityLiving(this.mate, 1.0D);
            return;
        }

        this.turtle.resetInLove();
        this.mate.resetInLove();
        this.turtle.setGrowingAge(6000);
        this.mate.setGrowingAge(6000);
        this.turtle.setHasEgg(true);
        this.turtle.setLayEggCooldown(100);
    }
}
