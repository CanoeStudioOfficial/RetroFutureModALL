package com.canoestudio.retrofuturelushcave.mixins;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureOceanMonument.StartMonument.class)
public abstract class MixinStartMonument {
    private static final int RAISED_HEIGHT_OFFSET = 64;
    private static final int VANILLA_MONUMENT_MIN_Y = 39;

    /** 新结构在StartMonument#create中建立组件和总边界框后立即平移。 */
    @Inject(method = "create", at = @At("RETURN"))
    private void raiseNewMonument(World worldIn, Random random, int chunkX, int chunkZ, CallbackInfo ci) {
        raiseIfVanillaHeight(worldIn);
    }

    /**
     * NBT反序列化后的StartMonument不会重新执行create；在写入任意区块前处理。
     * 总边界框minY会随组件一起被序列化，故可作为跨存档的幂等标记。
     */
    @Inject(method = "generateStructure", at = @At("HEAD"))
    private void raiseLoadedMonument(World worldIn, Random rand, StructureBoundingBox structurebb, CallbackInfo ci) {
        raiseIfVanillaHeight(worldIn);
    }

    @Unique
    private void raiseIfVanillaHeight(World worldIn) {
        if (worldIn == null || worldIn.provider.getDimension() != 0) {
            return;
        }

        StructureOceanMonument.StartMonument monument =
                (StructureOceanMonument.StartMonument) (Object) this;
        StructureBoundingBox monumentBox = monument.getBoundingBox();

        /* 原版MonumentBuilding与StructureStart总边界框均从minY=39开始；抬升后为103。 */
        if (monumentBox == null || monumentBox.minY != VANILLA_MONUMENT_MIN_Y) {
            return;
        }

        for (StructureComponent component : monument.getComponents()) {
            component.offset(0, RAISED_HEIGHT_OFFSET, 0);
        }

        /* 所有组件采用相同偏移，因此总边界框只需同步平移，无需调用继承的protected方法。 */
        monumentBox.offset(0, RAISED_HEIGHT_OFFSET, 0);
    }
}
