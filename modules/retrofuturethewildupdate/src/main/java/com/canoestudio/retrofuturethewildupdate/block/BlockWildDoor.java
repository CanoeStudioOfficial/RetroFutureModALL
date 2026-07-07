package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Random;

public class BlockWildDoor extends BlockDoor {

    public BlockWildDoor(String name, Material material, SoundType soundType,
                         float hardness, float resistance, CreativeTabs tab) {
        super(material);
        this.setRegistryName(RTWU.ID, name);
        this.setTranslationKey(RTWU.ID + "." + name);
        this.setCreativeTab(tab);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
    }

    @Override
    public Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? net.minecraft.init.Items.AIR : Item.getItemFromBlock(this);
    }

    @Override
    public ItemStack getItem(World worldIn, net.minecraft.util.math.BlockPos pos,
                             net.minecraft.block.state.IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }
}
