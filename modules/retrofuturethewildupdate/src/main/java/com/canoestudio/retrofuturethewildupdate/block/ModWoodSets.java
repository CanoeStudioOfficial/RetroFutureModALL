package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturelushcavecore.api.block.RetroBlockFamily;
import com.canoestudio.retrofuturelushcavecore.api.block.RetroBoatSet;
import com.canoestudio.retrofuturelushcavecore.api.block.RetroSignSet;
import com.canoestudio.retrofuturelushcavecore.api.block.RetroWoodSet;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityMangroveBoat;
import com.canoestudio.retrofuturethewildupdate.entity.EntityMangroveChestBoat;
import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import net.minecraft.util.ResourceLocation;

public final class ModWoodSets {

    public static final RetroBlockFamily MANGROVE_FAMILY = RetroBlockFamily.builder(ModBlocks.MANGROVE_PLANKS)
        .log(ModBlocks.MANGROVE_LOG)
        .strippedLog(ModBlocks.STRIPPED_MANGROVE_LOG)
        .stairs(ModBlocks.MANGROVE_STAIRS)
        .slab(ModBlocks.MANGROVE_SLAB)
        .fence(ModBlocks.MANGROVE_FENCE)
        .fenceGate(ModBlocks.MANGROVE_FENCE_GATE)
        .door(ModBlocks.MANGROVE_DOOR)
        .trapdoor(ModBlocks.MANGROVE_TRAPDOOR)
        .pressurePlate(ModBlocks.MANGROVE_PRESSURE_PLATE)
        .button(ModBlocks.MANGROVE_BUTTON)
        .sign(ModBlocks.MANGROVE_SIGN, ModBlocks.MANGROVE_WALL_SIGN)
        .build();

    public static final RetroSignSet MANGROVE_SIGNS = RetroSignSet.builder(key("mangrove"))
        .sign(ModBlocks.MANGROVE_SIGN, ModBlocks.MANGROVE_WALL_SIGN, ModItems.MANGROVE_SIGN)
        .signTile(TileEntityMangroveSign.class)
        .texture(key("textures/blocks/mangrove_sign.png"))
        .build();

    public static final RetroBoatSet MANGROVE_BOATS = RetroBoatSet.builder(key("mangrove"))
        .boat(ModItems.MANGROVE_BOAT, EntityMangroveBoat.class)
        .chestBoat(ModItems.MANGROVE_CHEST_BOAT, EntityMangroveChestBoat.class)
        .texture(key("textures/entity/boat/mangrove.png"))
        .build();

    public static final RetroWoodSet MANGROVE = RetroWoodSet.builder(key("mangrove"), ModBlocks.MANGROVE_PLANKS)
        .log(ModBlocks.MANGROVE_LOG)
        .strippedLog(ModBlocks.STRIPPED_MANGROVE_LOG)
        .wood(ModBlocks.MANGROVE_WOOD)
        .strippedWood(ModBlocks.STRIPPED_MANGROVE_WOOD)
        .leaves(ModBlocks.MANGROVE_LEAVES)
        .sapling(ModBlocks.MANGROVE_PROPAGULE)
        .block("roots", ModBlocks.MANGROVE_ROOTS)
        .block("muddy_roots", ModBlocks.MUDDY_MANGROVE_ROOTS)
        .family(MANGROVE_FAMILY)
        .signs(MANGROVE_SIGNS)
        .boats(MANGROVE_BOATS)
        .register();

    private ModWoodSets() {
    }

    public static RetroWoodSet mangrove() {
        return MANGROVE;
    }

    private static ResourceLocation key(String name) {
        return new ResourceLocation(RTWU.ID, name);
    }
}
