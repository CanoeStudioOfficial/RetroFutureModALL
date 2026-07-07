package com.canoestudio.retrofuturetrailsandtales.proxy;

import com.canoestudio.retrofuturelushcavecore.api.client.model.RetroModelRegistry;
import com.canoestudio.retrofuturetrailsandtales.RTAT;
import com.canoestudio.retrofuturetrailsandtales.block.BlockMangroveHangingSign;
import com.canoestudio.retrofuturetrailsandtales.block.BlockMangroveWallHangingSign;
import com.canoestudio.retrofuturetrailsandtales.block.ModBlocks;
import com.canoestudio.retrofuturetrailsandtales.block.TileEntityHangingSign;
import com.canoestudio.retrofuturetrailsandtales.client.renderer.RenderHangingSign;
import com.canoestudio.retrofuturetrailsandtales.item.ModItems;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RTAT.ID)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        registerBlockStateMappers();
        RetroModelRegistry.registerTileEntityRenderer(TileEntityHangingSign.class, new RenderHangingSign());
    }

    private static void registerBlockStateMappers() {
        ModelLoader.setCustomStateMapper(
            ModBlocks.MANGROVE_HANGING_SIGN,
            new StateMap.Builder().ignore(BlockMangroveHangingSign.ROTATION).build()
        );
        ModelLoader.setCustomStateMapper(
            ModBlocks.MANGROVE_WALL_HANGING_SIGN,
            new StateMap.Builder().ignore(BlockMangroveWallHangingSign.FACING).build()
        );
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        RetroModelRegistry.registerItems(ModItems.MANGROVE_HANGING_SIGN);
    }
}
