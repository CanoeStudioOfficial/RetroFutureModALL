package com.canoestudio.retrofuturemccore.api.ai.behavior;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public interface BehaviorControl<E extends EntityLivingBase> {

    Behavior.Status getStatus();

    Set<MemoryModuleType<?>> getRequiredMemories();

    boolean tryStart(WorldServer world, Brain<? extends E> brain, E entity, long gameTime);

    void tickOrStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime);

    void doStop(WorldServer world, Brain<? extends E> brain, E entity, long gameTime);

    String debugString();
}
