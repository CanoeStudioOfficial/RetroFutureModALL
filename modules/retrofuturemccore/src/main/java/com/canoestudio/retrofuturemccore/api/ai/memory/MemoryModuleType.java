package com.canoestudio.retrofuturemccore.api.ai.memory;

import java.util.List;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public final class MemoryModuleType<T> {

    public static final MemoryModuleType<Void> DUMMY = create("dummy");
    public static final MemoryModuleType<List<EntityLivingBase>> NEAREST_LIVING_ENTITIES = create("mobs");
    public static final MemoryModuleType<List<EntityPlayer>> NEAREST_PLAYERS = create("nearest_players");
    public static final MemoryModuleType<EntityPlayer> NEAREST_VISIBLE_PLAYER = create("nearest_visible_player");
    public static final MemoryModuleType<EntityPlayer> NEAREST_VISIBLE_ATTACKABLE_PLAYER =
            create("nearest_visible_targetable_player");
    public static final MemoryModuleType<Object> WALK_TARGET = create("walk_target");
    public static final MemoryModuleType<Object> LOOK_TARGET = create("look_target");
    public static final MemoryModuleType<EntityLivingBase> ATTACK_TARGET = create("attack_target");
    public static final MemoryModuleType<Boolean> ATTACK_COOLING_DOWN = create("attack_cooling_down");
    public static final MemoryModuleType<EntityLivingBase> INTERACTION_TARGET = create("interaction_target");
    public static final MemoryModuleType<EntityLivingBase> BREED_TARGET = create("breed_target");
    public static final MemoryModuleType<Entity> RIDE_TARGET = create("ride_target");
    public static final MemoryModuleType<DamageSource> HURT_BY = create("hurt_by");
    public static final MemoryModuleType<EntityLivingBase> HURT_BY_ENTITY = create("hurt_by_entity");
    public static final MemoryModuleType<EntityLivingBase> AVOID_TARGET = create("avoid_target");
    public static final MemoryModuleType<EntityLivingBase> NEAREST_HOSTILE = create("nearest_hostile");
    public static final MemoryModuleType<EntityLivingBase> NEAREST_ATTACKABLE = create("nearest_attackable");
    public static final MemoryModuleType<Boolean> IS_PANICKING = create("is_panicking");
    public static final MemoryModuleType<EntityPlayer> TEMPTING_PLAYER = create("tempting_player");
    public static final MemoryModuleType<Integer> TEMPTATION_COOLDOWN_TICKS = create("temptation_cooldown_ticks");
    public static final MemoryModuleType<Integer> GAZE_COOLDOWN_TICKS = create("gaze_cooldown_ticks");
    public static final MemoryModuleType<Boolean> IS_TEMPTED = create("is_tempted");
    public static final MemoryModuleType<Integer> LONG_JUMP_COOLDOWN_TICKS = create("long_jump_cooling_down");
    public static final MemoryModuleType<Boolean> LONG_JUMP_MID_JUMP = create("long_jump_mid_jump");
    public static final MemoryModuleType<Boolean> HAS_HUNTING_COOLDOWN = create("has_hunting_cooldown");
    public static final MemoryModuleType<Integer> RAM_COOLDOWN_TICKS = create("ram_cooldown_ticks");
    public static final MemoryModuleType<Vec3d> RAM_TARGET = create("ram_target");
    public static final MemoryModuleType<Boolean> IS_IN_WATER = create("is_in_water");
    public static final MemoryModuleType<Boolean> IS_PREGNANT = create("is_pregnant");
    public static final MemoryModuleType<Integer> PLAY_DEAD_TICKS = create("play_dead_ticks");

    private final String id;
    private final int hashCode;

    private MemoryModuleType(String id) {
        this.id = validate(id);
        this.hashCode = this.id.hashCode();
    }

    public static <T> MemoryModuleType<T> create(String id) {
        return new MemoryModuleType<T>(id);
    }

    public String getId() {
        return this.id;
    }

    private static String validate(String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Memory id cannot be empty");
        }
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemoryModuleType)) {
            return false;
        }
        MemoryModuleType<?> other = (MemoryModuleType<?>) obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
