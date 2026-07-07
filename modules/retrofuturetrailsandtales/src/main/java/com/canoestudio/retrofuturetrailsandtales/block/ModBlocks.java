package com.canoestudio.retrofuturetrailsandtales.block;

import com.canoestudio.retrofuturemccore.api.block.RetroSignRegistry;
import com.canoestudio.retrofuturetrailsandtales.RTAT;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RTAT.ID)
public final class ModBlocks {

    public static final Block MANGROVE_HANGING_SIGN = new BlockMangroveHangingSign();
    public static final Block MANGROVE_WALL_HANGING_SIGN = new BlockMangroveWallHangingSign();

    private static final Block[] BLOCKS = {
        MANGROVE_HANGING_SIGN,
        MANGROVE_WALL_HANGING_SIGN
    };

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModSignSets.mangrove();
        event.getRegistry().registerAll(BLOCKS);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        RetroSignRegistry.registerItems(event.getRegistry(), ModSignSets.MANGROVE_SIGNS);
    }

    public static void registerTileEntities() {
        RetroSignRegistry.registerTileEntity(TileEntityHangingSign.class, key("hanging_sign"));
    }

    private static ResourceLocation key(String name) {
        return new ResourceLocation(RTAT.ID, name);
    }

    private ModBlocks() {
    }
}
