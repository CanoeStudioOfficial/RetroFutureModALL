package com.canoestudio.retrofuturemccore.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public abstract class AbstractRetroUseItem implements RetroUseItem {

    @Override
    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand, ItemStack stack) {
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
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
        return stack.getMaxItemUseDuration();
    }
}
