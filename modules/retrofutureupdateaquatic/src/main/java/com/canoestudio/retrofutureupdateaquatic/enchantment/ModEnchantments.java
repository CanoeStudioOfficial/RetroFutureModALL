package com.canoestudio.retrofutureupdateaquatic.enchantment;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModEnchantments {

    public static final Enchantment LOYALTY = new EnchantmentTrident(EnchantmentTrident.Kind.LOYALTY);
    public static final Enchantment IMPALING = new EnchantmentTrident(EnchantmentTrident.Kind.IMPALING);
    public static final Enchantment RIPTIDE = new EnchantmentTrident(EnchantmentTrident.Kind.RIPTIDE);
    public static final Enchantment CHANNELING = new EnchantmentTrident(EnchantmentTrident.Kind.CHANNELING);

    private ModEnchantments() {
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(LOYALTY, IMPALING, RIPTIDE, CHANNELING);
    }
}
