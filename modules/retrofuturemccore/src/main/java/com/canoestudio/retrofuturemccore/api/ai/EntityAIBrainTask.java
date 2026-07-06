package com.canoestudio.retrofuturemccore.api.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.WorldServer;

public class EntityAIBrainTask<E extends EntityLivingBase> extends EntityAIBase {

    private final E entity;
    private Brain<E> brain;
    private boolean brainResolved;

    public EntityAIBrainTask(E entity) {
        this(entity, null);
    }

    public EntityAIBrainTask(E entity, Brain<E> brain) {
        this.entity = entity;
        this.brain = brain;
        this.brainResolved = brain != null;
        this.setMutexBits(0);
    }

    @Override
    public boolean shouldExecute() {
        return this.entity.isEntityAlive() && !this.entity.world.isRemote && this.resolveBrain() != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void updateTask() {
        Brain<E> activeBrain = this.resolveBrain();
        if (activeBrain != null && this.entity.world instanceof WorldServer) {
            activeBrain.tick((WorldServer) this.entity.world, this.entity);
        }
    }

    @Override
    public void resetTask() {
        Brain<E> activeBrain = this.resolveBrain();
        if (activeBrain != null && this.entity.world instanceof WorldServer) {
            activeBrain.stopAll((WorldServer) this.entity.world, this.entity);
        }
    }

    public Brain<E> getBrain() {
        return this.resolveBrain();
    }

    @SuppressWarnings("unchecked")
    private Brain<E> resolveBrain() {
        if (this.brain != null) {
            return this.brain;
        }
        if (this.entity instanceof BrainOwner) {
            this.brain = ((BrainOwner<E>) this.entity).getBrain();
            this.brainResolved = true;
            return this.brain;
        }
        if (!this.brainResolved) {
            this.brain = BrainProviderRegistry.createBrain(this.entity);
            this.brainResolved = true;
        }
        return this.brain;
    }
}
