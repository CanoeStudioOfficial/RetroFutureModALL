package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturemccore.api.block.RetroBoatRegistry;
import com.canoestudio.retrofuturemccore.api.block.RetroSignRegistry;
import com.canoestudio.retrofuturethewildupdate.block.ModWoodSets;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.sounds.ModSounds;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public class ModItems {

    public static final ItemWardenEgg WARDEN_EGG = new ItemWardenEgg();
    public static final Item ECHO_SHARD = simpleItem("echo_shard", CreativeTabs.MATERIALS);
    public static final Item DISC_FRAGMENT_5 = simpleItem("disc_fragment_5", CreativeTabs.MISC);
    public static final Item MUSIC_DISC_5 = new ItemWildRecord("music_disc_5", ModSounds.MUSIC_DISC_5);
    public static final Item RECOVERY_COMPASS = simpleItem("recovery_compass", CreativeTabs.TOOLS);
    public static final Item TADPOLE_BUCKET = new ItemTadpoleBucket();
    public static final Item MANGROVE_BOAT = new ItemMangroveBoat();
    public static final Item MANGROVE_HANGING_SIGN = new ItemMangroveHangingSign();
    public static final Item FROG_SPAWN_EGG = new ItemWildSpawnEgg("frog_spawn_egg", com.canoestudio.retrofuturethewildupdate.entity.ModEntities.FROG_NAME);
    public static final Item TADPOLE_SPAWN_EGG = new ItemWildSpawnEgg("tadpole_spawn_egg", com.canoestudio.retrofuturethewildupdate.entity.ModEntities.TADPOLE_NAME);

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            WARDEN_EGG,
            ECHO_SHARD,
            DISC_FRAGMENT_5,
            MUSIC_DISC_5,
            RECOVERY_COMPASS,
            TADPOLE_BUCKET,
            FROG_SPAWN_EGG,
            TADPOLE_SPAWN_EGG
        );
        RetroBoatRegistry.registerItems(event.getRegistry(), ModWoodSets.MANGROVE_BOATS);
        RetroSignRegistry.registerItems(event.getRegistry(), ModWoodSets.MANGROVE_SIGNS);
    }

    private static Item simpleItem(String name, CreativeTabs tab) {
        return new Item()
            .setRegistryName(RTWU.ID, name)
            .setTranslationKey(RTWU.ID + "." + name)
            .setCreativeTab(tab);
    }
}
