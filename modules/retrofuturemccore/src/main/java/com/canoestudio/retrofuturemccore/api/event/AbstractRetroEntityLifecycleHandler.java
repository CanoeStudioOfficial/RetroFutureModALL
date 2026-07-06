package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class AbstractRetroEntityLifecycleHandler implements RetroEntityLifecycleHandler {

    @Override
    public void onEntityJoinWorld(World world, Entity entity) {
    }

    @Override
    public void onLivingUpdate(World world, EntityLivingBase entity) {
    }

    @Override
    public boolean onLivingHurt(World world, EntityLivingBase entity, DamageSource source, MutableFloat amount) {
        return false;
    }

    @Override
    public boolean onLivingDeath(World world, EntityLivingBase entity, DamageSource source) {
        return false;
    }
}
