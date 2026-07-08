package com.canoestudio.retrofutureupdateaquatic.proxy;

import com.canoestudio.retrofuturemccore.api.client.model.RetroModelRegistry;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.client.render.RenderAquaticFish;
import com.canoestudio.retrofutureupdateaquatic.client.render.RenderDolphin;
import com.canoestudio.retrofutureupdateaquatic.client.render.RenderDrowned;
import com.canoestudio.retrofutureupdateaquatic.client.render.RenderPhantom;
import com.canoestudio.retrofutureupdateaquatic.client.render.RenderTurtle;
import com.canoestudio.retrofutureupdateaquatic.entity.AquaticFishType;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDolphin;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityThrownTrident;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RetroFutureUpdateAquatic.ID)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        registerBlockStateMappers();
        RetroModelRegistry.registerEntityRenderer(EntityAquaticFish.Cod.class,
            manager -> new RenderAquaticFish<EntityAquaticFish.Cod>(manager, AquaticFishType.COD));
        RetroModelRegistry.registerEntityRenderer(EntityAquaticFish.Salmon.class,
            manager -> new RenderAquaticFish<EntityAquaticFish.Salmon>(manager, AquaticFishType.SALMON));
        RetroModelRegistry.registerEntityRenderer(EntityAquaticFish.Pufferfish.class,
            manager -> new RenderAquaticFish<EntityAquaticFish.Pufferfish>(manager, AquaticFishType.PUFFERFISH));
        RetroModelRegistry.registerEntityRenderer(EntityAquaticFish.Tropical.class,
            manager -> new RenderAquaticFish<EntityAquaticFish.Tropical>(manager, AquaticFishType.TROPICAL_FISH));
        RetroModelRegistry.registerEntityRenderer(EntityDolphin.class, RenderDolphin::new);
        RetroModelRegistry.registerEntityRenderer(EntityDrowned.class, RenderDrowned::new);
        RetroModelRegistry.registerEntityRenderer(EntityTurtle.class, RenderTurtle::new);
        RetroModelRegistry.registerEntityRenderer(EntityPhantom.class, RenderPhantom::new);
        RetroModelRegistry.registerEntityRenderer(EntityThrownTrident.class,
            manager -> new RenderSnowball<EntityThrownTrident>(manager, ModItems.TRIDENT,
                Minecraft.getMinecraft().getRenderItem()));
    }

    private static void registerBlockStateMappers() {
        RetroModelRegistry.ignoreLiquidLevel(ModBlocks.SEAGRASS, ModBlocks.KELP, ModBlocks.BUBBLE_COLUMN,
            ModBlocks.SEA_PICKLE, ModBlocks.CONDUIT);
        for (ModBlocks.CoralSet coral : ModBlocks.corals()) {
            RetroModelRegistry.ignoreLiquidLevel(coral.deadPlant, coral.livePlant, coral.deadFan, coral.liveFan);
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerBlockStateMappers();
        RetroModelRegistry.registerItems(
            ModItems.DRIED_KELP,
            ModItems.NAUTILUS_SHELL,
            ModItems.HEART_OF_THE_SEA,
            ModItems.TRIDENT,
            ModItems.COD,
            ModItems.SALMON,
            ModItems.PUFFERFISH,
            ModItems.TROPICAL_FISH,
            ModItems.COOKED_COD,
            ModItems.COOKED_SALMON,
            ModItems.SCUTE,
            ModItems.TURTLE_HELMET,
            ModItems.PHANTOM_MEMBRANE,
            ModItems.COD_BUCKET,
            ModItems.SALMON_BUCKET,
            ModItems.PUFFERFISH_BUCKET,
            ModItems.TROPICAL_FISH_BUCKET
        );
        for (net.minecraft.block.Block block : ModBlocks.allBlocks()) {
            RetroModelRegistry.registerBlockItem(block);
        }
    }
}
