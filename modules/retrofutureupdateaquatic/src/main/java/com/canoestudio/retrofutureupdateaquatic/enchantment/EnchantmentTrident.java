package com.canoestudio.retrofutureupdateaquatic.enchantment;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class EnchantmentTrident extends Enchantment {

    public enum Kind {
        LOYALTY("loyalty", Rarity.UNCOMMON, 3, 5, 8),
        IMPALING("impaling", Rarity.RARE, 5, 1, 8),
        RIPTIDE("riptide", Rarity.RARE, 3, 10, 7),
        CHANNELING("channeling", Rarity.VERY_RARE, 1, 25, 25);

        private final String name;
        private final Rarity rarity;
        private final int maxLevel;
        private final int minBase;
        private final int minStep;

        Kind(String name, Rarity rarity, int maxLevel, int minBase, int minStep) {
            this.name = name;
            this.rarity = rarity;
            this.maxLevel = maxLevel;
            this.minBase = minBase;
            this.minStep = minStep;
        }
    }

    private final Kind kind;

    public EnchantmentTrident(Kind kind) {
        super(kind.rarity, EnumEnchantmentType.BREAKABLE, new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND});
        this.kind = kind;
        this.setRegistryName(RetroFutureUpdateAquatic.ID, kind.name);
        this.setName(RetroFutureUpdateAquatic.ID + "." + kind.name);
    }

    public Kind getKind() {
        return this.kind;
    }

    @Override
    public int getMaxLevel() {
        return this.kind.maxLevel;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return this.kind.minBase + (enchantmentLevel - 1) * this.kind.minStep;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 20;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return stack.getItem() == ModItems.TRIDENT;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canApply(stack);
    }

    @Override
    protected boolean canApplyTogether(Enchantment ench) {
        if (ench instanceof EnchantmentTrident) {
            Kind other = ((EnchantmentTrident) ench).getKind();
            if (this.kind == Kind.RIPTIDE) {
                return other != Kind.LOYALTY && other != Kind.CHANNELING && other != Kind.RIPTIDE;
            }
            if (this.kind == Kind.LOYALTY || this.kind == Kind.CHANNELING) {
                return other != Kind.RIPTIDE && other != this.kind;
            }
            return other != this.kind;
        }
        return super.canApplyTogether(ench);
    }
}
