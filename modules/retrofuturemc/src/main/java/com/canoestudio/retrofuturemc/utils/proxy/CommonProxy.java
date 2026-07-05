package com.canoestudio.retrofuturemc.utils.proxy;

import com.canoestudio.retrofuturemc.contents.world.gen.RetroFutureWorldGenerator;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {


    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new RetroFutureWorldGenerator(), 20);
        if (Loader.isModLoaded("moderncaveterrain")) {
            RetroFutureWorldGenerator.registerModernCaveTerrainDecorator();
        }

    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
