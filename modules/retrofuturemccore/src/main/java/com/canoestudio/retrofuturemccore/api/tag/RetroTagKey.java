package com.canoestudio.retrofuturemccore.api.tag;

import java.util.Objects;
import net.minecraft.util.ResourceLocation;

public final class RetroTagKey<T> {

    private final RetroTagDomain domain;
    private final ResourceLocation id;

    private RetroTagKey(RetroTagDomain domain, ResourceLocation id) {
        this.domain = Objects.requireNonNull(domain, "domain");
        this.id = Objects.requireNonNull(id, "id");
    }

    public static <T> RetroTagKey<T> of(RetroTagDomain domain, ResourceLocation id) {
        return new RetroTagKey<T>(domain, id);
    }

    public RetroTagDomain getDomain() {
        return this.domain;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RetroTagKey)) {
            return false;
        }
        RetroTagKey<?> other = (RetroTagKey<?>) obj;
        return this.domain == other.domain && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31 * this.domain.hashCode() + this.id.hashCode();
    }

    @Override
    public String toString() {
        return "#" + this.domain.name().toLowerCase() + "/" + this.id;
    }
}
