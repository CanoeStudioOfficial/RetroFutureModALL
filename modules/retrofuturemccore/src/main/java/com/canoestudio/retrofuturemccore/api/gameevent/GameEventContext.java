package com.canoestudio.retrofuturemccore.api.gameevent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public final class GameEventContext {

    private final Entity sourceEntity;
    private final Entity projectileOwner;
    private final IBlockState affectedState;

    private GameEventContext(Entity sourceEntity, Entity projectileOwner, IBlockState affectedState) {
        this.sourceEntity = sourceEntity;
        this.projectileOwner = projectileOwner;
        this.affectedState = affectedState;
    }

    public static GameEventContext empty() {
        return new GameEventContext(null, null, null);
    }

    public static GameEventContext of(Entity sourceEntity) {
        return new GameEventContext(sourceEntity, null, null);
    }

    public static GameEventContext of(Entity sourceEntity, IBlockState affectedState) {
        return new GameEventContext(sourceEntity, null, affectedState);
    }

    public static GameEventContext of(Entity sourceEntity, Entity projectileOwner, IBlockState affectedState) {
        return new GameEventContext(sourceEntity, projectileOwner, affectedState);
    }

    public Entity getSourceEntity() {
        return this.sourceEntity;
    }

    public Entity getProjectileOwner() {
        return this.projectileOwner;
    }

    public IBlockState getAffectedState() {
        return this.affectedState;
    }
}
