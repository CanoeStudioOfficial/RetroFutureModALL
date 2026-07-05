package com.canoestudio.retrofuturemc.contents.mobs.glowsquid;

import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class GlowSquidEvents {
    @SubscribeEvent
    public static void entities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        registry.register(
                EntityEntryBuilder.create()
                        .entity(EntityGlowSquid.class)
                        .egg(0x095b6a, 0x7df9ff)
                        .tracker(80, 3, true)
                        .id(new ResourceLocation(Tags.MOD_ID, "glow_squid"), 3)
                        .name("glow_squid")
                        .build()
        );
    }
}
