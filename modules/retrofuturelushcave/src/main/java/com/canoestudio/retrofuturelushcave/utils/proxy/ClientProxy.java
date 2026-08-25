package com.canoestudio.retrofuturelushcave.utils.proxy;

import com.canoestudio.retrofuturelushcave.contents.items.spyglass.SpyglassHandler;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.blocks.AmethystClusterBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.GlowLichenBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.HangingRootsBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.BigDripleaf;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.DripleafStem;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.SmallDripleaf;
import com.canoestudio.retrofuturemccore.api.client.model.RetroModelRegistry;
import com.canoestudio.retrofuturelushcave.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturelushcave.contents.mobs.axolotl.RenderAxolotl;
import com.canoestudio.retrofuturelushcave.contents.mobs.brownmooshrooms.EntityBrownMooshroom;
import com.canoestudio.retrofuturelushcave.contents.mobs.brownmooshrooms.RenderBrownMooshroom;
import com.canoestudio.retrofuturelushcave.contents.mobs.goat.EntityGoat;
import com.canoestudio.retrofuturelushcave.contents.mobs.goat.RenderGoat;
import com.canoestudio.retrofuturelushcave.contents.mobs.glowsquid.EntityGlowSquid;
import com.canoestudio.retrofuturelushcave.contents.mobs.glowsquid.RenderGlowSquid;
import com.canoestudio.retrofuturelushcave.utils.PowderSnowHudHandler;
import com.canoestudio.retrofuturelushcave.utils.RetroFutureClientCoreIntegration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;

import java.util.Arrays;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags.MOD_ID)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, RenderBrownMooshroom::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        MinecraftForge.EVENT_BUS.register(SpyglassHandler.class);
        MinecraftForge.EVENT_BUS.register(PowderSnowHudHandler.class);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerWaterStateMappers();
    }

    private static void registerWaterStateMappers() {
        RetroModelRegistry.ignoreStateProperties(ModBlocks.SMALL_DRIPLEAF,
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, SmallDripleaf.WATERLOGGED));
        RetroModelRegistry.ignoreStateProperties(ModBlocks.DRIPLEAF_STEM,
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, DripleafStem.WATERLOGGED));
        RetroModelRegistry.ignoreStateProperties(ModBlocks.BIG_DRIPLEAF,
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, BigDripleaf.WATERLOGGED));
        RetroModelRegistry.ignoreStateProperties(
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, BigDripleaf.WATERLOGGED),
                Arrays.asList(ModBlocks.BIG_DRIPLEAF_WATERLOGGED));
        RetroModelRegistry.ignoreStateProperties(ModBlocks.HANGING_ROOTS,
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, HangingRootsBlock.WATERLOGGED));
        RetroModelRegistry.ignoreStateProperties(
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, GlowLichenBlock.WATERLOGGED),
                Arrays.asList(ModBlocks.GLOW_LICHEN_VARIANTS));
        RetroModelRegistry.ignoreStateProperties(
                Arrays.<IProperty<?>>asList(BlockLiquid.LEVEL, AmethystClusterBlock.WATERLOGGED),
                Arrays.asList(ModBlocks.AMETHYST_CLUSTER, ModBlocks.LARGE_AMETHYST_BUD,
                        ModBlocks.MEDIUM_AMETHYST_BUD, ModBlocks.SMALL_AMETHYST_BUD));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        RetroFutureClientCoreIntegration.register();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

}
