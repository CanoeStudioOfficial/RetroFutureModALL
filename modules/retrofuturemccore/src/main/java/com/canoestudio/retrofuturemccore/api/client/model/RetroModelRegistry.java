package com.canoestudio.retrofuturemccore.api.client.model;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Client-only helpers that expose a modern-style registration facade over
 * Forge 1.12's ModelLoader and RenderingRegistry APIs.
 */
@SideOnly(Side.CLIENT)
public final class RetroModelRegistry {

    private static final String INVENTORY_VARIANT = "inventory";

    private RetroModelRegistry() {
    }

    public static void registerItem(Item item) {
        registerItem(item, 0);
    }

    public static void registerItem(Item item, int metadata) {
        ResourceLocation name = requireRegistryName(item);
        registerItem(item, metadata, name, INVENTORY_VARIANT);
    }

    public static void registerItem(Item item, int metadata, ResourceLocation model) {
        registerItem(item, metadata, model, INVENTORY_VARIANT);
    }

    public static void registerItem(Item item, int metadata, ResourceLocation model, String variant) {
        if (item == null || model == null) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(model, variant));
    }

    public static void registerItems(Item... items) {
        if (items == null) {
            return;
        }
        for (Item item : items) {
            registerItem(item);
        }
    }

    public static void registerBlockItem(Block block) {
        registerBlockItem(block, 0);
    }

    public static void registerBlockItem(Block block, int metadata) {
        ResourceLocation name = requireRegistryName(block);
        registerBlockItem(block, metadata, name, INVENTORY_VARIANT);
    }

    public static void registerBlockItem(Block block, int metadata, ResourceLocation model) {
        registerBlockItem(block, metadata, model, INVENTORY_VARIANT);
    }

    public static void registerBlockItem(Block block, int metadata, ResourceLocation model, String variant) {
        if (block == null || model == null) {
            return;
        }
        Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(model, variant));
    }

    public static void registerBlockItems(Block... blocks) {
        if (blocks == null) {
            return;
        }
        for (Block block : blocks) {
            registerBlockItem(block);
        }
    }

    public static void ignoreStateProperties(Block block, IProperty<?>... properties) {
        if (block == null || properties == null || properties.length == 0) {
            return;
        }
        ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(properties).build());
    }

    public static void ignoreStateProperties(IProperty<?>[] properties, Block... blocks) {
        if (blocks == null) {
            return;
        }
        for (Block block : blocks) {
            ignoreStateProperties(block, properties);
        }
    }

    public static void ignoreLiquidLevel(Block... blocks) {
        if (blocks == null) {
            return;
        }
        for (Block block : blocks) {
            ignoreStateProperties(block, BlockLiquid.LEVEL);
        }
    }

    public static <E extends Entity> void registerEntityRenderer(Class<E> entityClass,
            IRenderFactory<? super E> factory) {
        if (entityClass == null || factory == null) {
            return;
        }
        RenderingRegistry.registerEntityRenderingHandler(entityClass, factory);
    }

    public static <T extends TileEntity> void registerTileEntityRenderer(Class<T> tileEntityClass,
            TileEntitySpecialRenderer<? super T> renderer) {
        if (tileEntityClass == null || renderer == null) {
            return;
        }
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, renderer);
    }

    public static <E extends EntityLiving> void registerLivingRenderer(Class<E> entityClass,
            Supplier<? extends ModelBase> modelFactory, ResourceLocation texture, float shadowSize) {
        registerLivingRenderer(entityClass, modelFactory, new StaticTextureResolver<E>(texture), shadowSize);
    }

    public static <E extends EntityLiving> void registerLivingRenderer(Class<E> entityClass,
            Supplier<? extends ModelBase> modelFactory, Function<E, ResourceLocation> textureResolver,
            float shadowSize) {
        registerLivingRenderer(entityClass, modelFactory, textureResolver, shadowSize, null);
    }

    public static <E extends EntityLiving> void registerLivingRenderer(Class<E> entityClass,
            Supplier<? extends ModelBase> modelFactory, ResourceLocation texture, float shadowSize,
            Consumer<PreRenderContext<E>> preRender) {
        registerLivingRenderer(entityClass, modelFactory, new StaticTextureResolver<E>(texture), shadowSize, preRender);
    }

    public static <E extends EntityLiving> void registerLivingRenderer(Class<E> entityClass,
            final Supplier<? extends ModelBase> modelFactory, final Function<E, ResourceLocation> textureResolver,
            final float shadowSize, final Consumer<PreRenderContext<E>> preRender) {
        if (entityClass == null || modelFactory == null || textureResolver == null) {
            return;
        }
        registerEntityRenderer(entityClass, manager ->
            new SimpleLivingRenderer<E>(manager, modelFactory.get(), textureResolver, shadowSize, preRender));
    }

    public static ModelResourceLocation modelLocation(ResourceLocation model) {
        return new ModelResourceLocation(model, INVENTORY_VARIANT);
    }

    public static ModelResourceLocation modelLocation(ResourceLocation model, String variant) {
        return new ModelResourceLocation(model, variant);
    }

    private static ResourceLocation requireRegistryName(Item item) {
        if (item == null || item.getRegistryName() == null) {
            throw new IllegalArgumentException("Cannot register a model for an unregistered item");
        }
        return item.getRegistryName();
    }

    private static ResourceLocation requireRegistryName(Block block) {
        if (block == null || block.getRegistryName() == null) {
            throw new IllegalArgumentException("Cannot register a model for an unregistered block");
        }
        return block.getRegistryName();
    }

    public static final class PreRenderContext<E extends EntityLiving> {
        private final E entity;
        private final float partialTicks;

        public PreRenderContext(E entity, float partialTicks) {
            this.entity = entity;
            this.partialTicks = partialTicks;
        }

        public E getEntity() {
            return this.entity;
        }

        public float getPartialTicks() {
            return this.partialTicks;
        }
    }

    private static final class StaticTextureResolver<E extends EntityLiving> implements Function<E, ResourceLocation> {
        private final ResourceLocation texture;

        private StaticTextureResolver(ResourceLocation texture) {
            this.texture = texture;
        }

        @Override
        public ResourceLocation apply(E entity) {
            return this.texture;
        }
    }

    private static final class SimpleLivingRenderer<E extends EntityLiving> extends RenderLiving<E> {
        private final Function<E, ResourceLocation> textureResolver;
        private final Consumer<PreRenderContext<E>> preRender;

        private SimpleLivingRenderer(net.minecraft.client.renderer.entity.RenderManager renderManager,
                ModelBase model, Function<E, ResourceLocation> textureResolver, float shadowSize,
                Consumer<PreRenderContext<E>> preRender) {
            super(renderManager, model, shadowSize);
            this.textureResolver = textureResolver;
            this.preRender = preRender;
        }

        @Override
        protected ResourceLocation getEntityTexture(E entity) {
            return this.textureResolver.apply(entity);
        }

        @Override
        protected void preRenderCallback(E entity, float partialTickTime) {
            if (this.preRender != null) {
                this.preRender.accept(new PreRenderContext<E>(entity, partialTickTime));
            }
        }
    }
}
