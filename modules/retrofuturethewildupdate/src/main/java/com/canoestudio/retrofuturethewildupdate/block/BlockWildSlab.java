package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public abstract class BlockWildSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    public BlockWildSlab(String name, Material material, SoundType soundType, float hardness,
                         float resistance, CreativeTabs tab) {
        super(material);
        this.setRegistryName(RTWU.ID, name);
        this.setTranslationKey(RTWU.ID + "." + name);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(soundType);
        if (!this.isDouble()) {
            this.setCreativeTab(tab);
        }

        IBlockState state = this.blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
            this.useNeighborBrightness = true;
        }
        this.setDefaultState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        if (!this.isDouble()) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public String getTranslationKey(int meta) {
        return this.getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP) {
            return 8;
        }
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble()
            ? new BlockStateContainer(this, VARIANT)
            : new BlockStateContainer(this, VARIANT, HALF);
    }

    public enum Variant implements IStringSerializable {
        DEFAULT("default");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public static class Single extends BlockWildSlab {
        public Single(String name, Material material, SoundType soundType, float hardness,
                      float resistance, CreativeTabs tab) {
            super(name, material, soundType, hardness, resistance, tab);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static class Double extends BlockWildSlab {
        public Double(String name, Material material, SoundType soundType, float hardness,
                      float resistance, CreativeTabs tab) {
            super(name, material, soundType, hardness, resistance, tab);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }
}
