package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
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
}
