package com.canoestudio.retrofuturenetherupdate.proxy;

import com.canoestudio.retrofuturenetherupdate.RetroFutureNetherUpdate;
import net.minecraft.util.ResourceLocation;

public class CommonProxy {

    public void preInit() {
    }

    public void init() {
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RetroFutureNetherUpdate.ID, name);
    }
}
