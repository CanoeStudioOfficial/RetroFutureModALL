package com.canoestudio.retrofuturemccore.api.event;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class AbstractRetroDropHandler implements RetroDropHandler {

    @Override
    public boolean onLivingDrops(World world, EntityLivingBase entity, DamageSource source, List<EntityItem> drops,
            int lootingLevel, boolean recentlyHit) {
        return false;
    }
}
