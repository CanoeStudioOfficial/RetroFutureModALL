package com.canoestudio.retrofuturemccore.api.gameevent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.ResourceLocation;

public final class RetroGameEvent {

    private static final Map<ResourceLocation, RetroGameEvent> REGISTRY =
            new LinkedHashMap<ResourceLocation, RetroGameEvent>();

    public static final RetroGameEvent BLOCK_ATTACH = register("block_attach", 16, 10);
    public static final RetroGameEvent BLOCK_CHANGE = register("block_change", 16, 11);
    public static final RetroGameEvent BLOCK_CLOSE = register("block_close", 16, 9);
    public static final RetroGameEvent BLOCK_DESTROY = register("block_destroy", 16, 12);
    public static final RetroGameEvent BLOCK_DETACH = register("block_detach", 16, 9);
    public static final RetroGameEvent BLOCK_OPEN = register("block_open", 16, 10);
    public static final RetroGameEvent BLOCK_PLACE = register("block_place", 16, 13);
    public static final RetroGameEvent BLOCK_ACTIVATE = register("block_activate", 16, 10);
    public static final RetroGameEvent BLOCK_DEACTIVATE = register("block_deactivate", 16, 9);
    public static final RetroGameEvent BOUNCE = register("bounce", 16, 2);
    public static final RetroGameEvent CONTAINER_CLOSE = register("container_close", 16, 9);
    public static final RetroGameEvent CONTAINER_OPEN = register("container_open", 16, 10);
    public static final RetroGameEvent DRINK = register("drink", 16, 8);
    public static final RetroGameEvent EAT = register("eat", 16, 8);
    public static final RetroGameEvent ELYTRA_GLIDE = register("elytra_glide", 16, 4);
    public static final RetroGameEvent ENTITY_DAMAGE = register("entity_damage", 16, 7);
    public static final RetroGameEvent ENTITY_DIE = register("entity_die", 16, 15);
    public static final RetroGameEvent ENTITY_DISMOUNT = register("entity_dismount", 16, 5);
    public static final RetroGameEvent ENTITY_INTERACT = register("entity_interact", 16, 6);
    public static final RetroGameEvent ENTITY_MOUNT = register("entity_mount", 16, 6);
    public static final RetroGameEvent ENTITY_PLACE = register("entity_place", 16, 14);
    public static final RetroGameEvent ENTITY_ACTION = register("entity_action", 16, 4);
    public static final RetroGameEvent EQUIP = register("equip", 16, 5);
    public static final RetroGameEvent EXPLODE = register("explode", 16, 15);
    public static final RetroGameEvent FLAP = register("flap", 16, 1);
    public static final RetroGameEvent FLUID_PICKUP = register("fluid_pickup", 16, 12);
    public static final RetroGameEvent FLUID_PLACE = register("fluid_place", 16, 13);
    public static final RetroGameEvent HIT_GROUND = register("hit_ground", 16, 2);
    public static final RetroGameEvent INSTRUMENT_PLAY = register("instrument_play", 16, 3);
    public static final RetroGameEvent ITEM_INTERACT_START = register("item_interact_start", 16, 3);
    public static final RetroGameEvent ITEM_INTERACT_FINISH = register("item_interact_finish", 16, 3);
    public static final RetroGameEvent LIGHTNING_STRIKE = register("lightning_strike", 16, 14);
    public static final RetroGameEvent NOTE_BLOCK_PLAY = register("note_block_play", 16, 10);
    public static final RetroGameEvent PRIME_FUSE = register("prime_fuse", 16, 10);
    public static final RetroGameEvent PROJECTILE_LAND = register("projectile_land", 16, 2);
    public static final RetroGameEvent PROJECTILE_SHOOT = register("projectile_shoot", 16, 3);
    public static final RetroGameEvent SHEAR = register("shear", 16, 6);
    public static final RetroGameEvent SHRIEK = register("shriek", 32, 15);
    public static final RetroGameEvent SPLASH = register("splash", 16, 2);
    public static final RetroGameEvent STEP = register("step", 16, 1);
    public static final RetroGameEvent SWIM = register("swim", 16, 1);
    public static final RetroGameEvent TELEPORT = register("teleport", 16, 14);
    public static final RetroGameEvent UNEQUIP = register("unequip", 16, 4);

    private final ResourceLocation id;
    private final int notificationRadius;
    private final int vibrationFrequency;

    private RetroGameEvent(ResourceLocation id, int notificationRadius, int vibrationFrequency) {
        this.id = Objects.requireNonNull(id, "id");
        this.notificationRadius = notificationRadius;
        this.vibrationFrequency = vibrationFrequency;
    }

    public static RetroGameEvent register(String path, int notificationRadius, int vibrationFrequency) {
        return register(new ResourceLocation("minecraft", path), notificationRadius, vibrationFrequency);
    }

    public static synchronized RetroGameEvent register(ResourceLocation id, int notificationRadius,
            int vibrationFrequency) {
        RetroGameEvent existing = REGISTRY.get(id);
        if (existing != null) {
            return existing;
        }
        RetroGameEvent event = new RetroGameEvent(id, notificationRadius, vibrationFrequency);
        REGISTRY.put(id, event);
        return event;
    }

    public static synchronized RetroGameEvent get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public int getNotificationRadius() {
        return this.notificationRadius;
    }

    public int getVibrationFrequency() {
        return this.vibrationFrequency;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }
}
