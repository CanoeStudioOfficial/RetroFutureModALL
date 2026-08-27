package com.canoestudio.retrofuturelushcave.worldgen.cave;

import com.canoestudio.retrofuturelushcave.worldgen.NoiseChunk118;
import com.canoestudio.retrofuturelushcave.worldgen.NoiseGeneratorSettings118;
import com.canoestudio.retrofuturelushcave.worldgen.NoiseRouter118;
import com.canoestudio.retrofuturelushcave.worldgen.VanillaTerrainMigration118;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * 1.18地下洞穴路由入口。
 *
 * <p>与旧版不同，本类不再接管或重建基础地形。Mixin必须在原版
 * ChunkGeneratorOverworld#setBlocksInChunk及replaceBiomeBlocks完成后调用
 * {@link #migrateVanillaTerrainAndCarveUnderground(int, int, ChunkPrimer)}。该入口先把
 * 原版Primer迁移到+64深层布局，保留原版生成的地表水与表层材料，再仅在天然地下岩层中
 * 雕刻现代噪声洞穴。</p>
 */
public final class DensityCave118Generator {
    private final NoiseRouter118 noiseRouter;
    private final long aquiferSeed;

    public DensityCave118Generator(long worldSeed) {
        this.noiseRouter = new NoiseRouter118(worldSeed);
        this.aquiferSeed = worldSeed ^ 0x6A09E667F3BCC909L;
    }

    /**
     * 用于“原版写Primer时直接上移”的Mixin路径。此时原版地形与水体已经落在其最终高度，
     * 不可再次执行迁移；本方法只扫描实际地表并在天然岩层内雕刻现代地下洞穴。
     */
    public void carveShiftedVanillaTerrain(int chunkX, int chunkZ, ChunkPrimer primer, double[] vanillaHeightMap) {
        int[] actualSurfaceHeights = new int[256];
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int y = NoiseGeneratorSettings118.PRIMER_HEIGHT - 1;
                while (y > 0) {
                    IBlockState state = primer.getBlockState(localX, y, localZ);
                    if (state.getBlock() != Blocks.AIR) {
                        break;
                    }
                    y--;
                }
                actualSurfaceHeights[(localZ << 4) | localX] = y + 1;
            }
        }
        NoiseChunk118 noiseChunk = new NoiseChunk118(noiseRouter, aquiferSeed,
                chunkX, chunkZ, actualSurfaceHeights, vanillaHeightMap);
        noiseChunk.carveUnderground(primer);
    }

    /**
     * 旧完整密度地形入口保留为显式失败提示，防止配置未迁移时静默覆盖原版地表。
     */
    @Deprecated
    public void generateBaseTerrain(int chunkX, int chunkZ, ChunkPrimer primer) {
        throw new IllegalStateException("Use carveShiftedVanillaTerrain after vanilla terrain generation");
    }

    /**
     * 原版表层材料现在会先迁移后再雕刻，地下雕刻器只改天然可雕刻块，因此不再需要二次扫描恢复。
     */
    @Deprecated
    public void restoreUndergroundBiomeMaterials(int chunkX, int chunkZ, ChunkPrimer primer) {
        // Intentionally empty.
    }

    public double sampleUndergroundHumidity(int blockX, int blockZ) {
        return noiseRouter.sampleUndergroundHumidity(blockX, blockZ);
    }

    public double sampleUndergroundContinentalness(int blockX, int blockZ) {
        return noiseRouter.sampleUndergroundContinentalness(blockX, blockZ);
    }

    public double sampleUndergroundBiomeDepth(int blockX, int primerY, int blockZ) {
        NoiseRouter118.ClimateSample climate = noiseRouter.sampleClimate(blockX, blockZ);
        return noiseRouter.sampleUndergroundBiomeDepth(blockX, primerY, blockZ, climate);
    }

    public static int getMappedSeaLevel() {
        return VanillaTerrainMigration118.TARGET_SEA_LEVEL;
    }

    public static int getDeepStoneCeilingY() {
        return VanillaTerrainMigration118.DEEP_SPACE_HEIGHT - 1;
    }

    public static int getRouterYForPrimerY(int primerY) {
        return NoiseGeneratorSettings118.ROUTER_MIN_Y + primerY;
    }
}
