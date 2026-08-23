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
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.BlockLiquid;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        registerWaterStateMappers();
        RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, RenderBrownMooshroom::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        MinecraftForge.EVENT_BUS.register(SpyglassHandler.class);
        MinecraftForge.EVENT_BUS.register(PowderSnowHudHandler.class);
    }

    private static void registerWaterStateMappers() {
        RetroModelRegistry.ignoreStateProperties(ModBlocks.SMALL_DRIPLEAF,
                BlockLiquid.LEVEL, SmallDripleaf.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.DRIPLEAF_STEM,
                BlockLiquid.LEVEL, DripleafStem.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.BIG_DRIPLEAF,
                BlockLiquid.LEVEL, BigDripleaf.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.BIG_DRIPLEAF_WATERLOGGED,
                BlockLiquid.LEVEL, BigDripleaf.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.HANGING_ROOTS,
                BlockLiquid.LEVEL, HangingRootsBlock.WATERLOGGED);
        for (GlowLichenBlock glowLichen : ModBlocks.GLOW_LICHEN_VARIANTS) {
            RetroModelRegistry.ignoreStateProperties(glowLichen,
                    BlockLiquid.LEVEL, GlowLichenBlock.WATERLOGGED);
        }
        RetroModelRegistry.ignoreStateProperties(ModBlocks.AMETHYST_CLUSTER,
                BlockLiquid.LEVEL, AmethystClusterBlock.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.LARGE_AMETHYST_BUD,
                BlockLiquid.LEVEL, AmethystClusterBlock.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.MEDIUM_AMETHYST_BUD,
                BlockLiquid.LEVEL, AmethystClusterBlock.WATERLOGGED);
        RetroModelRegistry.ignoreStateProperties(ModBlocks.SMALL_AMETHYST_BUD,
                BlockLiquid.LEVEL, AmethystClusterBlock.WATERLOGGED);
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
