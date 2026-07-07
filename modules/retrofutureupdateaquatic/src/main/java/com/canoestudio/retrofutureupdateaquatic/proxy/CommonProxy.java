package com.canoestudio.retrofutureupdateaquatic.proxy;

import com.canoestudio.retrofuturemccore.api.world.RetroWorldgenRegistry;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.block.TileEntityConduit;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.world.ModAquaticSpawns;
import com.canoestudio.retrofutureupdateaquatic.world.gen.AquaticWorldGenerator;
import com.canoestudio.retrofutureupdateaquatic.world.gen.AquaticStructureGenerator;
import com.canoestudio.retrofutureupdateaquatic.world.AquaticLootTables;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CommonProxy {

    public void preInit() {
        AquaticLootTables.init();
        RetroWorldgenRegistry.registerGenerator(new AquaticStructureGenerator(), 0);
        RetroWorldgenRegistry.registerGenerator(new AquaticWorldGenerator(), 8);
        GameRegistry.registerTileEntity(TileEntityConduit.class, prefix("conduit"));
    }

    public void init() {
        ModAquaticSpawns.init();
        registerRecipes();
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RetroFutureUpdateAquatic.ID, name);
    }

    private void registerRecipes() {
        GameRegistry.addSmelting(ModItems.COD, new ItemStack(ModItems.COOKED_COD), 0.35F);
        GameRegistry.addSmelting(ModItems.SALMON, new ItemStack(ModItems.COOKED_SALMON), 0.35F);
        ForgeRegistries.RECIPES.register(new ShapedOreRecipe(prefix("dried_kelp_block"),
            new ItemStack(ModBlocks.DRIED_KELP_BLOCK), "KKK", "KKK", "KKK", 'K', ModItems.DRIED_KELP)
            .setRegistryName(prefix("dried_kelp_block")));
        ForgeRegistries.RECIPES.register(new ShapedOreRecipe(prefix("blue_ice"),
            new ItemStack(ModBlocks.BLUE_ICE), "III", "III", "III", 'I', Blocks.PACKED_ICE)
            .setRegistryName(prefix("blue_ice")));
        ForgeRegistries.RECIPES.register(new ShapedOreRecipe(prefix("turtle_helmet"),
            new ItemStack(ModItems.TURTLE_HELMET), "SSS", "S S", 'S', ModItems.SCUTE)
            .setRegistryName(prefix("turtle_helmet")));
        registerWoodRecipes();
    }

    private void registerWoodRecipes() {
        for (ModBlocks.WoodSet wood : ModBlocks.woods()) {
            ItemStack vanillaLog = vanillaLogStack(wood.name, 1);
            ItemStack planks = planksStack(wood.name, 4);
            ForgeRegistries.RECIPES.register(new ShapedOreRecipe(prefix(wood.name + "_wood"),
                new ItemStack(wood.wood, 3), "LL", "LL", 'L', vanillaLog)
                .setRegistryName(prefix(wood.name + "_wood")));
            ForgeRegistries.RECIPES.register(new ShapedOreRecipe(prefix("stripped_" + wood.name + "_wood"),
                new ItemStack(wood.strippedWood, 3), "LL", "LL", 'L', new ItemStack(wood.strippedLog))
                .setRegistryName(prefix("stripped_" + wood.name + "_wood")));
            ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(prefix(wood.name + "_planks_from_wood"),
                planks.copy(), new ItemStack(wood.wood))
                .setRegistryName(prefix(wood.name + "_planks_from_wood")));
            ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(prefix(wood.name + "_planks_from_stripped_log"),
                planks.copy(), new ItemStack(wood.strippedLog))
                .setRegistryName(prefix(wood.name + "_planks_from_stripped_log")));
            ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(prefix(wood.name + "_planks_from_stripped_wood"),
                planks.copy(), new ItemStack(wood.strippedWood))
                .setRegistryName(prefix(wood.name + "_planks_from_stripped_wood")));
        }
    }

    private ItemStack vanillaLogStack(String woodName, int count) {
        int metadata = woodMetadata(woodName);
        return metadata < 4 ? new ItemStack(Blocks.LOG, count, metadata)
            : new ItemStack(Blocks.LOG2, count, metadata - 4);
    }

    private ItemStack planksStack(String woodName, int count) {
        return new ItemStack(Blocks.PLANKS, count, woodMetadata(woodName));
    }

    private int woodMetadata(String woodName) {
        if ("spruce".equals(woodName)) {
            return 1;
        }
        if ("birch".equals(woodName)) {
            return 2;
        }
        if ("jungle".equals(woodName)) {
            return 3;
        }
        if ("acacia".equals(woodName)) {
            return 4;
        }
        if ("dark_oak".equals(woodName)) {
            return 5;
        }
        return 0;
    }
}
