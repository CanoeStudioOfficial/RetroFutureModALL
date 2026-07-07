package com.canoestudio.retrofuturelushcave.contents.items;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class ItemRetroFutureRecord extends ItemRecord {
    public ItemRetroFutureRecord(String name, SoundEvent sound) {
        super(name, sound);
        setTranslationKey(Tags.MOD_ID + "." + name);
        setRegistryName(name);
        setCreativeTab(CREATIVE_TABS);
        ModItems.ITEMS.add(this);
    }
}
