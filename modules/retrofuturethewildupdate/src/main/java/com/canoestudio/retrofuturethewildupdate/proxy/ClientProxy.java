package com.canoestudio.retrofuturethewildupdate.proxy;

import com.canoestudio.retrofuturemccore.api.client.model.RetroModelRegistry;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.client.models.ModelFrog;
import com.canoestudio.retrofuturethewildupdate.client.models.ModelTadpole;
import com.canoestudio.retrofuturethewildupdate.client.particle.ParticleSonicBoom;
import com.canoestudio.retrofuturethewildupdate.client.renderer.RenderWarden;
import com.canoestudio.retrofuturethewildupdate.entity.EntityFrog;
import com.canoestudio.retrofuturethewildupdate.entity.EntityTadpole;
import com.canoestudio.retrofuturethewildupdate.entity.Warden;
import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RTWU.ID)
public class ClientProxy extends CommonProxy {

    private static final ResourceLocation TADPOLE_TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/entity/tadpole/tadpole.png");

    @Override
    public void preInit() {
        super.preInit();
        RetroModelRegistry.registerEntityRenderer(Warden.class, RenderWarden::new);
        RetroModelRegistry.registerLivingRenderer(
            EntityFrog.class,
            ModelFrog::new,
            entity -> new ResourceLocation(RTWU.ID, "textures/entity/frog/frog_" + entity.getVariantName() + ".png"),
            0.3F,
            context -> {
                if (context.getEntity().isChild()) {
                    GlStateManager.scale(0.55F, 0.55F, 0.55F);
                }
            }
        );
        RetroModelRegistry.registerLivingRenderer(
            EntityTadpole.class,
            ModelTadpole::new,
            TADPOLE_TEXTURE,
            0.14F,
            context -> GlStateManager.scale(0.8F, 0.8F, 0.8F)
        );
    }

    @Override
    public void spawnSonicBoom(World world, double x, double y, double z) {
        ParticleSonicBoom particle = new ParticleSonicBoom(world, x, y, z);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        RetroModelRegistry.registerItems(
            ModItems.WARDEN_EGG,
            ModItems.ECHO_SHARD,
            ModItems.DISC_FRAGMENT_5,
            ModItems.RECOVERY_COMPASS,
            ModItems.TADPOLE_BUCKET,
            ModItems.FROG_SPAWN_EGG,
            ModItems.TADPOLE_SPAWN_EGG
        );
        RetroModelRegistry.registerBlockItems(
            ModBlocks.SCULK,
            ModBlocks.SCULK_VEIN,
            ModBlocks.SCULK_SENSOR,
            ModBlocks.SCULK_SHRIEKER,
            ModBlocks.SCULK_CATALYST,
            ModBlocks.MUD,
            ModBlocks.PACKED_MUD,
            ModBlocks.MUD_BRICKS,
            ModBlocks.MANGROVE_LOG,
            ModBlocks.STRIPPED_MANGROVE_LOG,
            ModBlocks.MANGROVE_PLANKS,
            ModBlocks.MANGROVE_LEAVES,
            ModBlocks.MANGROVE_ROOTS,
            ModBlocks.MUDDY_MANGROVE_ROOTS,
            ModBlocks.MANGROVE_PROPAGULE,
            ModBlocks.FROGSPAWN,
            ModBlocks.OCHRE_FROGLIGHT,
            ModBlocks.VERDANT_FROGLIGHT,
            ModBlocks.PEARLESCENT_FROGLIGHT,
            ModBlocks.REINFORCED_DEEPSLATE
        );
    }
}
