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

    private ModPotionRegistry() {
    }

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(
            new RetroAquaticPotion(false, 8954814, "effect.retrofutureupdateaquatic.dolphins_grace", true)
                .setRegistryName(RetroFutureUpdateAquatic.ID, "dolphins_grace"),
            new RetroAquaticPotion(false, 1950417, "effect.retrofutureupdateaquatic.conduit_power", true)
                .setRegistryName(RetroFutureUpdateAquatic.ID, "conduit_power"),
            new RetroAquaticPotion(false, 16773073, "effect.retrofutureupdateaquatic.slow_falling", true)
                .setRegistryName(RetroFutureUpdateAquatic.ID, "slow_falling")
        );
    }

    @SubscribeEvent
    public static void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
        event.getRegistry().registerAll(
            new PotionType("slow_falling", new PotionEffect(ModPotions.SLOW_FALLING, 1800))
                .setRegistryName(RetroFutureUpdateAquatic.ID, "slow_falling"),
            new PotionType("slow_falling", new PotionEffect(ModPotions.SLOW_FALLING, 4800))
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
