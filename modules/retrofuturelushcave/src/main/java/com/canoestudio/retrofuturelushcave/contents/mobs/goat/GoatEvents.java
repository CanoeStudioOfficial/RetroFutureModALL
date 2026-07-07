package com.canoestudio.retrofuturelushcave.contents.mobs.goat;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class GoatEvents {
    @SubscribeEvent
    public static void entities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        registry.register(
                EntityEntryBuilder.create()
                        .entity(EntityGoat.class)
                        .egg(0xa58d74, 0xd7c69a)
                        .tracker(96, 3, true)
                        .spawn(EnumCreatureType.CREATURE, 8, 2, 4, Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES)
                        .id(new ResourceLocation(Tags.MOD_ID, "goat"), 2)
                        .name("goat")
                        .build()
        );
    }
}
