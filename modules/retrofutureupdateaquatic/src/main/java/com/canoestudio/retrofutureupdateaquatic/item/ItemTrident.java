package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.enchantment.ModEnchantments;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityThrownTrident;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
        int riptide = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.RIPTIDE, stack);
        if (riptide > 0 && !canRiptide(worldIn, playerIn)) {
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

        int riptide = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.RIPTIDE, stack);
        if (riptide > 0 && canRiptide(worldIn, player)) {
            launchRiptide(worldIn, player, stack, riptide);
        } else if (!worldIn.isRemote) {
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            if (!player.capabilities.isCreativeMode) {
                thrownStack.setItemDamage(Math.min(thrownStack.getMaxDamage(), thrownStack.getItemDamage() + 1));
            }
            EntityThrownTrident trident = new EntityThrownTrident(worldIn, player, thrownStack);
            trident.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
            worldIn.spawnEntity(trident);
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
        target.attackEntityFrom(DamageSource.causeMobDamage(attacker), getAttackDamage(stack, target));
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1 && this.isDamageable();
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == ModEnchantments.LOYALTY
            || enchantment == ModEnchantments.IMPALING
            || enchantment == ModEnchantments.RIPTIDE
            || enchantment == ModEnchantments.CHANNELING
            || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    private static float getAttackDamage(ItemStack stack, EntityLivingBase target) {
        return 8.0F + getImpalingBonus(stack, target);
    }

    public static float getThrownDamage(ItemStack stack, EntityLivingBase target) {
        return 8.0F + getImpalingBonus(stack, target);
    }

    private static float getImpalingBonus(ItemStack stack, EntityLivingBase target) {
        int impaling = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.IMPALING, stack);
        if (impaling <= 0) {
            return 0.0F;
        }
        return target.isInWater() || target.isWet() ? impaling * 2.5F : 0.0F;
    }

    private static boolean canRiptide(World world, EntityPlayer player) {
        return player.isInWater() || player.isWet() || world.isRainingAt(new BlockPos(player));
    }

    private static void launchRiptide(World world, EntityPlayer player, ItemStack stack, int level) {
        Vec3d look = player.getLookVec();
        float strength = 2.5F + level * 0.75F;
        player.motionX = look.x * strength;
        player.motionY = look.y * strength;
        player.motionZ = look.z * strength;
        player.velocityChanged = true;
        player.fallDistance = 0.0F;
        if (!world.isRemote) {
            stack.damageItem(1, player);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_SPLASH,
            SoundCategory.PLAYERS, 1.0F, 1.0F + level * 0.1F);
    }
}
