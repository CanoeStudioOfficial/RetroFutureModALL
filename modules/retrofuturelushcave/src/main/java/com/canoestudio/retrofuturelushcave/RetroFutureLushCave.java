package com.canoestudio.retrofuturelushcave;

import com.canoestudio.retrofuturelushcave.utils.proxy.CommonProxy;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.utils.RetroFutureBehaviorEvents;
import com.canoestudio.retrofuturelushcave.utils.RetroFutureCoreIntegration;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:retrofuturelushcavecore@[1.0.0,);required-after:moderncaveterrain@[2.1.0,)")
public class RetroFutureLushCave {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @SidedProxy(clientSide = "com.canoestudio.retrofuturelushcave.utils.proxy.ClientProxy", serverSide = "com.canoestudio.retrofuturelushcave.utils.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) { proxy.preInit(event); }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        RetroFutureCoreIntegration.register();
        MinecraftForge.EVENT_BUS.register(new RetroFutureBehaviorEvents());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) { proxy.postInit(event); }
}
