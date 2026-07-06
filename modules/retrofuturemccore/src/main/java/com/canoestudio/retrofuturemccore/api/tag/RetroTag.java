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
    private final Set<RetroTagKey<T>> references = new LinkedHashSet<RetroTagKey<T>>();

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

    public RetroTag<T> addReference(RetroTagKey<T> reference) {
        this.references.add(reference);
        return this;
    }

    public RetroTag<T> clear() {
        this.ids.clear();
        this.values.clear();
        this.predicates.clear();
        this.references.clear();
        return this;
    }

    public boolean contains(T value, ResourceLocation id) {
        return contains(value, id, new LinkedHashSet<RetroTagKey<?>>());
    }

    private boolean contains(T value, ResourceLocation id, Set<RetroTagKey<?>> visiting) {
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
        for (RetroTagKey<T> reference : this.references) {
            if (!visiting.add(reference)) {
                continue;
            }
            RetroTag<T> referencedTag = RetroTagRegistry.get(reference);
            if (referencedTag != null && referencedTag.contains(value, id, visiting)) {
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

    public Set<RetroTagKey<T>> getReferences() {
        return Collections.unmodifiableSet(this.references);
    }
}
