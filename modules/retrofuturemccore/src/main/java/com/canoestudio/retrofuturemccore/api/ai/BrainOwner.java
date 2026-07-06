package com.canoestudio.retrofuturemccore.api.ai;

import net.minecraft.entity.EntityLivingBase;

public interface BrainOwner<E extends EntityLivingBase> {

    Brain<E> getBrain();
}
