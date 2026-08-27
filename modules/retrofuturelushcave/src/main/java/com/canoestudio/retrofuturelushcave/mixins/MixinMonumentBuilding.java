package com.canoestudio.retrofuturelushcave.mixins;

import java.util.List;
import java.util.Random;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureOceanMonumentPieces;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureOceanMonumentPieces.MonumentBuilding.class)
public abstract class MixinMonumentBuilding {
    private static final int RAISED_HEIGHT_OFFSET = 64;

    @Shadow @Final private List<StructureOceanMonumentPieces.Piece> childPieces;

    @Inject(
            method = "<init>(Ljava/util/Random;IILnet/minecraft/util/EnumFacing;)V",
            at = @At("RETURN")
    )
    private void raiseCompleteMonument(Random random, int x, int z, EnumFacing facing, CallbackInfo ci) {
        ((StructureOceanMonumentPieces.MonumentBuilding) (Object) this)
                .offset(0, RAISED_HEIGHT_OFFSET, 0);
        for (StructureOceanMonumentPieces.Piece piece : childPieces) {
            piece.offset(0, RAISED_HEIGHT_OFFSET, 0);
        }
    }
}
