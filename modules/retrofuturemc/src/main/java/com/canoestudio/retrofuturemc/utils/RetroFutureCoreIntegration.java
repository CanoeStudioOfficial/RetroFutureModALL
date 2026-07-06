package com.canoestudio.retrofuturemc.utils;

import com.canoestudio.retrofuturemc.contents.items.ItemGoatHorn;
import com.canoestudio.retrofuturemc.contents.items.ModItems;
import com.canoestudio.retrofuturemccore.api.item.RetroUseItem;
import com.canoestudio.retrofuturemccore.api.item.RetroUseItemRegistry;
import com.canoestudio.retrofuturemccore.api.tag.RetroTagRegistry;
import com.canoestudio.retrofuturemccore.api.tag.RetroTags;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public final class RetroFutureCoreIntegration {
    private static boolean registered;

    private RetroFutureCoreIntegration() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        registerTags();
        registerUseItems();
    }

    private static void registerTags() {
        RetroTagRegistry.addValue(RetroTags.GOAT_HORN_INSTRUMENTS, ModItems.GOAT_HORN);
        RetroTagRegistry.addValue(RetroTags.ZOOM_ITEMS, ModItems.SPYGLASS);
        RetroTagRegistry.addValue(RetroTags.AXOLOTL_HUNT_TARGETS, EntityGuardian.class);
    }

    private static void registerUseItems() {
        RetroUseItemRegistry.register(ModItems.GOAT_HORN, new RetroUseItem() {
            @Override
            public boolean matches(ItemStack stack) {
                return true;
            }

            @Override
            public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand, ItemStack stack) {
                return ItemGoatHorn.useHorn(world, player, hand, stack);
            }

            @Override
            public void onUseTick(World world, EntityLivingBase entity, ItemStack stack, int remainingUseTicks) {
            }

            @Override
            public void onUseStop(World world, EntityLivingBase entity, ItemStack stack, int remainingUseTicks) {
            }

            @Override
            public ItemStack onUseFinish(World world, EntityLivingBase entity, ItemStack stack, ItemStack result) {
                return result;
            }

            @Override
            public int getUseDuration(ItemStack stack) {
                return ItemGoatHorn.USE_DURATION;
            }
        });
    }

}
