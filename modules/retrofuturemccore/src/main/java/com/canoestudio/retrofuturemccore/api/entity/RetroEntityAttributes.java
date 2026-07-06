package com.canoestudio.retrofuturemccore.api.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public final class RetroEntityAttributes {

    private RetroEntityAttributes() {
    }

    public static IAttributeInstance getOrRegister(EntityLivingBase entity, IAttribute attribute) {
        IAttributeInstance instance = entity.getEntityAttribute(attribute);
        return instance != null ? instance : entity.getAttributeMap().registerAttribute(attribute);
    }

    public static void setBaseValue(EntityLivingBase entity, IAttribute attribute, double value) {
        getOrRegister(entity, attribute).setBaseValue(value);
    }
}
