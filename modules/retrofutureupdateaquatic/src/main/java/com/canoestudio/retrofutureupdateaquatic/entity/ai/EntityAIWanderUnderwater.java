package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Water-only wander goal copied from Oceanic Expanse's mature 1.12.2 fish AI.
 * The block-material checks are routed through Fluidlogged API so waterlogged
 * blocks are valid navigation targets as well.
 */
public class EntityAIWanderUnderwater extends EntityAIWander {

    private final boolean forceOffFloor;
    private final int wanderXZ;
    private final int wanderY;

    public EntityAIWanderUnderwater(EntityCreature creatureIn, double speedIn, int chance,
            boolean forceOffFloorIn) {
        this(creatureIn, speedIn, chance, forceOffFloorIn, 10, 7);
    }

    public EntityAIWanderUnderwater(EntityCreature creatureIn, double speedIn, int chance,
            boolean forceOffFloorIn, int wanderXZIn, int wanderYIn) {
        super(creatureIn, speedIn, chance);
        this.wanderXZ = wanderXZIn;
        this.wanderY = wanderYIn;
        this.forceOffFloor = forceOffFloorIn;
    }

    @Override
    public boolean shouldExecute() {
        BlockPos entityPos = new BlockPos(this.entity.posX, this.entity.posY, this.entity.posZ);
        if (this.forceOffFloor && FluidloggedSupport.isEntityInWater(this.entity)
                && FluidloggedSupport.isWater(this.entity.world, entityPos.down())
                && !this.entity.world.isAirBlock(entityPos.up())) {
            this.makeUpdate();
        }

        if (!FluidloggedSupport.isEntityInWater(this.entity)) {
            return false;
        }
        return super.shouldExecute();
    }

    @Override
    protected Vec3d getPosition() {
        Vec3d position = RandomPositionGenerator.findRandomTarget(this.entity, this.wanderXZ, this.wanderY);
        for (int tries = 0; tries <= 10 && position != null; tries++) {
            if (FluidloggedSupport.isWater(this.entity.world, new BlockPos(position))) {
                return position;
            }
            position = RandomPositionGenerator.findRandomTarget(this.entity, this.wanderXZ, this.wanderY);
        }
        return null;
    }
}
