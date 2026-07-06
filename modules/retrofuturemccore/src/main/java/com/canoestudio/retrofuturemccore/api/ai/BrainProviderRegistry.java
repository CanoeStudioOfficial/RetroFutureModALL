package com.canoestudio.retrofuturemccore.api.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;

public final class BrainProviderRegistry {

    private static final Map<Class<? extends EntityLivingBase>, BrainFactory<? extends EntityLivingBase>> FACTORIES =
            new LinkedHashMap<Class<? extends EntityLivingBase>, BrainFactory<? extends EntityLivingBase>>();

    private BrainProviderRegistry() {
    }

    public static synchronized <E extends EntityLivingBase> void register(Class<E> entityClass,
            BrainFactory<? super E> factory) {
        FACTORIES.put(entityClass, factory);
    }

    public static synchronized void unregister(Class<? extends EntityLivingBase> entityClass) {
        FACTORIES.remove(entityClass);
    }

    public static synchronized void clear() {
        FACTORIES.clear();
    }

    public static synchronized boolean hasProvider(EntityLivingBase entity) {
        return findFactory(entity.getClass()) != null;
    }

    public static synchronized <E extends EntityLivingBase> Brain<E> createBrain(E entity) {
        BrainFactory<? super E> factory = findFactoryForEntity(entity);
        return factory == null ? null : factory.createBrain(entity);
    }

    @SuppressWarnings("unchecked")
    private static <E extends EntityLivingBase> BrainFactory<? super E> findFactoryForEntity(E entity) {
        return (BrainFactory<? super E>) findFactory(entity.getClass());
    }

    private static BrainFactory<? extends EntityLivingBase> findFactory(Class<?> entityClass) {
        Class<?> current = entityClass;
        while (current != null && EntityLivingBase.class.isAssignableFrom(current)) {
            BrainFactory<? extends EntityLivingBase> factory = FACTORIES.get(current);
            if (factory != null) {
                return factory;
            }
            current = current.getSuperclass();
        }

        for (Map.Entry<Class<? extends EntityLivingBase>, BrainFactory<? extends EntityLivingBase>> entry
                : FACTORIES.entrySet()) {
            if (entry.getKey().isAssignableFrom(entityClass)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
