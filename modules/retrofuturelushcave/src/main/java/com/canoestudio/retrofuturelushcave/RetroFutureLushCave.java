package com.canoestudio.retrofuturelushcave;

import com.canoestudio.retrofuturelushcave.commands.CommandLocateLushCave;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.utils.RetroFutureBehaviorEvents;
import com.canoestudio.retrofuturelushcave.utils.RetroFutureCoreIntegration;
import com.canoestudio.retrofuturelushcave.utils.proxy.CommonProxy;
import com.canoestudio.retrofuturelushcave.worldgen.cave.MapGen118Canyon;
import com.canoestudio.retrofuturelushcave.worldgen.cave.DeepSlateReplacer;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.LushCaveAquaticSpawner;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.LushCaveRootSystemDecorator;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.UndergroundCaveFeatureDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:mixinbooter@[11.12,);after:fluidlogged_api@[3.3.2,);after:retrofuturemccore@[1.0.0,);")
public class RetroFutureLushCave {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @SidedProxy(clientSide = "com.canoestudio.retrofuturelushcave.utils.proxy.ClientProxy", serverSide = "com.canoestudio.retrofuturelushcave.utils.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        MinecraftForge.EVENT_BUS.register(DeepSlateReplacer.class);
        MinecraftForge.EVENT_BUS.register(LushCaveAquaticSpawner.class);
        MinecraftForge.EVENT_BUS.register(LushCaveRootSystemDecorator.class);
        MinecraftForge.EVENT_BUS.register(UndergroundCaveFeatureDecorator.class);

        MinecraftForge.TERRAIN_GEN_BUS.register(MapGen118Canyon.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        if (Loader.isModLoaded("retrofuturemccore")) {
            RetroFutureCoreIntegration.register();
            MinecraftForge.EVENT_BUS.register(new RetroFutureBehaviorEvents());
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) { proxy.postInit(event); }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandLocateLushCave());
    }
}
