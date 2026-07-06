package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.LookTarget;
import com.canoestudio.retrofuturemccore.api.ai.util.WalkTarget;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class SetWalkTargetFromLookTargetBehavior<E extends EntityLiving> extends Behavior<E> {

    private final double speed;
    private final int closeEnoughDistance;

    public SetWalkTargetFromLookTargetBehavior(double speed, int closeEnoughDistance) {
        super(requirements(), 20);
        this.speed = speed;
        this.closeEnoughDistance = closeEnoughDistance;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        LookTarget lookTarget = (LookTarget) brain.getMemory(MemoryModuleType.LOOK_TARGET).orElse(null);
        if (lookTarget != null) {
            Vec3d target = lookTarget.currentPosition();
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, this.speed, this.closeEnoughDistance));
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        Map<MemoryModuleType<?>, MemoryStatus> requirements = new LinkedHashMap<MemoryModuleType<?>, MemoryStatus>();
        requirements.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT);
        requirements.put(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
        return requirements;
    }
}
