package com.canoestudio.retrofuturelushcave.mixins;

import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;
import com.canoestudio.retrofuturelushcave.worldgen.cave.Geode118Generator;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.UndergroundCaveFeatureDecorator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.MapGenBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 固定世界海平面Y=127的直接Primer上移路径。
 *
 * <p>以原版ChunkGeneratorOverworld为基础：原版基础地形写入时直接上移64格；
 * 原版表层替换在已上移的Primer中运行；原版CAVE调用被新版1.18地下密度雕刻替换。</p>
 */
@Mixin(ChunkGeneratorOverworld.class)
public abstract class MixinChunkGeneratorOverworld {
    @Shadow @Final private World world;
    @Shadow @Final private double[] heightMap;
    @Shadow private MapGenBase caveGenerator;

    @Unique private DensityCave118Generator densityRouter;
    @Unique private boolean geodePlacedThisPopulate;

    @Inject(method = "setBlocksInChunk", at = @At("HEAD"))
    private void startShiftForBaseTerrain(int chunkX, int chunkZ, ChunkPrimer primer, CallbackInfo ci) {
        if (this.world.getSeaLevel() != 127) {
            this.world.setSeaLevel(127);
        }
    }

    /**
     * 原版基础地形已整体上移；填充新获得的0..63深层空间，避免其保持为空。
     * 仅操作ChunkPrimer，不会触发流体更新或邻居递归。
     */
    @Inject(method = "setBlocksInChunk", at = @At("RETURN"))
    private void finishShiftAndFillDeepBase(int chunkX, int chunkZ, ChunkPrimer primer, CallbackInfo ci) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = 0; y <= 64; y++) {
                    primer.setBlockState(localX, y, localZ,
                            y <= 4 ? Blocks.BEDROCK.getDefaultState() : Blocks.STONE.getDefaultState());
                }
            }
        }
    }

    /** 仅移动原版setBlocksInChunk的基础地形写入；不要移动replaceBiomeBlocks的表层写入。 */
    @ModifyArg(
            method = "setBlocksInChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkPrimer;setBlockState(IIILnet/minecraft/block/state/IBlockState;)V"
            ),
            index = 1
    )
    private int shiftYForBaseTerrain(int y) {
        /*
         * ChunkPrimer仅支持0..255。原版setBlocksInChunk仍可能写到原始Y=255，
         * 因此不能无条件y+64；那会写入256..319并污染相邻列的Primer索引。
         * 保持原始0..128精确+64（海平面63->127），把129..255压缩到193..255。
         */
        if (y <= 128) {
            return y + 64;
        }
        return 193 + (y - 129) * 62 / 126;
    }

    /** 用新版噪声洞穴替换原版MapGenCaves；其他MapGenBase调用（包括峡谷）保持原流程。 */
    @Redirect(
            method = "generateChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/MapGenBase;generate(Lnet/minecraft/world/World;IILnet/minecraft/world/chunk/ChunkPrimer;)V"
            )
    )
    private void redirectCaveGeneration(MapGenBase mapGen, World worldIn,
                                        int chunkX, int chunkZ, ChunkPrimer primer) {
        if (mapGen == this.caveGenerator) {
            if (this.densityRouter == null) {
                this.densityRouter = new DensityCave118Generator(this.world.getSeed());
            }
            this.densityRouter.carveShiftedVanillaTerrain(chunkX, chunkZ, primer, this.heightMap);
        } else {
            mapGen.generate(worldIn, chunkX, chunkZ, primer);
        }
    }

    @Inject(method = "populate", at = @At("HEAD"))
    private void resetGeodePopulateState(int chunkX, int chunkZ, CallbackInfo ci) {
        this.geodePlacedThisPopulate = false;
    }

    @Inject(
            method = "populate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/structure/MapGenMineshaft;generateStructure(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/ChunkPos;)Z",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void placeAmethystGeodeAfterMineshaft(int chunkX, int chunkZ, CallbackInfo ci) {
        Geode118Generator.populateBeforeBiomeDecorate(this.world, chunkX, chunkZ);
        this.geodePlacedThisPopulate = true;
    }

    @Inject(
            method = "populate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;decorate(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void placeAmethystGeodeFallback(int chunkX, int chunkZ, CallbackInfo ci) {
        if (!this.geodePlacedThisPopulate) {
            Geode118Generator.populateBeforeBiomeDecorate(this.world, chunkX, chunkZ);
        }
    }

    @Inject(
            method = "populate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;decorate(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void placeUndergroundCaveFeatures(int chunkX, int chunkZ, CallbackInfo ci) {
        UndergroundCaveFeatureDecorator.populateLushPlacedFeatures(this.world, chunkX, chunkZ);
    }
}
