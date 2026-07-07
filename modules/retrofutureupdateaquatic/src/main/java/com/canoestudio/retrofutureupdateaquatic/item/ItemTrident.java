package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityThrownTrident;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemTrident extends Item {

    public ItemTrident() {
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "trident");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".trident");
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxStackSize(1);
        this.setMaxDamage(250);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entityLiving;
        int charge = this.getMaxItemUseDuration(stack) - timeLeft;
        if (charge < 10) {
            return;
        }

        if (!worldIn.isRemote) {
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            EntityThrownTrident trident = new EntityThrownTrident(worldIn, player, thrownStack);
            trident.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
            worldIn.spawnEntity(trident);
            stack.damageItem(1, player);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT,
            SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.addStat(StatList.getObjectUseStats(this));
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 8.0F);
        stack.damageItem(1, attacker);
        return true;
    }
}
