package com.canoestudio.retrofuturevillageandpillage.proxy;

import com.canoestudio.retrofuturevillageandpillage.RetroFutureVillageAndPillage;
import net.minecraft.util.ResourceLocation;

public class CommonProxy {

    public void preInit() {
    }

    public void init() {
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RetroFutureVillageAndPillage.ID, name);
    }
}
