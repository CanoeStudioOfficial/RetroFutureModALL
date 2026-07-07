package com.canoestudio.retrofuturethewildupdate.enchantment;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public final class ModEnchantments {

    public static final Enchantment SWIFT_SNEAK = new EnchantmentSwiftSneak();

    private ModEnchantments() {
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(SWIFT_SNEAK);
    }
}
