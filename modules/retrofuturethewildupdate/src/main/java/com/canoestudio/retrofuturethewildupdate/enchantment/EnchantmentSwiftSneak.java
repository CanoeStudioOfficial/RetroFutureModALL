package com.canoestudio.retrofuturethewildupdate.enchantment;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentSwiftSneak extends Enchantment {

    public EnchantmentSwiftSneak() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR_LEGS, new EntityEquipmentSlot[] {EntityEquipmentSlot.LEGS});
        this.setRegistryName(RTWU.ID, "swift_sneak");
        this.setName(RTWU.ID + ".swift_sneak");
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 25 + enchantmentLevel * 25;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }
}
