package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import net.minecraft.entity.ai.EntityAIWander;

/** Land wandering counterpart to the reference turtle AI. */
public final class EntityAITurtleWanderLand extends EntityAIWander {

    private final EntityTurtle turtle;

    public EntityAITurtleWanderLand(EntityTurtle turtle, double speed, int chance) {
        super(turtle, speed, chance);
        this.turtle = turtle;
    }

    @Override
    public boolean shouldExecute() {
        return !this.turtle.hasEgg() && !FluidloggedSupport.isEntityInWater(this.turtle)
            && super.shouldExecute();
    }
}
