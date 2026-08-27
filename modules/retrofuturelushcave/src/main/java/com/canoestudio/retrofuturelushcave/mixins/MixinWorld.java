package com.canoestudio.retrofuturelushcave.mixins;

import com.canoestudio.retrofuturelushcave.worldgen.cave.OverworldHeightCompat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow @Final
    public WorldProvider provider;

    @Redirect(
            method = "canSnowAtBody",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float temperatureForRaisedSnow(Biome biome, BlockPos pos) {
        return this.provider.getDimension() == 0
                ? biome.getTemperature(OverworldHeightCompat.toVanillaClimatePosition(pos))
                : biome.getTemperature(pos);
    }

    @Redirect(
            method = "canBlockFreezeBody",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float temperatureForRaisedFreezing(Biome biome, BlockPos raisedWorldPos) {
        return this.provider.getDimension() == 0
                ? biome.getTemperature(OverworldHeightCompat.toVanillaClimatePosition(raisedWorldPos))
                : biome.getTemperature(raisedWorldPos);
    }
}
