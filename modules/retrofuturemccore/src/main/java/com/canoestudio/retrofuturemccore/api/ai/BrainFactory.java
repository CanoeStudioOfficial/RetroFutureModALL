package com.canoestudio.retrofuturemccore.api.ai;

import net.minecraft.entity.EntityLivingBase;

public interface BrainFactory<E extends EntityLivingBase> {

    Brain<E> createBrain(E entity);
}
