package com.canoestudio.retrofuturemccore.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public interface RetroUseItem {

    boolean matches(ItemStack stack);

    ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand, ItemStack stack);

    void onUseTick(World world, EntityLivingBase entity, ItemStack stack, int remainingUseTicks);

    void onUseStop(World world, EntityLivingBase entity, ItemStack stack, int remainingUseTicks);

    ItemStack onUseFinish(World world, EntityLivingBase entity, ItemStack stack, ItemStack result);

    int getUseDuration(ItemStack stack);
}
