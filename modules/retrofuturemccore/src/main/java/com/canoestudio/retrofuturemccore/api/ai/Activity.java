package com.canoestudio.retrofuturemccore.api.ai;

import java.util.Objects;

public final class Activity {

    public static final Activity CORE = create("core");
    public static final Activity IDLE = create("idle");
    public static final Activity WORK = create("work");
    public static final Activity PLAY = create("play");
    public static final Activity REST = create("rest");
    public static final Activity MEET = create("meet");
    public static final Activity PANIC = create("panic");
    public static final Activity RAID = create("raid");
    public static final Activity PRE_RAID = create("pre_raid");
    public static final Activity HIDE = create("hide");
    public static final Activity FIGHT = create("fight");
    public static final Activity CELEBRATE = create("celebrate");
    public static final Activity ADMIRE_ITEM = create("admire_item");
    public static final Activity AVOID = create("avoid");
    public static final Activity RIDE = create("ride");
    public static final Activity PLAY_DEAD = create("play_dead");
    public static final Activity LONG_JUMP = create("long_jump");
    public static final Activity RAM = create("ram");
    public static final Activity TONGUE = create("tongue");
    public static final Activity SWIM = create("swim");
    public static final Activity LAY_SPAWN = create("lay_spawn");
    public static final Activity SNIFF = create("sniff");
    public static final Activity INVESTIGATE = create("investigate");
    public static final Activity ROAR = create("roar");
    public static final Activity EMERGE = create("emerge");
    public static final Activity DIG = create("dig");

    private final String id;
    private final int hashCode;

    private Activity(String id) {
        this.id = validate(id);
        this.hashCode = this.id.hashCode();
    }

    public static Activity create(String id) {
        return new Activity(id);
    }

    public String getId() {
        return this.id;
    }

    private static String validate(String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity id cannot be empty");
        }
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Activity)) {
            return false;
        }
        Activity other = (Activity) obj;
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
