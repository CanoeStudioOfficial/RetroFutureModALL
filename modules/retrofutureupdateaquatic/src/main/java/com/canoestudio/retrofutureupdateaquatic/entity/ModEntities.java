package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntityRegistry;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModEntities {

    private static int entityId = 0;

    private ModEntities() {
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();
        RetroEntityRegistry.builder(EntityAquaticFish.Cod.class, id("cod"), entityId++)
            .factory(EntityAquaticFish.Cod::new)
            .name(RetroFutureUpdateAquatic.ID + ".cod")
            .tracker(64, 3, true)
            .egg(0xc1a76a, 0xe5c48b)
            .register(registry);
        RetroEntityRegistry.builder(EntityAquaticFish.Salmon.class, id("salmon"), entityId++)
            .factory(EntityAquaticFish.Salmon::new)
            .name(RetroFutureUpdateAquatic.ID + ".salmon")
            .tracker(64, 3, true)
            .egg(0xa00d13, 0xead4c8)
            .register(registry);
        RetroEntityRegistry.builder(EntityAquaticFish.Pufferfish.class, id("pufferfish"), entityId++)
            .factory(EntityAquaticFish.Pufferfish::new)
            .name(RetroFutureUpdateAquatic.ID + ".pufferfish")
            .tracker(64, 3, true)
            .egg(0xf6d558, 0x37a68b)
            .register(registry);
        RetroEntityRegistry.builder(EntityAquaticFish.Tropical.class, id("tropical_fish"), entityId++)
            .factory(EntityAquaticFish.Tropical::new)
            .name(RetroFutureUpdateAquatic.ID + ".tropical_fish")
            .tracker(64, 3, true)
            .egg(0xff9b00, 0x00a8ff)
            .register(registry);
        RetroEntityRegistry.builder(EntityDolphin.class, id("dolphin"), entityId++)
            .factory(EntityDolphin::new)
            .name(RetroFutureUpdateAquatic.ID + ".dolphin")
            .tracker(80, 3, true)
            .egg(0x223b4d, 0xd2e8e8)
            .register(registry);
        RetroEntityRegistry.builder(EntityDrowned.class, id("drowned"), entityId++)
            .factory(EntityDrowned::new)
            .name(RetroFutureUpdateAquatic.ID + ".drowned")
            .tracker(80, 3, true)
            .egg(0x4c655f, 0x799c65)
            .register(registry);
        RetroEntityRegistry.builder(EntityTurtle.class, id("turtle"), entityId++)
            .factory(EntityTurtle::new)
            .name(RetroFutureUpdateAquatic.ID + ".turtle")
            .tracker(80, 3, true)
            .egg(0xe7e7d7, 0x41a552)
            .register(registry);
        RetroEntityRegistry.builder(EntityPhantom.class, id("phantom"), entityId++)
            .factory(EntityPhantom::new)
            .name(RetroFutureUpdateAquatic.ID + ".phantom")
            .tracker(80, 3, true)
            .egg(0x43518a, 0x88fffb)
            .register(registry);
        RetroEntityRegistry.builder(EntityThrownTrident.class, id("trident"), entityId++)
            .factory(EntityThrownTrident::new)
            .name(RetroFutureUpdateAquatic.ID + ".trident")
            .tracker(96, 1, true)
            .register(registry);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(RetroFutureUpdateAquatic.ID, path);
    }
}
