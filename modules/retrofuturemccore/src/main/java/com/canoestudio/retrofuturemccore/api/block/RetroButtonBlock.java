package com.canoestudio.retrofuturemccore.api.block;

import javax.annotation.Nullable;
import net.minecraft.block.BlockButton;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RetroButtonBlock extends BlockButton {

    private final boolean wooden;

    public RetroButtonBlock(String modid, String name, boolean wooden, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        this(new ResourceLocation(modid, name), wooden, soundType, hardness, resistance, tab);
    }

    public RetroButtonBlock(ResourceLocation name, boolean wooden, SoundType soundType,
            float hardness, float resistance, CreativeTabs tab) {
        super(wooden);
        this.wooden = wooden;
        this.setRegistryName(name);
        this.setTranslationKey(name.getNamespace() + "." + name.getPath());
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        this.setCreativeTab(tab);
    }

    @Override
    protected void playClickSound(@Nullable EntityPlayer player, World worldIn, BlockPos pos) {
        worldIn.playSound(player, pos, this.wooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON
            : SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, this.wooden ? 0.6F : 0.6F);
    }

    @Override
    protected void playReleaseSound(World worldIn, BlockPos pos) {
        worldIn.playSound(null, pos, this.wooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF
            : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, this.wooden ? 0.5F : 0.5F);
    }
}
