package com.canoestudio.retrofuturethewildupdate.proxy;

import com.canoestudio.retrofuturelushcavecore.api.world.RetroWorldgenRegistry;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.world.biome.ModBiomes;
import com.canoestudio.retrofuturethewildupdate.world.gen.WildUpdateWorldGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CommonProxy {

    public void preInit() {
        ModBlocks.registerTileEntities();
        RetroWorldgenRegistry.registerGenerator(new WildUpdateWorldGenerator(), 30);
    }

    public void init() {
        ModBiomes.init();
    }

    public void spawnSonicBoom(World world, double x, double y, double z) {
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RTWU.ID, name);
    }
}
