package com.canoestudio.retrofuturemccore.api.ai.sensing;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;

public class NearestPlayersSensor<E extends EntityLivingBase> extends Sensor<E> {

    private final double range;

    public NearestPlayersSensor(double range) {
        super();
        this.range = range;
    }

    public NearestPlayersSensor(int scanRate, double range) {
        super(scanRate);
        this.range = range;
    }

    @Override
    protected void doTick(WorldServer world, Brain<? extends E> brain, final E entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox().grow(this.range, this.range, this.range);
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, box);
        Collections.sort(players, new Comparator<EntityPlayer>() {
            @Override
            public int compare(EntityPlayer first, EntityPlayer second) {
                return Double.compare(entity.getDistanceSq(first), entity.getDistanceSq(second));
            }
        });
        brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, players);
        if (players.isEmpty()) {
            brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        } else {
            brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, players.get(0));
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Collections.<MemoryModuleType<?>>singleton(MemoryModuleType.NEAREST_PLAYERS);
    }
}
