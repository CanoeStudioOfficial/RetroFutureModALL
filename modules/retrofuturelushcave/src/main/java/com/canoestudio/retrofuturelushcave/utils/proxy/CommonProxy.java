package com.canoestudio.retrofuturelushcave.utils.proxy;

import com.canoestudio.retrofuturelushcave.worldgen.cave.DeepSlateReplacer;
import com.canoestudio.retrofuturelushcave.worldgen.cave.MapGen118Canyon;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.LushCaveAquaticSpawner;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.LushCaveRootSystemDecorator;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.UndergroundCaveFeatureDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(DeepSlateReplacer.class);
        MinecraftForge.EVENT_BUS.register(LushCaveAquaticSpawner.class);
        MinecraftForge.EVENT_BUS.register(LushCaveRootSystemDecorator.class);
        MinecraftForge.EVENT_BUS.register(UndergroundCaveFeatureDecorator.class);

        MinecraftForge.TERRAIN_GEN_BUS.register(MapGen118Canyon.class);
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
