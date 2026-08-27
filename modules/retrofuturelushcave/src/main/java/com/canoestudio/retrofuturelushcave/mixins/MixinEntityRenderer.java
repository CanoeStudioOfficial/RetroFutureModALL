package com.canoestudio.retrofuturelushcave.mixins;

import com.canoestudio.retrofuturelushcave.worldgen.cave.OverworldHeightCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Shadow @Final private Minecraft mc;

    @Redirect(
            method = "renderRainSnow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float temperatureForRaisedRainSnow(Biome biome, BlockPos pos) {
        return this.mc.world != null && this.mc.world.provider.getDimension() == 0
                ? biome.getTemperature(OverworldHeightCompat.toVanillaClimatePosition(pos))
                : biome.getTemperature(pos);
    }
}
