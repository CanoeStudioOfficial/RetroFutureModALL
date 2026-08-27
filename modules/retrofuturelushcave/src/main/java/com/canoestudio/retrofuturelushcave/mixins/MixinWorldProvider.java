package com.canoestudio.retrofuturelushcave.mixins;

import com.canoestudio.retrofuturelushcave.worldgen.cave.OverworldHeightCompat;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {
    @Shadow(remap = false)
    public abstract int getDimension();

    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void raiseOverworldClouds(CallbackInfoReturnable<Float> cir) {
        if (this.getDimension() == 0) {
            cir.setReturnValue(cir.getReturnValue() + OverworldHeightCompat.getVerticalOffset());
        }
    }
}
