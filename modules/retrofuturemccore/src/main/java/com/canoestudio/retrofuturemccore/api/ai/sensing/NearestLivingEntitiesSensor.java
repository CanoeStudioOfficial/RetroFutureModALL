package com.canoestudio.retrofuturemccore.api.ai.sensing;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;

public class NearestLivingEntitiesSensor<E extends EntityLivingBase> extends Sensor<E> {

    private final double range;

    public NearestLivingEntitiesSensor(double range) {
        super();
        this.range = range;
    }

    public NearestLivingEntitiesSensor(int scanRate, double range) {
        super(scanRate);
        this.range = range;
    }

    @Override
    protected void doTick(WorldServer world, Brain<? extends E> brain, final E entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox().grow(this.range, this.range, this.range);
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        entities.remove(entity);
        Collections.sort(entities, new Comparator<EntityLivingBase>() {
            @Override
            public int compare(EntityLivingBase first, EntityLivingBase second) {
                return Double.compare(entity.getDistanceSq(first), entity.getDistanceSq(second));
            }
        });
        brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, entities);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Collections.<MemoryModuleType<?>>singleton(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }
}
