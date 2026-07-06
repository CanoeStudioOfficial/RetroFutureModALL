package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.LookTarget;
import com.canoestudio.retrofuturemccore.api.ai.util.WalkTarget;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class SetWalkTargetFromAttackTargetBehavior<E extends EntityLiving> extends Behavior<E> {

    private final double speed;
    private final int closeEnoughDistance;
    private final double attackReach;

    public SetWalkTargetFromAttackTargetBehavior(double speed, int closeEnoughDistance, double attackReach) {
        super(requirements(), 20);
        this.speed = speed;
        this.closeEnoughDistance = closeEnoughDistance;
        this.attackReach = attackReach;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        EntityLivingBase target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isEntityAlive()) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return;
        }

        brain.setMemory(MemoryModuleType.LOOK_TARGET, new LookTarget.EntityLookTarget(target, true));
        if (entity.getDistanceSq(target) > this.attackReach * this.attackReach) {
            brain.setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(target.getPositionVector(), this.speed, this.closeEnoughDistance));
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        Map<MemoryModuleType<?>, MemoryStatus> requirements = new LinkedHashMap<MemoryModuleType<?>, MemoryStatus>();
        requirements.put(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT);
        requirements.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
        requirements.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
        return requirements;
    }
}
