package com.canoestudio.retrofuturemccore.api.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

public class RetroPressurePlateBlock extends BlockPressurePlate {

    public RetroPressurePlateBlock(String modid, String name, Material material, Sensitivity sensitivity,
            SoundType soundType, float hardness, float resistance, CreativeTabs tab) {
        this(new ResourceLocation(modid, name), material, sensitivity, soundType, hardness, resistance, tab);
    }

    public RetroPressurePlateBlock(ResourceLocation name, Material material, Sensitivity sensitivity,
            SoundType soundType, float hardness, float resistance, CreativeTabs tab) {
        super(material, sensitivity);
        this.setRegistryName(name);
        this.setTranslationKey(name.getNamespace() + "." + name.getPath());
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(tab);
    }
}
