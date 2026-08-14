package com.canoestudio.retrofuturemccore.api.block;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class RetroPressurePlateBlock extends BlockPressurePlate implements IFluidloggable {

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

    @Override
    public boolean isFluidValid(net.minecraft.block.state.IBlockState state, World world, BlockPos pos,
            Fluid fluid) {
        return FluidloggedSupport.isWater(fluid);
    }
}
