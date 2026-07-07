package com.canoestudio.retrofutureupdateaquatic;

import com.canoestudio.retrofutureupdateaquatic.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
    dependencies = "required-after:retrofuturemccore@[1.0.0,);required-after:fluidlogged_api")
public class RetroFutureUpdateAquatic {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final String ID = Tags.MOD_ID;

    @SidedProxy(
        clientSide = "com.canoestudio.retrofutureupdateaquatic.proxy.ClientProxy",
        serverSide = "com.canoestudio.retrofutureupdateaquatic.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance(Tags.MOD_ID)
    public static RetroFutureUpdateAquatic instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Loading {}", Tags.MOD_NAME);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(ID, name);
    }
}
