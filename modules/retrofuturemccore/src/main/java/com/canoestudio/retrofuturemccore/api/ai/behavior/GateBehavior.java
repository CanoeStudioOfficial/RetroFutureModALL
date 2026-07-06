package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class GateBehavior<E extends EntityLivingBase> implements BehaviorControl<E> {

    private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final OrderPolicy orderPolicy;
    private final RunningPolicy runningPolicy;
    private final WeightedBehaviorList<E> behaviors = new WeightedBehaviorList<E>();
    private Behavior.Status status = Behavior.Status.STOPPED;

    public GateBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
            Collection<? extends MemoryModuleType<?>> exitErasedMemories, OrderPolicy orderPolicy,
            RunningPolicy runningPolicy, Collection<WeightedBehavior<E>> behaviors) {
        this.entryCondition = new LinkedHashMap<MemoryModuleType<?>, MemoryStatus>(entryCondition);
        this.exitErasedMemories = new LinkedHashSet<MemoryModuleType<?>>(exitErasedMemories);
        this.orderPolicy = orderPolicy;
        this.runningPolicy = runningPolicy;
        for (WeightedBehavior<E> behavior : behaviors) {
            this.behaviors.add(behavior.getBehavior(), behavior.getWeight());
        }
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        Set<MemoryModuleType<?>> required = new LinkedHashSet<MemoryModuleType<?>>(this.entryCondition.keySet());
        for (BehaviorControl<? super E> behavior : this.behaviors.getBehaviors()) {
            required.addAll(behavior.getRequiredMemories());
        }
        return Collections.unmodifiableSet(required);
    }

    @Override
    public final boolean tryStart(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        if (!this.hasRequiredMemories(brain)) {
            return false;
        }

        this.status = Behavior.Status.RUNNING;
        this.orderPolicy.apply(this.behaviors, world.rand);
        this.runningPolicy.apply(this.behaviors.getBehaviors(), world, brain, entity, gameTime);
        return true;
    }

    @Override
    public final void tickOrStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        for (BehaviorControl<? super E> behavior : this.behaviors.getBehaviors()) {
            if (behavior.getStatus() == Behavior.Status.RUNNING) {
                behavior.tickOrStop(world, brain, entity, gameTime);
            }
        }

        if (!this.hasRunningChild()) {
            this.doStop(world, brain, entity, gameTime);
        }
    }

    @Override
    public final void doStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        this.status = Behavior.Status.STOPPED;
        for (BehaviorControl<? super E> behavior : this.behaviors.getBehaviors()) {
            if (behavior.getStatus() == Behavior.Status.RUNNING) {
                behavior.doStop(world, brain, entity, gameTime);
            }
        }
        for (MemoryModuleType<?> memoryType : this.exitErasedMemories) {
            brain.eraseMemory(memoryType);
        }
    }

    @Override
    public String debugString() {
        List<String> running = new ArrayList<String>();
        for (BehaviorControl<? super E> behavior : this.behaviors.getBehaviors()) {
            if (behavior.getStatus() == Behavior.Status.RUNNING) {
                running.add(behavior.debugString());
            }
        }
        return this.getClass().getSimpleName() + ": " + running;
    }

    private boolean hasRequiredMemories(Brain<? extends E> brain) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            if (!brain.checkMemory(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRunningChild() {
        for (BehaviorControl<? super E> behavior : this.behaviors.getBehaviors()) {
            if (behavior.getStatus() == Behavior.Status.RUNNING) {
                return true;
            }
        }
        return false;
    }

    public enum OrderPolicy {
        ORDERED {
            @Override
            <E extends EntityLivingBase> void apply(WeightedBehaviorList<E> behaviors, Random random) {
            }
        },
        SHUFFLED {
            @Override
            <E extends EntityLivingBase> void apply(WeightedBehaviorList<E> behaviors, Random random) {
                behaviors.shuffle(random);
            }
        };

        abstract <E extends EntityLivingBase> void apply(WeightedBehaviorList<E> behaviors, Random random);
    }

    public enum RunningPolicy {
        RUN_ONE {
            @Override
            <E extends EntityLivingBase> void apply(List<BehaviorControl<? super E>> behaviors, WorldServer world,
                    Brain<? extends E> brain, E entity, long gameTime) {
                for (BehaviorControl<? super E> behavior : behaviors) {
                    if (behavior.getStatus() == Behavior.Status.STOPPED
                            && behavior.tryStart(world, brain, entity, gameTime)) {
                        return;
                    }
                }
            }
        },
        TRY_ALL {
            @Override
            <E extends EntityLivingBase> void apply(List<BehaviorControl<? super E>> behaviors, WorldServer world,
                    Brain<? extends E> brain, E entity, long gameTime) {
                for (BehaviorControl<? super E> behavior : behaviors) {
                    if (behavior.getStatus() == Behavior.Status.STOPPED) {
                        behavior.tryStart(world, brain, entity, gameTime);
                    }
                }
            }
        };

        abstract <E extends EntityLivingBase> void apply(List<BehaviorControl<? super E>> behaviors, WorldServer world,
                Brain<? extends E> brain, E entity, long gameTime);
    }
}
