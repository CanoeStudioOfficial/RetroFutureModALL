package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryStatus;
import com.canoestudio.retrofuturemccore.api.ai.util.LookTarget;
import java.util.Collections;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class LookAtTargetSink<E extends EntityLiving> extends Behavior<E> {

    private final float yawSpeed;
    private final float pitchSpeed;

    public LookAtTargetSink(float yawSpeed, float pitchSpeed) {
        super(requirements(), Integer.MAX_VALUE);
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
    }

    @Override
    protected boolean canStillUse(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        return brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(WorldServer world, Brain<? extends E> brain, E entity, long gameTime) {
        LookTarget target = (LookTarget) brain.getMemory(MemoryModuleType.LOOK_TARGET).orElse(null);
        if (target != null) {
            Vec3d position = target.currentPosition();
            entity.getLookHelper().setLookPosition(position.x, position.y, position.z, this.yawSpeed, this.pitchSpeed);
        }
    }

    private static Map<MemoryModuleType<?>, MemoryStatus> requirements() {
        return Collections.<MemoryModuleType<?>, MemoryStatus>singletonMap(MemoryModuleType.LOOK_TARGET,
                MemoryStatus.VALUE_PRESENT);
    }
}
