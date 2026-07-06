package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;

public class RunOneBehavior<E extends EntityLivingBase> extends GateBehavior<E> {

    public RunOneBehavior(Collection<WeightedBehavior<E>> weightedBehaviors) {
        this(Collections.<MemoryModuleType<?>, MemoryStatus>emptyMap(), weightedBehaviors);
    }

    public RunOneBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
            Collection<WeightedBehavior<E>> weightedBehaviors) {
        super(entryCondition, Collections.<MemoryModuleType<?>>emptySet(), OrderPolicy.SHUFFLED, RunningPolicy.RUN_ONE,
                weightedBehaviors);
    }
}
