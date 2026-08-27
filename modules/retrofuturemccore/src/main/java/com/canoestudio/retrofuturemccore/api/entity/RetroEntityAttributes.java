package com.canoestudio.retrofuturemccore.api.entity;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public final class RetroEntityAttributes {

    private RetroEntityAttributes() {
    }

    public static IAttributeInstance getOrRegister(EntityLivingBase entity, IAttribute attribute) {
        if (entity == null || attribute == null) {
            return null;
        }
        IAttributeInstance instance = entity.getEntityAttribute(attribute);
        return instance != null ? instance : entity.getAttributeMap().registerAttribute(attribute);
    }

    /**
     * Registers every attribute in the entity's attribute map if it is not
     * already present. Attributes are attached to entities in 1.12 rather
     * than being entries in a global Forge registry.
     */
    public static void registerAll(EntityLivingBase entity, List<? extends IAttribute> attributes) {
        if (entity == null || attributes == null) {
            return;
        }
        for (IAttribute attribute : attributes) {
            getOrRegister(entity, attribute);
        }
    }

    public static void setBaseValue(EntityLivingBase entity, IAttribute attribute, double value) {
        IAttributeInstance instance = getOrRegister(entity, attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    /**
     * Applies a prepared list of base values and registers missing attributes
     * as needed. This keeps entity setup compact while preserving the normal
     * EntityLivingBase attribute initialization order.
     */
    public static void setBaseValues(EntityLivingBase entity, List<AttributeValue> values) {
        if (entity == null || values == null) {
            return;
        }
        for (AttributeValue value : values) {
            if (value != null) {
                setBaseValue(entity, value.getAttribute(), value.getValue());
            }
        }
    }

    public static AttributeValue value(IAttribute attribute, double value) {
        return new AttributeValue(attribute, value);
    }

    public static final class AttributeValue {
        private final IAttribute attribute;
        private final double value;

        private AttributeValue(IAttribute attribute, double value) {
            this.attribute = attribute;
            this.value = value;
        }

        public IAttribute getAttribute() {
            return attribute;
        }

        public double getValue() {
            return value;
        }
    }
}
