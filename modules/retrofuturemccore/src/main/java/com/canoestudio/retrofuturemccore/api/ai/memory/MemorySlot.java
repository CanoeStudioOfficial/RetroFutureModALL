package com.canoestudio.retrofuturemccore.api.ai.memory;

import com.canoestudio.retrofuturemccore.api.ai.Brain;

public class MemorySlot<T> {

    public static final long NEVER_EXPIRE = Long.MAX_VALUE;

    private T value;
    private long timeToLive = NEVER_EXPIRE;

    private MemorySlot() {
    }

    public static <T> MemorySlot<T> create() {
        return new MemorySlot<T>();
    }

    public void tick() {
        if (this.hasValue() && this.canExpire()) {
            if (this.hasExpired()) {
                this.clear();
            } else {
                --this.timeToLive;
            }
        }
    }

    public void set(T value) {
        this.set(value, NEVER_EXPIRE);
    }

    public void set(T value, long timeToLive) {
        this.value = value;
        this.timeToLive = timeToLive;
    }

    public void clear() {
        this.value = null;
        this.timeToLive = NEVER_EXPIRE;
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public T value() {
        return this.value;
    }

    public boolean canExpire() {
        return this.timeToLive != NEVER_EXPIRE;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    public long timeToLive() {
        return this.timeToLive;
    }

    public void visit(MemoryModuleType<?> type, Brain.Visitor visitor) {
        visitMemorySlot(type, this, visitor);
    }

    @SuppressWarnings("unchecked")
    private static <U> void visitMemorySlot(MemoryModuleType<?> type, MemorySlot<U> slot, Brain.Visitor visitor) {
        MemoryModuleType<U> typedType = (MemoryModuleType<U>) type;
        if (slot.value == null) {
            visitor.acceptEmpty(typedType);
        } else if (slot.canExpire()) {
            visitor.accept(typedType, slot.value, slot.timeToLive);
        } else {
            visitor.accept(typedType, slot.value);
        }
    }

    @Override
    public String toString() {
        if (this.value == null) {
            return "<empty>";
        }
        String text = String.valueOf(this.value);
        return this.canExpire() ? text + " (ttl: " + this.timeToLive + ")" : text;
    }
}
