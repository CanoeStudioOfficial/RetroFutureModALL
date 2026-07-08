package com.canoestudio.retrofuturemccore.api.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidloggableBlock;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

public class RetroTrapDoorBlock extends BlockTrapDoor implements RetroFluidloggableBlock {

    public RetroTrapDoorBlock(String modid, String name, Material material, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        this(new ResourceLocation(modid, name), material, soundType, hardness, resistance, tab);
    }

    public RetroTrapDoorBlock(ResourceLocation name, Material material, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        super(material);
        this.setRegistryName(name);
        this.setTranslationKey(name.getNamespace() + "." + name.getPath());
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(tab);
        this.useNeighborBrightness = true;
    }
}
