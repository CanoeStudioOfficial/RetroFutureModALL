package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockBlueIce extends Block {

    public BlockBlueIce() {
        super(Material.PACKED_ICE, MapColor.LIGHT_BLUE);
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "blue_ice");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".blue_ice");
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setSoundType(SoundType.GLASS);
        this.setHardness(2.8F);
        this.setResistance(2.8F);
        this.slipperiness = 0.989F;
    }
}
