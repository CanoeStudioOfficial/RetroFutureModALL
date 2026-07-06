package com.canoestudio.retrofuturemccore.api.ai;

import com.canoestudio.retrofuturemccore.api.ai.behavior.Behavior;
import com.canoestudio.retrofuturemccore.api.ai.behavior.BehaviorControl;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemorySlot;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.sensing.Sensor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public class Brain<E extends EntityLivingBase> {

    private final Map<MemoryModuleType<?>, MemorySlot<?>> memories = new HashMap<MemoryModuleType<?>, MemorySlot<?>>();
    private final List<Sensor<? super E>> sensors = new ArrayList<Sensor<? super E>>();
    private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority =
            new TreeMap<Integer, Map<Activity, Set<BehaviorControl<? super E>>>>();
    private final Map<Activity, Map<MemoryModuleType<?>, MemoryStatus>> activityRequirements =
            new HashMap<Activity, Map<MemoryModuleType<?>, MemoryStatus>>();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped =
            new HashMap<Activity, Set<MemoryModuleType<?>>>();
    private Set<Activity> coreActivities = new LinkedHashSet<Activity>();
    private final Set<Activity> activeActivities = new LinkedHashSet<Activity>();
    private Activity defaultActivity = Activity.IDLE;

    public Brain() {
        this.setCoreActivities(Collections.singleton(Activity.CORE));
        this.useDefaultActivity();
    }

    public <U> Brain<E> registerMemory(MemoryModuleType<U> memoryType) {
        Objects.requireNonNull(memoryType, "memoryType");
        if (!this.memories.containsKey(memoryType)) {
            this.memories.put(memoryType, MemorySlot.<U>create());
        }
        return this;
    }

    public Brain<E> registerMemories(Collection<? extends MemoryModuleType<?>> memoryTypes) {
        for (MemoryModuleType<?> memoryType : memoryTypes) {
            this.registerMemory(memoryType);
        }
        return this;
    }

    public Brain<E> addSensor(Sensor<? super E> sensor) {
        Objects.requireNonNull(sensor, "sensor");
        this.sensors.add(sensor);
        this.registerMemories(sensor.requires());
        return this;
    }

    public Brain<E> addActivity(Activity activity) {
        return this.addActivity(activity, Collections.<MemoryModuleType<?>, MemoryStatus>emptyMap(),
                Collections.<MemoryModuleType<?>>emptySet());
    }

    public Brain<E> addActivity(Activity activity, Map<MemoryModuleType<?>, MemoryStatus> requirements) {
        return this.addActivity(activity, requirements, Collections.<MemoryModuleType<?>>emptySet());
    }

    public Brain<E> addActivity(Activity activity, Map<MemoryModuleType<?>, MemoryStatus> requirements,
            Collection<? extends MemoryModuleType<?>> memoriesToEraseWhenStopped) {
        Objects.requireNonNull(activity, "activity");
        Map<MemoryModuleType<?>, MemoryStatus> copiedRequirements =
                new LinkedHashMap<MemoryModuleType<?>, MemoryStatus>(requirements);
        this.activityRequirements.put(activity, copiedRequirements);
        this.registerMemories(copiedRequirements.keySet());

        if (memoriesToEraseWhenStopped.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.remove(activity);
        } else {
            this.activityMemoriesToEraseWhenStopped.put(activity,
                    new LinkedHashSet<MemoryModuleType<?>>(memoriesToEraseWhenStopped));
        }
        return this;
    }

    public Brain<E> addBehavior(Activity activity, int priority, BehaviorControl<? super E> behavior) {
        Objects.requireNonNull(activity, "activity");
        Objects.requireNonNull(behavior, "behavior");
        this.addActivityIfMissing(activity);
        this.registerMemories(behavior.getRequiredMemories());

        Map<Activity, Set<BehaviorControl<? super E>>> byActivity = this.availableBehaviorsByPriority.get(priority);
        if (byActivity == null) {
            byActivity = new LinkedHashMap<Activity, Set<BehaviorControl<? super E>>>();
            this.availableBehaviorsByPriority.put(priority, byActivity);
        }

        Set<BehaviorControl<? super E>> behaviors = byActivity.get(activity);
        if (behaviors == null) {
            behaviors = new LinkedHashSet<BehaviorControl<? super E>>();
            byActivity.put(activity, behaviors);
        }

        behaviors.add(behavior);
        return this;
    }

    public Brain<E> addBehaviors(Activity activity, int priority,
            Collection<? extends BehaviorControl<? super E>> behaviors) {
        for (BehaviorControl<? super E> behavior : behaviors) {
            this.addBehavior(activity, priority, behavior);
        }
        return this;
    }

    private void addActivityIfMissing(Activity activity) {
        if (!this.activityRequirements.containsKey(activity)) {
            this.addActivity(activity);
        }
    }

    public void clearMemories() {
        for (MemorySlot<?> memory : this.memories.values()) {
            memory.clear();
        }
    }

    public <U> void eraseMemory(MemoryModuleType<U> memoryType) {
        MemorySlot<U> slot = this.getMemorySlotIfPresent(memoryType);
        if (slot != null) {
            slot.clear();
        }
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType, U value) {
        this.setMemoryInternal(memoryType, value, MemorySlot.NEVER_EXPIRE);
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType, Optional<? extends U> optionalValue) {
        this.setMemoryInternal(memoryType, optionalValue.isPresent() ? optionalValue.get() : null,
                MemorySlot.NEVER_EXPIRE);
    }

    public <U> void setMemoryWithExpiry(MemoryModuleType<U> memoryType, U value, long timeToLive) {
        this.setMemoryInternal(memoryType, value, timeToLive);
    }

    private <U> void setMemoryInternal(MemoryModuleType<U> memoryType, U value, long timeToLive) {
        MemorySlot<U> slot = this.getOrCreateMemorySlot(memoryType);
        if (isEmptyCollection(value)) {
            slot.clear();
        } else if (value == null) {
            slot.clear();
        } else {
            slot.set(value, timeToLive);
        }
    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> memoryType) {
        MemorySlot<U> slot = this.getMemorySlotIfPresent(memoryType);
        return slot == null ? Optional.<U>empty() : Optional.ofNullable(slot.value());
    }

    public <U> long getTimeUntilExpiry(MemoryModuleType<U> memoryType) {
        MemorySlot<U> slot = this.getMemorySlotIfPresent(memoryType);
        return slot == null ? MemorySlot.NEVER_EXPIRE : slot.timeToLive();
    }

    public <U> boolean isMemoryValue(MemoryModuleType<U> memoryType, U value) {
        MemorySlot<U> slot = this.getMemorySlotIfPresent(memoryType);
        return slot != null && Objects.equals(value, slot.value());
    }

    public boolean hasMemoryValue(MemoryModuleType<?> memoryType) {
        return this.checkMemory(memoryType, MemoryStatus.VALUE_PRESENT);
    }

    public boolean checkMemory(MemoryModuleType<?> memoryType, MemoryStatus status) {
        MemorySlot<?> slot = this.getMemorySlotIfPresent(memoryType);
        if (slot == null) {
            return false;
        }
        return status == MemoryStatus.REGISTERED
                || status == MemoryStatus.VALUE_PRESENT && slot.hasValue()
                || status == MemoryStatus.VALUE_ABSENT && !slot.hasValue();
    }

    public void forEachMemory(Visitor visitor) {
        for (Map.Entry<MemoryModuleType<?>, MemorySlot<?>> entry : this.memories.entrySet()) {
            entry.getValue().visit(entry.getKey(), visitor);
        }
    }

    public void setCoreActivities(Collection<Activity> activities) {
        this.coreActivities = new LinkedHashSet<Activity>(activities);
    }

    public Set<Activity> getActiveActivities() {
        return Collections.unmodifiableSet(this.activeActivities);
    }

    public Optional<Activity> getActiveNonCoreActivity() {
        for (Activity activity : this.activeActivities) {
            if (!this.coreActivities.contains(activity)) {
                return Optional.of(activity);
            }
        }
        return Optional.empty();
    }

    public boolean isActive(Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public void setDefaultActivity(Activity activity) {
        this.defaultActivity = Objects.requireNonNull(activity, "activity");
    }

    public void useDefaultActivity() {
        this.setActiveActivity(this.defaultActivity);
    }

    public void setActiveActivityIfPossible(Activity activity) {
        if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
        } else {
            this.useDefaultActivity();
        }
    }

    public void setActiveActivityToFirstValid(List<Activity> activities) {
        for (Activity activity : activities) {
            if (this.activityRequirementsAreMet(activity)) {
                this.setActiveActivity(activity);
                return;
            }
        }
    }

    private void setActiveActivity(Activity activity) {
        if (!this.activeActivities.contains(activity)) {
            this.eraseMemoriesForOtherActivitiesThan(activity);
            this.activeActivities.clear();
            this.activeActivities.addAll(this.coreActivities);
            this.activeActivities.add(activity);
        }
    }

    private void eraseMemoriesForOtherActivitiesThan(Activity activity) {
        for (Activity oldActivity : this.activeActivities) {
            if (!oldActivity.equals(activity)) {
                Set<MemoryModuleType<?>> memoriesToErase = this.activityMemoriesToEraseWhenStopped.get(oldActivity);
                if (memoriesToErase != null) {
                    for (MemoryModuleType<?> memoryType : memoriesToErase) {
                        this.eraseMemory(memoryType);
                    }
                }
            }
        }
    }

    private boolean activityRequirementsAreMet(Activity activity) {
        Map<MemoryModuleType<?>, MemoryStatus> requirements = this.activityRequirements.get(activity);
        if (requirements == null) {
            return false;
        }

        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> requirement : requirements.entrySet()) {
            if (!this.checkMemory(requirement.getKey(), requirement.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void tick(WorldServer world, E entity) {
        this.forgetOutdatedMemories();
        this.tickSensors(world, entity);
        this.startEachNonRunningBehavior(world, entity);
        this.tickEachRunningBehavior(world, entity);
    }

    private void tickSensors(WorldServer world, E entity) {
        for (Sensor<? super E> sensor : this.sensors) {
            sensor.tick(world, this, entity);
        }
    }

    private void forgetOutdatedMemories() {
        for (MemorySlot<?> memory : this.memories.values()) {
            memory.tick();
        }
    }

    public void stopAll(WorldServer world, E entity) {
        long gameTime = world.getTotalWorldTime();
        for (BehaviorControl<? super E> behavior : this.getRunningBehaviors()) {
            behavior.doStop(world, this, entity, gameTime);
        }
    }

    private void startEachNonRunningBehavior(WorldServer world, E entity) {
        long gameTime = world.getTotalWorldTime();
        for (Map<Activity, Set<BehaviorControl<? super E>>> behaviorsByActivity
                : this.availableBehaviorsByPriority.values()) {
            for (Map.Entry<Activity, Set<BehaviorControl<? super E>>> behaviorsForActivity
                    : behaviorsByActivity.entrySet()) {
                if (this.activeActivities.contains(behaviorsForActivity.getKey())) {
                    for (BehaviorControl<? super E> behavior : behaviorsForActivity.getValue()) {
                        if (behavior.getStatus() == Behavior.Status.STOPPED) {
                            behavior.tryStart(world, this, entity, gameTime);
                        }
                    }
                }
            }
        }
    }

    private void tickEachRunningBehavior(WorldServer world, E entity) {
        long gameTime = world.getTotalWorldTime();
        for (BehaviorControl<? super E> behavior : this.getRunningBehaviors()) {
            behavior.tickOrStop(world, this, entity, gameTime);
        }
    }

    public List<BehaviorControl<? super E>> getRunningBehaviors() {
        List<BehaviorControl<? super E>> runningBehaviors = new ArrayList<BehaviorControl<? super E>>();
        for (Map<Activity, Set<BehaviorControl<? super E>>> behaviorsByActivity
                : this.availableBehaviorsByPriority.values()) {
            for (Set<BehaviorControl<? super E>> behaviors : behaviorsByActivity.values()) {
                for (BehaviorControl<? super E> behavior : behaviors) {
                    if (behavior.getStatus() == Behavior.Status.RUNNING) {
                        runningBehaviors.add(behavior);
                    }
                }
            }
        }
        return runningBehaviors;
    }

    public void removeAllBehaviors() {
        this.availableBehaviorsByPriority.clear();
    }

    public boolean isBrainDead() {
        return this.memories.isEmpty() && this.sensors.isEmpty() && this.availableBehaviorsByPriority.isEmpty();
    }

    private <U> MemorySlot<U> getOrCreateMemorySlot(MemoryModuleType<U> memoryType) {
        this.registerMemory(memoryType);
        return this.getMemorySlotIfPresent(memoryType);
    }

    @SuppressWarnings("unchecked")
    private <U> MemorySlot<U> getMemorySlotIfPresent(MemoryModuleType<U> memoryType) {
        return (MemorySlot<U>) this.memories.get(memoryType);
    }

    private static boolean isEmptyCollection(Object value) {
        return value instanceof Collection && ((Collection<?>) value).isEmpty();
    }

    public interface Visitor {

        <U> void acceptEmpty(MemoryModuleType<U> type);

        <U> void accept(MemoryModuleType<U> type, U value);

        <U> void accept(MemoryModuleType<U> type, U value, long timeToLive);
    }
}
