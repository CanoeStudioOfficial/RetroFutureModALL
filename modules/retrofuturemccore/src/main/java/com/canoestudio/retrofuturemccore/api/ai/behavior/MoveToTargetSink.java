package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.WalkTarget;
import java.util.Collections;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class MoveToTargetSink<E extends EntityLiving> extends Behavior<E> {

    public MoveToTargetSink() {
        super(requirements(), Integer.MAX_VALUE);
    }

    @Override
    protected boolean canStillUse(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        return brain.hasMemoryValue(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        WalkTarget walkTarget = (WalkTarget) brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (walkTarget == null) {
            return;
        }

        Vec3d target = walkTarget.getTarget();
        double closeEnoughSq = walkTarget.getCloseEnoughDistance() * walkTarget.getCloseEnoughDistance();
        if (entity.getPositionVector().squareDistanceTo(target) <= closeEnoughSq) {
            entity.getNavigator().clearPath();
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else if (entity.getNavigator().noPath()) {
            entity.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, walkTarget.getSpeed());
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_PRESENT);
    }
}
