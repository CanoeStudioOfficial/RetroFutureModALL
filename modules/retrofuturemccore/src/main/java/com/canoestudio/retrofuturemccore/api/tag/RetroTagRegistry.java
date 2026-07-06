package com.canoestudio.retrofuturemccore.api.tag;

import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class RetroTagRegistry {

    private static final Map<RetroTagKey<?>, RetroTag<?>> TAGS = new LinkedHashMap<RetroTagKey<?>, RetroTag<?>>();

    private RetroTagRegistry() {
    }

    public static <T> RetroTag<T> getOrCreate(RetroTagKey<T> key) {
        RetroTag<T> tag = get(key);
        if (tag == null) {
            tag = new RetroTag<T>(key);
            TAGS.put(key, tag);
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    public static <T> RetroTag<T> get(RetroTagKey<T> key) {
        return (RetroTag<T>) TAGS.get(key);
    }

    public static Collection<RetroTag<?>> getTags() {
        return Collections.unmodifiableCollection(TAGS.values());
    }

    public static <T> void addId(RetroTagKey<T> key, ResourceLocation id) {
        getOrCreate(key).addId(id);
    }

    public static <T> void addValue(RetroTagKey<T> key, T value) {
        getOrCreate(key).addValue(value);
    }

    public static <T> void addPredicate(RetroTagKey<T> key, Predicate<T> predicate) {
        getOrCreate(key).addPredicate(predicate);
    }

    public static <T> void addReference(RetroTagKey<T> key, RetroTagKey<T> reference) {
        getOrCreate(key).addReference(reference);
    }

    public static <T> void clear(RetroTagKey<T> key) {
        getOrCreate(key).clear();
    }

    public static boolean containsItem(RetroTagKey<Item> key, Item item) {
        RetroTag<Item> tag = get(key);
        return tag != null && tag.contains(item, item == null ? null : item.getRegistryName());
    }

    public static boolean containsBlock(RetroTagKey<Block> key, Block block) {
        RetroTag<Block> tag = get(key);
        return tag != null && tag.contains(block, block == null ? null : block.getRegistryName());
    }

    public static boolean containsEntity(RetroTagKey<Class<? extends Entity>> key, Entity entity) {
        return entity != null && containsEntityClass(key, entity.getClass());
    }

    public static boolean containsEntityClass(RetroTagKey<Class<? extends Entity>> key,
            Class<? extends Entity> entityClass) {
        RetroTag<Class<? extends Entity>> tag = get(key);
        if (tag == null || entityClass == null) {
            return false;
        }
        EntityEntry entry = EntityRegistry.getEntry(entityClass);
        ResourceLocation id = entry == null ? null : entry.getRegistryName();
        if (tag.contains(entityClass, id)) {
            return true;
        }
        for (Class<? extends Entity> taggedClass : tag.getValues()) {
            if (taggedClass.isAssignableFrom(entityClass)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsGameEvent(RetroTagKey<RetroGameEvent> key, RetroGameEvent event) {
        RetroTag<RetroGameEvent> tag = get(key);
        return tag != null && tag.contains(event, event == null ? null : event.getId());
    }

    public static Item item(ResourceLocation id) {
        return ForgeRegistries.ITEMS.getValue(id);
    }

    public static Block block(ResourceLocation id) {
        return ForgeRegistries.BLOCKS.getValue(id);
    }
}
