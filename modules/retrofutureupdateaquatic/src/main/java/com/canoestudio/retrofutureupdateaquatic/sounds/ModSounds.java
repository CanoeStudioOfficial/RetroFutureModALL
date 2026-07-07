package com.canoestudio.retrofutureupdateaquatic.sounds;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModSounds {

    public static final SoundEvent PHANTOM_AMBIENT = createEvent("entity.phantom.ambient");
    public static final SoundEvent PHANTOM_BITE = createEvent("entity.phantom.bite");
    public static final SoundEvent PHANTOM_DEATH = createEvent("entity.phantom.death");
    public static final SoundEvent PHANTOM_FLAP = createEvent("entity.phantom.flap");
    public static final SoundEvent PHANTOM_HURT = createEvent("entity.phantom.hurt");
    public static final SoundEvent PHANTOM_SWOOP = createEvent("entity.phantom.swoop");

    private ModSounds() {
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        registry.registerAll(PHANTOM_AMBIENT, PHANTOM_BITE, PHANTOM_DEATH, PHANTOM_FLAP, PHANTOM_HURT,
            PHANTOM_SWOOP);
    }

    private static SoundEvent createEvent(String sound) {
        ResourceLocation name = RetroFutureUpdateAquatic.prefix(sound);
        return new SoundEvent(name).setRegistryName(name);
    }
}
