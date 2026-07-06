package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import java.util.Collections;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class CountDownCooldownTicks<E extends EntityLivingBase> extends Behavior<E> {

    private final MemoryModuleType<Integer> cooldownMemory;

    public CountDownCooldownTicks(MemoryModuleType<Integer> cooldownMemory) {
        super(requirement(cooldownMemory), Integer.MAX_VALUE);
        this.cooldownMemory = cooldownMemory;
    }

    @Override
    protected boolean canStillUse(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        return brain.hasMemoryValue(this.cooldownMemory);
    }

    @Override
    protected void tick(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        Integer ticks = brain.getMemory(this.cooldownMemory).orElse(Integer.valueOf(0));
        if (ticks.intValue() <= 1) {
            brain.eraseMemory(this.cooldownMemory);
        } else {
            brain.setMemory(this.cooldownMemory, Integer.valueOf(ticks.intValue() - 1));
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirement(MemoryModuleType<?> cooldownMemory) {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(cooldownMemory, MemoryStatus.VALUE_PRESENT);
    }
}
