package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockWildTrapDoor extends BlockTrapDoor {

    public BlockWildTrapDoor(String name, Material material, SoundType soundType,
                             float hardness, float resistance, CreativeTabs tab) {
        super(material);
        this.setRegistryName(RTWU.ID, name);
        this.setTranslationKey(RTWU.ID + "." + name);
        this.setCreativeTab(tab);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
    }
}
