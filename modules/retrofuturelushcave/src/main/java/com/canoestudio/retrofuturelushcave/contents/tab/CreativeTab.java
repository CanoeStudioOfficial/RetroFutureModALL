package com.canoestudio.retrofuturelushcave.contents.tab;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTab {
    public static final CreativeTabs CREATIVE_TABS = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.Glow_Berries, 1);
        }
    };
}
