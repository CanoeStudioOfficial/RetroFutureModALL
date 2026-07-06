package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import java.util.Collections;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class SetMemoryBehavior<E extends EntityLivingBase, T> extends Behavior<E> {

    private final MemoryModuleType<T> memoryType;
    private final T value;
    private final long expiry;

    public SetMemoryBehavior(MemoryModuleType<T> memoryType, T value) {
        this(memoryType, value, -1L);
    }

    public SetMemoryBehavior(MemoryModuleType<T> memoryType, T value, long expiry) {
        super(Collections.emptyMap(), 20);
        this.memoryType = memoryType;
        this.value = value;
        this.expiry = expiry;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        if (this.expiry >= 0L) {
            brain.setMemoryWithExpiry(this.memoryType, this.value, this.expiry);
        } else {
            brain.setMemory(this.memoryType, this.value);
        }
    }
}
