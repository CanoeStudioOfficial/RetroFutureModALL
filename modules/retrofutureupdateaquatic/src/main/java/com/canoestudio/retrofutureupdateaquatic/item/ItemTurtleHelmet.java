package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

public class ItemTurtleHelmet extends ItemArmor {

    public static final ArmorMaterial TURTLE_MATERIAL = EnumHelper.addArmorMaterial(
        RetroFutureUpdateAquatic.ID + ":turtle", RetroFutureUpdateAquatic.ID + ":turtle",
        25, new int[] {2, 5, 6, 2}, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);

    public ItemTurtleHelmet() {
        super(TURTLE_MATERIAL, 0, EntityEquipmentSlot.HEAD);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "turtle_helmet");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".turtle_helmet");
        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    /**
     * Matches the mature aquatic implementation: the shell grants the
     * player a fresh 10-second breathing reserve while out of water, rather
     * than duplicating normal underwater breathing while already submerged.
     */
    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if (!FluidloggedSupport.isEntityInWater(player)) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 200, 0, false, false));
        }
    }
}
