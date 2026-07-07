package com.canoestudio.retrofuturetrailsandtales.block;

import com.canoestudio.retrofuturemccore.api.block.RetroSignSet;
import com.canoestudio.retrofuturetrailsandtales.RTAT;
import com.canoestudio.retrofuturetrailsandtales.item.ModItems;
import net.minecraft.util.ResourceLocation;

public final class ModSignSets {

    public static final RetroSignSet MANGROVE_SIGNS = RetroSignSet.builder(key("mangrove"))
        .hangingSign(ModBlocks.MANGROVE_HANGING_SIGN, ModBlocks.MANGROVE_WALL_HANGING_SIGN,
            ModItems.MANGROVE_HANGING_SIGN)
        .hangingSignTile(TileEntityHangingSign.class)
        .texture(key("textures/blocks/mangrove_hanging_sign.png"))
        .register();

    private ModSignSets() {
    }

    public static RetroSignSet mangrove() {
        return MANGROVE_SIGNS;
    }

    private static ResourceLocation key(String name) {
        return new ResourceLocation(RTAT.ID, name);
    }
}
