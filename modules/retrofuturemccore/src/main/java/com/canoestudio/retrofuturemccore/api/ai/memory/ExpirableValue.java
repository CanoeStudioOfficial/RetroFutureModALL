package com.canoestudio.retrofuturemccore.api.ai.memory;

import java.util.Optional;

public final class ExpirableValue<T> {

    public static final long NEVER_EXPIRE = Long.MAX_VALUE;

    private final T value;
    private final long timeToLive;

    private ExpirableValue(T value, long timeToLive) {
        this.value = value;
        this.timeToLive = timeToLive;
    }

    public static <T> ExpirableValue<T> of(T value) {
        return new ExpirableValue<T>(value, NEVER_EXPIRE);
    }

    public static <T> ExpirableValue<T> of(T value, long ticksUntilExpiry) {
        return new ExpirableValue<T>(value, ticksUntilExpiry);
    }

    public T value() {
        return this.value;
    }

    public Optional<Long> timeToLive() {
        return this.canExpire() ? Optional.of(this.timeToLive) : Optional.<Long>empty();
    }

    public long rawTimeToLive() {
        return this.timeToLive;
    }

    public boolean canExpire() {
        return this.timeToLive != NEVER_EXPIRE;
    }

    @Override
    public String toString() {
        String text = String.valueOf(this.value);
        return this.canExpire() ? text + " (ttl: " + this.timeToLive + ")" : text;
    }
}
