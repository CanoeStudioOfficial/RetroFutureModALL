package com.canoestudio.retrofutureupdateaquatic.proxy;

import com.canoestudio.retrofuturemccore.api.client.model.RetroModelRegistry;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralFan;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralPlant;
import com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle;
import com.canoestudio.retrofutureupdateaquatic.block.BlockConduit;
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
import com.canoestudio.retrofutureupdateaquatic.world.biome.AquaticBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RetroFutureUpdateAquatic.ID)
public class ClientProxy extends CommonProxy {

    /**
     * Forge exposes the 1.12 fog hook, so no renderer overwrite or Mixin is
     * needed. Fluidlogged API supplies the water state at the camera even
     * when a solid block is occupying the position.
     */
    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityPlayer) || entity.world == null) {
            return;
        }

        BlockPos eyePos = new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        if (!FluidloggedSupport.isWater(entity.world, eyePos)) {
            return;
        }

        Biome biome = entity.world.getBiome(eyePos);
        float density = AquaticBiomes.isFrozen(biome) ? 0.075F : 0.06F;
        event.setDensity(density);
        event.setCanceled(true);
    }

    @Override
    public void preInit() {
        super.preInit();
        registerWaterStateMappers();
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

    private static void registerWaterStateMappers() {
        RetroModelRegistry.ignoreStateProperties(ModBlocks.SEAGRASS, BlockLiquid.LEVEL);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.KELP, BlockLiquid.LEVEL);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.SEA_PICKLE,
            BlockLiquid.LEVEL, BlockSeaPickle.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.CONDUIT,
            BlockLiquid.LEVEL, BlockConduit.WATERLOGGED);
        for (ModBlocks.CoralSet coral : ModBlocks.corals()) {
            RetroModelRegistry.ignoreStateProperties(coral.deadPlant,
                BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED);
            RetroModelRegistry.ignoreStateProperties(coral.livePlant,
                BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED);
            RetroModelRegistry.ignoreStateProperties(coral.deadFan,
                BlockLiquid.LEVEL, BlockCoralFan.WATERLOGGED);
            RetroModelRegistry.ignoreStateProperties(coral.liveFan,
                BlockLiquid.LEVEL, BlockCoralFan.WATERLOGGED);
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
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
            ModItems.DEBUG_STICK,
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
