package com.canoestudio.retrofuturemc.utils.proxy;

import com.canoestudio.retrofuturemc.contents.items.spyglass.SpyglassHandler;
import com.canoestudio.retrofuturemc.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturemc.contents.mobs.axolotl.RenderAxolotl;
import com.canoestudio.retrofuturemc.contents.mobs.brownmooshrooms.EntityBrownMooshroom;
import com.canoestudio.retrofuturemc.contents.mobs.brownmooshrooms.RenderBrownMooshroom;
import com.canoestudio.retrofuturemc.contents.mobs.goat.EntityGoat;
import com.canoestudio.retrofuturemc.contents.mobs.goat.RenderGoat;
import com.canoestudio.retrofuturemc.contents.mobs.glowsquid.EntityGlowSquid;
import com.canoestudio.retrofuturemc.contents.mobs.glowsquid.RenderGlowSquid;
import com.canoestudio.retrofuturemc.utils.PowderSnowHudHandler;
import com.canoestudio.retrofuturemc.utils.RetroFutureClientCoreIntegration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, RenderBrownMooshroom::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        MinecraftForge.EVENT_BUS.register(SpyglassHandler.class);
        MinecraftForge.EVENT_BUS.register(PowderSnowHudHandler.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        RetroFutureClientCoreIntegration.register();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

}
