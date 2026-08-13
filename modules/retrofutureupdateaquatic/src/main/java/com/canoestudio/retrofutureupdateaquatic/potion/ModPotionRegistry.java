package com.canoestudio.retrofutureupdateaquatic.potion;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModPotionRegistry {

    /*
     * ObjectHolder fields are populated only after all registry events have
     * been fired.  Keep the instances used while creating PotionTypes here so
     * that their PotionEffects never capture a null Potion.
     */
    private static final Potion DOLPHINS_GRACE = new RetroAquaticPotion(
        false, 8954814, "effect.retrofutureupdateaquatic.dolphins_grace", true);
    private static final Potion CONDUIT_POWER = new RetroAquaticPotion(
        false, 1950417, "effect.retrofutureupdateaquatic.conduit_power", true);
    private static final Potion SLOW_FALLING = new RetroAquaticPotion(
        false, 16773073, "effect.retrofutureupdateaquatic.slow_falling", true);

    private ModPotionRegistry() {
    }

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(
            DOLPHINS_GRACE.setRegistryName(RetroFutureUpdateAquatic.ID, "dolphins_grace"),
            CONDUIT_POWER.setRegistryName(RetroFutureUpdateAquatic.ID, "conduit_power"),
            SLOW_FALLING.setRegistryName(RetroFutureUpdateAquatic.ID, "slow_falling")
        );
    }

    @SubscribeEvent
    public static void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
        event.getRegistry().registerAll(
            new PotionType("slow_falling", new PotionEffect(SLOW_FALLING, 1800))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "slow_falling"),
            new PotionType("slow_falling", new PotionEffect(SLOW_FALLING, 4800))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "long_slow_falling"),
            new PotionType("turtle_master",
                new PotionEffect(MobEffects.SLOWNESS, 400, 3),
                new PotionEffect(MobEffects.RESISTANCE, 400, 2))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "turtle_master"),
            new PotionType("turtle_master",
                new PotionEffect(MobEffects.SLOWNESS, 800, 3),
                new PotionEffect(MobEffects.RESISTANCE, 800, 2))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "long_turtle_master"),
            new PotionType("turtle_master",
                new PotionEffect(MobEffects.SLOWNESS, 400, 5),
                new PotionEffect(MobEffects.RESISTANCE, 400, 3))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "strong_turtle_master")
        );
    }
}
