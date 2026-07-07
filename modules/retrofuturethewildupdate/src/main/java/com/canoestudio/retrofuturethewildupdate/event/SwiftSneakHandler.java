package com.canoestudio.retrofuturethewildupdate.event;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RTWU.ID)
public final class SwiftSneakHandler {

    private SwiftSneakHandler() {
    }

    @SubscribeEvent
    public static void onInputUpdate(InputUpdateEvent event) {
        if (!event.getMovementInput().sneak || event.getEntityPlayer().capabilities.isFlying) {
            return;
        }

        ItemStack legs = event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SWIFT_SNEAK, legs);
        if (level <= 0) {
            return;
        }

        float targetMultiplier = Math.min(0.3F + 0.15F * level, 1.0F);
        float correction = targetMultiplier / 0.3F;
        event.getMovementInput().moveStrafe *= correction;
        event.getMovementInput().moveForward *= correction;
    }
}
