package com.canoestudio.retrofuturemccore.api.ai.behavior;

import net.minecraft.entity.EntityLivingBase;

public final class WeightedBehavior<E extends EntityLivingBase> {

    private final BehaviorControl<? super E> behavior;
    private final int weight;

    private WeightedBehavior(BehaviorControl<? super E> behavior, int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Behavior weight must be positive");
        }
        this.behavior = behavior;
        this.weight = weight;
    }

    public static <E extends EntityLivingBase> WeightedBehavior<E> of(BehaviorControl<? super E> behavior, int weight) {
        return new WeightedBehavior<E>(behavior, weight);
    }

    public BehaviorControl<? super E> getBehavior() {
        return this.behavior;
    }

    public int getWeight() {
        return this.weight;
    }
}
