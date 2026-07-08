package com.canoestudio.retrofutureupdateaquatic.potion;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class RetroAquaticPotion extends Potion {

    public RetroAquaticPotion(boolean badEffect, int color, String name, boolean beneficial) {
        super(badEffect, color);
        this.setPotionName(name);
        if (beneficial) {
            this.setBeneficial();
        }
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return true;
    }
}
