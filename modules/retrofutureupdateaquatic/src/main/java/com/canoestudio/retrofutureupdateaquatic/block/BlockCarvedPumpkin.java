package com.canoestudio.retrofutureupdateaquatic.block;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/**
 * 1.13's separate carved-pumpkin block, represented on the 1.12.2 block API.
 * The parent class supplies the normal pumpkin placement and golem logic; the
 * overridden patterns also make the new block valid for snow/iron golems.
 */
public class BlockCarvedPumpkin extends BlockPumpkin {

    private static final Predicate<IBlockState> IS_CARVED_PUMPKIN = new Predicate<IBlockState>() {
        @Override
        public boolean apply(@Nullable IBlockState state) {
            return state != null && state.getBlock() instanceof BlockCarvedPumpkin;
        }
    };

    private BlockPattern snowmanPattern;
    private BlockPattern golemPattern;

    public BlockCarvedPumpkin(ResourceLocation registryName) {
        super();
        this.setRegistryName(registryName);
        this.setTranslationKey(registryName.getNamespace() + "." + registryName.getPath());
    }

    @Override
    protected BlockPattern getSnowmanPattern() {
        if (this.snowmanPattern == null) {
            this.snowmanPattern = FactoryBlockPattern.start().aisle("^", "#", "#")
                .where('^', BlockWorldState.hasState(BlockStateMatcher.forBlock(this)))
                .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW)))
                .build();
        }
        return this.snowmanPattern;
    }

    @Override
    protected BlockPattern getGolemPattern() {
        if (this.golemPattern == null) {
            this.golemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~")
                .where('^', BlockWorldState.hasState(BlockStateMatcher.forBlock(this)))
                .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK)))
                .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR)))
                .build();
        }
        return this.golemPattern;
    }
}
