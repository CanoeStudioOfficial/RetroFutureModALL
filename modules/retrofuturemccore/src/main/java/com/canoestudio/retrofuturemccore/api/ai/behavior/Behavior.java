package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public abstract class Behavior<E extends EntityLivingBase> implements BehaviorControl<E> {

    public static final int DEFAULT_DURATION = 60;

    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Status status = Status.STOPPED;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    protected Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition) {
        this(entryCondition, DEFAULT_DURATION);
    }

    protected Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int timeOutDuration) {
        this(entryCondition, timeOutDuration, timeOutDuration);
    }

    protected Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int minDuration, int maxDuration) {
        if (minDuration < 0 || maxDuration < minDuration) {
            throw new IllegalArgumentException("Invalid behavior duration range: " + minDuration + " - " + maxDuration);
        }
        this.entryCondition = new LinkedHashMap<MemoryModuleType<?>, MemoryStatus>(entryCondition);
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return Collections.unmodifiableSet(this.entryCondition.keySet());
    }

    @Override
    public final boolean tryStart(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        if (this.hasRequiredMemories(brain) && this.checkExtraStartConditions(world, brain, entity)) {
            this.status = Status.RUNNING;
            int duration = this.minDuration;
            if (this.maxDuration > this.minDuration) {
                duration += world.rand.nextInt(this.maxDuration + 1 - this.minDuration);
            }
            this.endTimestamp = gameTime + (long) duration;
            this.start(world, brain, entity, gameTime);
            return true;
        }
        return false;
    }

    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
    }

    @Override
    public final void tickOrStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        if (!this.timedOut(gameTime) && this.canStillUse(world, brain, entity, gameTime)) {
            this.tick(world, brain, entity, gameTime);
        } else {
            this.doStop(world, brain, entity, gameTime);
        }
    }

    protected void tick(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
    }

    @Override
    public final void doStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        this.status = Status.STOPPED;
        this.stop(world, brain, entity, gameTime);
    }

    protected void stop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
    }

    protected boolean canStillUse(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        return false;
    }

    protected boolean timedOut(long gameTime) {
        return gameTime > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(WorldServer world, Brain<? extends E> brain, E entity) {
        return true;
    }

    protected boolean hasRequiredMemories(Brain<? extends E> brain) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            if (!brain.checkMemory(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    public enum Status {
        STOPPED,
        RUNNING
    }
}
