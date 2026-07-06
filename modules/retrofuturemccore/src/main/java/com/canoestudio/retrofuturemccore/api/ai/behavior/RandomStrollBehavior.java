package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.WalkTarget;
import java.util.Collections;
import java.util.Map;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class RandomStrollBehavior<E extends EntityCreature> extends Behavior<E> {

    private final double speed;
    private final int horizontalRange;
    private final int verticalRange;
    private final float chance;

    public RandomStrollBehavior(double speed) {
        this(speed, 10, 7, 0.001F);
    }

    public RandomStrollBehavior(double speed, int horizontalRange, int verticalRange, float chance) {
        super(requirements(), 20);
        this.speed = speed;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.chance = chance;
    }

    @Override
    protected boolean checkExtraStartConditions(WorldServer world, Brain<? extends E> brain, E entity) {
        return entity.getRNG().nextFloat() < this.chance;
    }

    @Override
    protected void start(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        Vec3d target = RandomPositionGenerator.findRandomTarget(entity, this.horizontalRange, this.verticalRange);
        if (target != null) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, this.speed, 1));
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT);
    }
}
