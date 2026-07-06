package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.LookTarget;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class SetEntityLookTargetBehavior<E extends EntityLiving> extends Behavior<E> {

    private final Class<? extends EntityLivingBase> targetClass;
    private final float maxDistance;

    public SetEntityLookTargetBehavior(Class<? extends EntityLivingBase> targetClass, float maxDistance) {
        super(requirements(), 20);
        this.targetClass = targetClass;
        this.maxDistance = maxDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(WorldServer world, Brain<? extends E> brain, E entity) {
        return this.findTarget(brain, entity) != null;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        EntityLivingBase target = this.findTarget(brain, entity);
        if (target != null) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new LookTarget.EntityLookTarget(target, true));
        }
    }

    private EntityLivingBase findTarget(Brain<? extends E> brain, E entity) {
        List<EntityLivingBase> entities = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                .orElse(Collections.<EntityLivingBase>emptyList());
        double maxDistanceSq = this.maxDistance * this.maxDistance;
        for (EntityLivingBase target : entities) {
            if (this.targetClass.isInstance(target) && entity.getDistanceSq(target) <= maxDistanceSq
                    && entity.canEntityBeSeen(target)) {
                return target;
            }
        }
        return null;
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT);
    }
}
