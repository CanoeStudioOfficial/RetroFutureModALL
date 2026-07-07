package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturelushcavecore.api.block.RetroBoatRegistry;
import com.canoestudio.retrofuturelushcavecore.api.entity.RetroEntityRegistry;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public class ModEntities {

    private static int entityId = 0;

    public static final ResourceLocation WARDEN_NAME = new ResourceLocation(RTWU.ID, "warden");
    public static final ResourceLocation ALLAY_NAME = new ResourceLocation(RTWU.ID, "allay");
    public static final ResourceLocation FROG_NAME = new ResourceLocation(RTWU.ID, "frog");
    public static final ResourceLocation TADPOLE_NAME = new ResourceLocation(RTWU.ID, "tadpole");
    public static final ResourceLocation MANGROVE_BOAT_NAME = new ResourceLocation(RTWU.ID, "mangrove_boat");
    public static final ResourceLocation MANGROVE_CHEST_BOAT_NAME = new ResourceLocation(RTWU.ID, "mangrove_chest_boat");

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();
        RetroEntityRegistry.builder(Warden.class, WARDEN_NAME, entityId++)
            .factory(Warden::new)
            .name(RTWU.ID + ".warden")
            .tracker(80, 3, true)
            .register(registry);
        RetroEntityRegistry.builder(EntityAllay.class, ALLAY_NAME, entityId++)
            .factory(EntityAllay::new)
            .name(RTWU.ID + ".allay")
            .tracker(80, 3, true)
            .egg(0x00daff, 0xf2f9ff)
            .register(registry);
        RetroEntityRegistry.builder(EntityFrog.class, FROG_NAME, entityId++)
            .factory(EntityFrog::new)
            .name(RTWU.ID + ".frog")
            .tracker(80, 3, true)
            .egg(0xd07444, 0xffe297)
            .register(registry);
        RetroEntityRegistry.builder(EntityTadpole.class, TADPOLE_NAME, entityId++)
            .factory(EntityTadpole::new)
            .name(RTWU.ID + ".tadpole")
            .tracker(64, 3, true)
            .egg(0x6d5335, 0x160a03)
            .register(registry);
        RetroBoatRegistry.registerBoatEntity(registry, EntityMangroveBoat.class, MANGROVE_BOAT_NAME, entityId++,
            RTWU.ID + ".mangrove_boat", EntityMangroveBoat::new);
        RetroBoatRegistry.registerBoatEntity(registry, EntityMangroveChestBoat.class, MANGROVE_CHEST_BOAT_NAME, entityId++,
            RTWU.ID + ".mangrove_chest_boat", EntityMangroveChestBoat::new);
    }
}
