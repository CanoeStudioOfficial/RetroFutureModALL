package com.canoestudio.retrofuturethewildupdate.sounds;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public final class ModSounds {

    public static final SoundEvent WARDEN_AMBIENT;
    public static final SoundEvent WARDEN_HURT;
    public static final SoundEvent WARDEN_DEATH;
    public static final SoundEvent WARDEN_STEP;
    public static final SoundEvent WARDEN_ROAR;
    public static final SoundEvent WARDEN_SNIFF;
    public static final SoundEvent WARDEN_HEARTBEAT;
    public static final SoundEvent WARDEN_ATTACK_IMPACT;
    public static final SoundEvent WARDEN_NEARBY_CLOSER;
    public static final SoundEvent WARDEN_SONIC_BOOM;
    public static final SoundEvent WARDEN_EMERGE;
    public static final SoundEvent WARDEN_DIG;
    public static final SoundEvent WARDEN_TENDRIL;
    public static final SoundEvent MUSIC_DISC_5;
    public static final SoundEvent ALLAY_AMBIENT_WITH_ITEM;
    public static final SoundEvent ALLAY_AMBIENT_WITHOUT_ITEM;
    public static final SoundEvent ALLAY_HURT;
    public static final SoundEvent ALLAY_DEATH;
    public static final SoundEvent ALLAY_ITEM_GIVEN;
    public static final SoundEvent ALLAY_ITEM_TAKEN;
    public static final SoundEvent ALLAY_THROW;

    private static SoundEvent createEvent(String sound) {
        ResourceLocation name = RTWU.prefix(sound);
        return new SoundEvent(name).setRegistryName(name);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        registry.registerAll(
            WARDEN_AMBIENT,
            WARDEN_HURT,
            WARDEN_DEATH,
            WARDEN_STEP,
            WARDEN_ROAR,
            WARDEN_SNIFF,
            WARDEN_HEARTBEAT,
            WARDEN_ATTACK_IMPACT,
            WARDEN_NEARBY_CLOSER,
            WARDEN_SONIC_BOOM,
            WARDEN_EMERGE,
            WARDEN_DIG,
            WARDEN_TENDRIL,
            MUSIC_DISC_5,
            ALLAY_AMBIENT_WITH_ITEM,
            ALLAY_AMBIENT_WITHOUT_ITEM,
            ALLAY_HURT,
            ALLAY_DEATH,
            ALLAY_ITEM_GIVEN,
            ALLAY_ITEM_TAKEN,
            ALLAY_THROW
        );
    }

    private ModSounds() {
    }

    static {
        WARDEN_AMBIENT = createEvent("mob.warden.ambient");
        WARDEN_HURT = createEvent("mob.warden.hurt");
        WARDEN_DEATH = createEvent("mob.warden.death");
        WARDEN_STEP = createEvent("mob.warden.step");
        WARDEN_ROAR = createEvent("mob.warden.roar");
        WARDEN_SNIFF = createEvent("mob.warden.sniff");
        WARDEN_HEARTBEAT = createEvent("mob.warden.heartbeat");
        WARDEN_ATTACK_IMPACT = createEvent("mob.warden.attack_impact");
        WARDEN_NEARBY_CLOSER = createEvent("mob.warden.nearby_closer");
        WARDEN_SONIC_BOOM = createEvent("mob.warden.sonic_boom");
        WARDEN_EMERGE = createEvent("mob.warden.emerge");
        WARDEN_DIG = createEvent("mob.warden.dig");
        WARDEN_TENDRIL = createEvent("mob.warden.tendril");
        MUSIC_DISC_5 = createEvent("music_disc.5");
        ALLAY_AMBIENT_WITH_ITEM = createEvent("entity.allay.ambient_with_item");
        ALLAY_AMBIENT_WITHOUT_ITEM = createEvent("entity.allay.ambient_without_item");
        ALLAY_HURT = createEvent("entity.allay.hurt");
        ALLAY_DEATH = createEvent("entity.allay.death");
        ALLAY_ITEM_GIVEN = createEvent("entity.allay.item_given");
        ALLAY_ITEM_TAKEN = createEvent("entity.allay.item_taken");
        ALLAY_THROW = createEvent("entity.allay.item_thrown");
    }
}
