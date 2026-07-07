package com.canoestudio.retrofuturebuzzybees.proxy;

import com.canoestudio.retrofuturebuzzybees.RetroFutureBuzzyBees;
import net.minecraft.util.ResourceLocation;

public class CommonProxy {

    public void preInit() {
    }

    public void init() {
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RetroFutureBuzzyBees.ID, name);
    }
}
