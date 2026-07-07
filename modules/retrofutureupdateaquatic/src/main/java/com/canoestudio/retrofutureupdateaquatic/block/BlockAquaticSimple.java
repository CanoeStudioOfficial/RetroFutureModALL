package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockAquaticSimple extends Block {

    public BlockAquaticSimple(String name, Material material, MapColor mapColor, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        super(material, mapColor);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, name);
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name);
        this.setSoundType(soundType);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setCreativeTab(tab);
    }
}
