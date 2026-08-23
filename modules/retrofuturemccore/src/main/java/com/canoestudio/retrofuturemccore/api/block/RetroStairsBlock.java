package com.canoestudio.retrofuturemccore.api.block;

import com.canoestudio.retrofuturemccore.api.fluid.RetroFluidloggableBlock;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

public class RetroStairsBlock extends BlockStairs implements RetroFluidloggableBlock {

    public RetroStairsBlock(String modid, String name, IBlockState modelState, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        this(new ResourceLocation(modid, name), modelState, soundType, hardness, resistance, tab);
    }

    public RetroStairsBlock(ResourceLocation name, IBlockState modelState, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        super(modelState);
        this.setRegistryName(name);
        this.setTranslationKey(name.getNamespace() + "." + name.getPath());
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(tab);
        this.useNeighborBrightness = true;
    }
}
