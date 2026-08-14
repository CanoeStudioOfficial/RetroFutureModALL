package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** The 1.13 pufferfish food, including its intentionally dangerous effects. */
public class ItemPufferfish extends ItemFood {

    public ItemPufferfish() {
        super(1, 0.1F, false);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "pufferfish");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".pufferfish");
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.FOOD);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (!worldIn.isRemote) {
            player.addPotionEffect(new PotionEffect(MobEffects.POISON, 1200, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 300, 2));
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 0));
        }
    }
}
