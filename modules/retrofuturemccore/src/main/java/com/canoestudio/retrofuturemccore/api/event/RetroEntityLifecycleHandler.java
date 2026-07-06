package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public interface RetroEntityLifecycleHandler {

    void onEntityJoinWorld(World world, Entity entity);

    void onLivingUpdate(World world, EntityLivingBase entity);

    boolean onLivingHurt(World world, EntityLivingBase entity, DamageSource source, MutableFloat amount);

    boolean onLivingDeath(World world, EntityLivingBase entity, DamageSource source);
}
