package com.canoestudio.retrofuturemccore.api.block;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RetroTrapDoorBlock extends BlockTrapDoor implements IFluidloggable {

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

    @Override
    public boolean isFluidValid(IBlockState state, World world, BlockPos pos, Fluid fluid) {
        return FluidloggedSupport.isWater(fluid);
    }
}
