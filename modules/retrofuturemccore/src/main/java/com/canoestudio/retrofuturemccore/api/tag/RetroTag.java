package com.canoestudio.retrofuturemccore.api.tag;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.ResourceLocation;

public class RetroTag<T> {

    private final RetroTagKey<T> key;
    private final Set<ResourceLocation> ids = new LinkedHashSet<ResourceLocation>();
    private final Set<T> values = new LinkedHashSet<T>();
    private final Set<Predicate<T>> predicates = new LinkedHashSet<Predicate<T>>();

    RetroTag(RetroTagKey<T> key) {
        this.key = key;
    }

    public RetroTagKey<T> getKey() {
        return this.key;
    }

    public RetroTag<T> addId(ResourceLocation id) {
        this.ids.add(id);
        return this;
    }

    public RetroTag<T> addValue(T value) {
        this.values.add(value);
        return this;
    }

    public RetroTag<T> addPredicate(Predicate<T> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public boolean contains(T value, ResourceLocation id) {
        if (id != null && this.ids.contains(id)) {
            return true;
        }
        if (this.values.contains(value)) {
            return true;
        }
        for (Predicate<T> predicate : this.predicates) {
            if (predicate.test(value)) {
                return true;
            }
        }
        return false;
    }

    public Set<ResourceLocation> getIds() {
        return Collections.unmodifiableSet(this.ids);
    }

    public Set<T> getValues() {
        return Collections.unmodifiableSet(this.values);
    }
}
