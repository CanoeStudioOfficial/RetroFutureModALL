package com.canoestudio.retrofutureupdateaquatic.potion;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(RetroFutureUpdateAquatic.ID)
public final class ModPotions {

    @GameRegistry.ObjectHolder("dolphins_grace")
    public static final Potion DOLPHINS_GRACE = null;
    @GameRegistry.ObjectHolder("conduit_power")
    public static final Potion CONDUIT_POWER = null;
    @GameRegistry.ObjectHolder("slow_falling")
    public static final Potion SLOW_FALLING = null;

    @GameRegistry.ObjectHolder("slow_falling")
    public static final PotionType SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("long_slow_falling")
    public static final PotionType LONG_SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("turtle_master")
    public static final PotionType TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("long_turtle_master")
    public static final PotionType LONG_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("strong_turtle_master")
    public static final PotionType STRONG_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("splash_slow_falling")
    public static final PotionType SPLASH_SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("long_splash_slow_falling")
    public static final PotionType LONG_SPLASH_SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("lingering_slow_falling")
    public static final PotionType LINGERING_SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("long_lingering_slow_falling")
    public static final PotionType LONG_LINGERING_SLOW_FALLING_TYPE = null;
    @GameRegistry.ObjectHolder("splash_turtle_master")
    public static final PotionType SPLASH_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("long_splash_turtle_master")
    public static final PotionType LONG_SPLASH_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("strong_splash_turtle_master")
    public static final PotionType STRONG_SPLASH_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("lingering_turtle_master")
    public static final PotionType LINGERING_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("long_lingering_turtle_master")
    public static final PotionType LONG_LINGERING_TURTLE_MASTER_TYPE = null;
    @GameRegistry.ObjectHolder("strong_lingering_turtle_master")
    public static final PotionType STRONG_LINGERING_TURTLE_MASTER_TYPE = null;

    public static PotionEffect slowFalling(int duration) {
        return new PotionEffect(SLOW_FALLING, duration, 0, true, true);
    }

    public static PotionEffect dolphinsGrace(int duration) {
        return new PotionEffect(DOLPHINS_GRACE, duration, 0, true, true);
    }

    public static PotionEffect conduitPower(int duration) {
        return new PotionEffect(CONDUIT_POWER, duration, 0, true, true);
    }

    private ModPotions() {
    }
}
