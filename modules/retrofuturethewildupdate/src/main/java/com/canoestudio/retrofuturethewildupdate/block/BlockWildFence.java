package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockWildFence extends BlockFence {

    public BlockWildFence(String name, Material material, MapColor mapColor, SoundType soundType,
                          float hardness, float resistance, CreativeTabs tab) {
        super(material, mapColor);
        this.setRegistryName(RTWU.ID, name);
        this.setTranslationKey(RTWU.ID + "." + name);
        this.setCreativeTab(tab);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
    }
}
