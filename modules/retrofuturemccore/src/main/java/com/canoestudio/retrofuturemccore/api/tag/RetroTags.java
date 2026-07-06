package com.canoestudio.retrofuturemccore.api.tag;

import com.canoestudio.retrofuturemccore.Tags;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class RetroTags {

    public static final RetroTagKey<Item> GOAT_HORN_INSTRUMENTS = item("goat_horn_instruments");
    public static final RetroTagKey<Item> ZOOM_ITEMS = item("zoom_items");
    public static final RetroTagKey<Item> BRUSH_ITEMS = item("brush_items");
    public static final RetroTagKey<Block> COPPER_BLOCKS = block("copper_blocks");
    public static final RetroTagKey<Block> WAXED_COPPER_BLOCKS = block("waxed_copper_blocks");
    public static final RetroTagKey<Class<? extends Entity>> AXOLOTL_HUNT_TARGETS = entity("axolotl_hunt_targets");
    public static final RetroTagKey<RetroGameEvent> VIBRATIONS = gameEvent("vibrations");
    public static final RetroTagKey<RetroGameEvent> WARDEN_CAN_LISTEN = gameEvent("warden_can_listen");
    public static final RetroTagKey<RetroGameEvent> IGNORE_VIBRATIONS_SNEAKING = gameEvent("ignore_vibrations_sneaking");

    private RetroTags() {
    }

    public static RetroTagKey<Item> item(String path) {
        return RetroTagKey.of(RetroTagDomain.ITEM, new ResourceLocation(Tags.MOD_ID, path));
    }

    public static RetroTagKey<Block> block(String path) {
        return RetroTagKey.of(RetroTagDomain.BLOCK, new ResourceLocation(Tags.MOD_ID, path));
    }

    public static RetroTagKey<Class<? extends Entity>> entity(String path) {
        return RetroTagKey.of(RetroTagDomain.ENTITY, new ResourceLocation(Tags.MOD_ID, path));
    }

    public static RetroTagKey<RetroGameEvent> gameEvent(String path) {
        return RetroTagKey.of(RetroTagDomain.GAME_EVENT, new ResourceLocation(Tags.MOD_ID, path));
    }
}
