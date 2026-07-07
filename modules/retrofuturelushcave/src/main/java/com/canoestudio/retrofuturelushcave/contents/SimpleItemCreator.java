package com.canoestudio.retrofuturelushcave.contents;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.item.Item;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class SimpleItemCreator extends Item {
    public SimpleItemCreator(String name) {
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setCreativeTab(CREATIVE_TABS);

        ModItems.ITEMS.add(this);
    }
}
