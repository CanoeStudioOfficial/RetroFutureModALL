package com.canoestudio.retrofuturelushcave.utils.registry;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturemccore.api.block.RetroBlockRegistration;
import com.canoestudio.retrofuturemccore.api.client.model.RetroModelRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.BLOCKITEMS;
import static com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class ContentRegister {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        RetroBlockRegistration.registerBlocks(event.getRegistry(), ModBlocks.BLOCKS);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RetroBlockRegistration.registerItems(event.getRegistry(), ModItems.ITEMS);
        RetroBlockRegistration.registerBlockItems(event.getRegistry(), BLOCKITEMS);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        RetroModelRegistry.registerItems(ModItems.ITEMS);
        RetroModelRegistry.registerItems(BLOCKITEMS);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerModels();
    }
}
