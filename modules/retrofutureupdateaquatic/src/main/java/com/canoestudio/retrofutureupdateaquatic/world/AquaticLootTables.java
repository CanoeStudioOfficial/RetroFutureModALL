package com.canoestudio.retrofutureupdateaquatic.world;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

public final class AquaticLootTables {

    public static final ResourceLocation SHIPWRECK_SUPPLY = register("chests/shipwreck_supply");
    public static final ResourceLocation SHIPWRECK_MAP = register("chests/shipwreck_map");
    public static final ResourceLocation SHIPWRECK_TREASURE = register("chests/shipwreck_treasure");
    public static final ResourceLocation OCEAN_RUIN = register("chests/ocean_ruin");
    public static final ResourceLocation BURIED_TREASURE = register("chests/buried_treasure");

    private AquaticLootTables() {
    }

    public static void init() {
    }

    private static ResourceLocation register(String path) {
        return LootTableList.register(RetroFutureUpdateAquatic.prefix(path));
    }
}
