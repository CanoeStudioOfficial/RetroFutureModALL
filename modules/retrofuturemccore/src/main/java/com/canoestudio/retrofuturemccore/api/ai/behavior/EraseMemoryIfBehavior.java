package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class EraseMemoryIfBehavior<E extends EntityLivingBase, T> extends Behavior<E> {

    private final MemoryModuleType<T> memoryType;
    private final Predicate<T> predicate;

    public EraseMemoryIfBehavior(MemoryModuleType<T> memoryType, Predicate<T> predicate) {
        super(requirement(memoryType), 20);
        this.memoryType = memoryType;
        this.predicate = predicate;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        T value = brain.getMemory(this.memoryType).orElse(null);
        if (value != null && this.predicate.test(value)) {
            brain.eraseMemory(this.memoryType);
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirement(MemoryModuleType<?> memoryType) {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(memoryType, MemoryStatus.VALUE_PRESENT);
    }
}
