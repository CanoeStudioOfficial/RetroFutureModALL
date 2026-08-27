package com.canoestudio.retrofuturelushcave.worldgen;

import com.canoestudio.retrofuturelushcave.config.Configuration;

import static com.canoestudio.retrofuturelushcave.RetroFutureLushCave.LOGGER;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** 基于官方1.18密度路由的地下雕刻器；真实Primer表面仅用于边界适配。 */
public final class NoiseChunk118 {
    private static final int CELL_COUNT_XZ = 16 / NoiseGeneratorSettings118.CELL_WIDTH;
    private static final int CELL_COUNT_Y = NoiseGeneratorSettings118.PRIMER_HEIGHT / NoiseGeneratorSettings118.CELL_HEIGHT;
    private static final int CLIMATE_CACHE_SIZE = CELL_COUNT_XZ + 1;

    /** 可选的指定区块入口诊断。 */
    public static final boolean DEBUG_ENTRANCE_DIAGNOSTIC = false;
    public static final int DEBUG_ENTRANCE_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_ENTRANCE_CHUNK_Z = Integer.MIN_VALUE;

    private final NoiseRouter118 noiseRouter;
    private final Aquifer118 aquifer;
    private final int originX;
    private final int originZ;
    /** 每列真实表层上方Y，索引为 {@code (localZ << 4) | localX}。 */
    private final int[] actualSurfaceHeights;
    private final VanillaTerrainDensity112 vanillaTerrainDensity;

    private final NoiseInterpolator118 finalDensityInterpolator;
    private final NoiseInterpolator118 noodleToggleInterpolator;
    private final NoiseInterpolator118 noodleThicknessInterpolator;
    private final NoiseInterpolator118 noodleRidgeAInterpolator;
    private final NoiseInterpolator118 noodleRidgeBInterpolator;

    public NoiseChunk118(NoiseRouter118 noiseRouter, long aquiferSeed,
                         int chunkX, int chunkZ, int[] actualSurfaceHeights, double[] vanillaHeightMap) {
        if (actualSurfaceHeights == null || actualSurfaceHeights.length != 256) {
            throw new IllegalArgumentException("actualSurfaceHeights must contain 16x16 columns");
        }
        this.noiseRouter = noiseRouter;
        this.originX = chunkX << 4;
        this.originZ = chunkZ << 4;
        this.actualSurfaceHeights = actualSurfaceHeights.clone();
        this.vanillaTerrainDensity = new VanillaTerrainDensity112(vanillaHeightMap, chunkX, chunkZ);

        Aquifer118.SurfaceHeightProvider surfaceHeightProvider = (x, z) -> VanillaTerrainMigration118.TARGET_SEA_LEVEL;
        Aquifer118.FluidPicker fluidPicker = Aquifer118.createOverworldFluidPicker(
                VanillaTerrainMigration118.TARGET_SEA_LEVEL,
                NoiseGeneratorSettings118.LAVA_LEVEL_112);
        this.aquifer = new Aquifer118(
                aquiferSeed, chunkX, chunkZ, 0, NoiseGeneratorSettings118.PRIMER_HEIGHT,
                surfaceHeightProvider, fluidPicker,
                noiseRouter.getAquiferBarrierNoise(), noiseRouter.getAquiferFloodednessNoise(),
                noiseRouter.getAquiferSpreadNoise(), noiseRouter.getAquiferLavaNoise(), true);

        this.finalDensityInterpolator = new NoiseInterpolator118((x, rawRouterY, z) -> {
            int routerY = mapRouterYAtCorner(x, rawRouterY, z);
            int primerY = routerY - NoiseGeneratorSettings118.ROUTER_MIN_Y;
            double terrainDensity = vanillaTerrainDensity.sampleAtGridPoint(x, primerY, z);
            return noiseRouter.getCaveDensity(x, routerY, z, terrainDensity);
        }, originX, originZ);
        this.noodleToggleInterpolator = new NoiseInterpolator118(
                (x, rawRouterY, z) -> noiseRouter.getNoodleToggle(x, mapRouterYAtCorner(x, rawRouterY, z), z), originX, originZ);
        this.noodleThicknessInterpolator = new NoiseInterpolator118(
                (x, rawRouterY, z) -> noiseRouter.getNoodleThickness(x, mapRouterYAtCorner(x, rawRouterY, z), z), originX, originZ);
        this.noodleRidgeAInterpolator = new NoiseInterpolator118(
                (x, rawRouterY, z) -> noiseRouter.getNoodleRidgeA(x, mapRouterYAtCorner(x, rawRouterY, z), z), originX, originZ);
        this.noodleRidgeBInterpolator = new NoiseInterpolator118(
                (x, rawRouterY, z) -> noiseRouter.getNoodleRidgeB(x, mapRouterYAtCorner(x, rawRouterY, z), z), originX, originZ);
    }

    /**
     * 在迁移后的天然岩层中雕刻洞穴。地表保护带阻止普通洞穴破坏地表；只有密度明显为负的
     * 入口/强洞穴分支可穿入该保护带，因此入口会沿真实山坡出现而不会把普通地表筛成蜂窝。
     */
    public void carveUnderground(ChunkPrimer primer) {
        /* 官方NoiseChunk不在每个Chunk雕刻后做局部地表流体BFS/原始方块恢复。
         * 该后处理只在16×16范围内运行，会把同一洞室恢复成块状实体墙。 */
        boolean diagnostic = DEBUG_ENTRANCE_DIAGNOSTIC
                && (originX >> 4) == DEBUG_ENTRANCE_CHUNK_X && (originZ >> 4) == DEBUG_ENTRANCE_CHUNK_Z;
        int shallowNegativeCandidates = 0;
        int entranceRejected = 0;
        int entranceAccepted = 0;
        int topLayerRejected = 0;
        int shallowAirWritten = 0;
        int entranceMouthAirWritten = 0;
        int entranceMouthCaveEntranceSignal = 0;
        int entranceMouthSpaghettiSignal = 0;
        int entranceMinimumDepth = Integer.MAX_VALUE;
        int entranceMaximumDepth = -1;
        boolean[] entranceAirColumns = new boolean[256];
        boolean[] entranceMouthColumns = new boolean[256];
        fillAllInterpolators(0, true);
        for (int cellX = 0; cellX < CELL_COUNT_XZ; cellX++) {
            if (cellX > 0) swapAllInterpolators();
            fillAllInterpolators(cellX + 1, false);

            for (int cellZ = 0; cellZ < CELL_COUNT_XZ; cellZ++) {
                for (int cellY = 0; cellY < CELL_COUNT_Y; cellY++) {
                    int rawRouterCellStartY = NoiseGeneratorSettings118.ROUTER_MIN_Y
                            + cellY * NoiseGeneratorSettings118.CELL_HEIGHT;
                    int primerStartY = cellY * NoiseGeneratorSettings118.CELL_HEIGHT;
                    for (int inX = 0; inX < NoiseGeneratorSettings118.CELL_WIDTH; inX++) {
                        int localX = cellX * NoiseGeneratorSettings118.CELL_WIDTH + inX;
                        double tx = (double) inX / NoiseGeneratorSettings118.CELL_WIDTH;
                        for (int inZ = 0; inZ < NoiseGeneratorSettings118.CELL_WIDTH; inZ++) {
                            int localZ = cellZ * NoiseGeneratorSettings118.CELL_WIDTH + inZ;
                            double tz = (double) inZ / NoiseGeneratorSettings118.CELL_WIDTH;
                            int surfaceIndex = (localZ << 4) | localX;
                            int actualSurface = actualSurfaceHeights[surfaceIndex];
                            int surfaceFluidBottom = findSurfaceFluidBottom(primer, localX, actualSurface, localZ);
                            for (int inY = 0; inY < NoiseGeneratorSettings118.CELL_HEIGHT; inY++) {
                                int primerY = primerStartY + inY;
                                double ty = (double) inY / NoiseGeneratorSettings118.CELL_HEIGHT;
                                IBlockState existing = primer.getBlockState(localX, primerY, localZ);
                                if (!VanillaTerrainMigration118.isNaturalCarvable(existing)) continue;

                                int depthBelowActualSurface = actualSurface - primerY;
                                /* 所有高度统一使用NoiseChunk的4x4x8连续插值。此前在真实表面下16格
                                 * 改为逐方块采样，导致同一洞室在第16格处切换成另一套密度场，形成截图中的
                                 * 平整矩形顶棚和半截悬台。真实Primer表面只在后续顶盖判定中使用。 */
                                double routed = noiseRouter.getSqueeze(0.64D *
                                        finalDensityInterpolator.getValue(cellZ, cellY, tx, ty, tz));
                                double noodle = noiseRouter.getNoodleDensityFromInterpolated(
                                        noodleToggleInterpolator.getValue(cellZ, cellY, tx, ty, tz),
                                        noodleThicknessInterpolator.getValue(cellZ, cellY, tx, ty, tz),
                                        noodleRidgeAInterpolator.getValue(cellZ, cellY, tx, ty, tz),
                                        noodleRidgeBInterpolator.getValue(cellZ, cellY, tx, ty, tz));
                                double density = Math.min(routed, noodle);
                                boolean narrowSurfaceEntrance = false;
                                boolean caveEntranceControls = false;
                                /*
                                 * 入口资格严格沿用官方 entrances() = min(CAVE_ENTRANCE, 3D Spaghetti)。
                                 * 当前1.12适配只使用真实Primer表面确定保护带，并排除真实连续水体列；
                                 * 不再以单独子噪声人为截断官方入口。
                                 */
                                if (depthBelowActualSurface <= Configuration.CAVE_GENERATION.surfaceEntranceDepth) {
                                    int routerY = mapRouterYAtBlock(localX, primerY, localZ);
                                    double caveEntranceDensity = noiseRouter.getCaveEntranceDensity(originX + localX, routerY,
                                            originZ + localZ);
                                    double entranceSpaghettiDensity = noiseRouter.getEntranceSpaghettiDensity(originX + localX, routerY,
                                            originZ + localZ);
                                    double entranceDensity = Math.min(caveEntranceDensity, entranceSpaghettiDensity);
                                    if (density <= 0.0D) shallowNegativeCandidates++;
                                                                        if (density <= 0.0D
                                            && entranceDensity <= Configuration.CAVE_GENERATION.entranceDensityThreshold

                                            && surfaceFluidBottom < 0) {

                                        /* 用户要求河流、海洋和湖泊列不生成洞穴入口。官方中水体与
                                         * 地表密度同源；迁移架构下以真实Primer连续液柱作为等价排除。 */
                                        narrowSurfaceEntrance = true;
                                        caveEntranceControls = caveEntranceDensity <= entranceSpaghettiDensity;
                                        entranceAccepted++;
                                    } else if (density <= 0.0D) {
                                        entranceRejected++;
                                    }
                                }
                                /* 1.12地表并不参与1.18的sloped_cheese密度场。为避免普通
                                 * Cheese、Spaghetti或Noodle沿浅层负密度直接穿出地面，保护带内
                                 * 只有官方entrances()分支可以写空气；不再使用会留下缝隙的密度偏置。 */
                                if (depthBelowActualSurface <= Configuration.CAVE_GENERATION.surfaceEntranceDepth) {
                                    if (!narrowSurfaceEntrance) {
                                        if (density <= 0.0D) topLayerRejected++;
                                        continue;
                                    }
                                } else if (density > 0.0D) {
                                    continue;
                                }

                                /* 不能让普通Cheese/Spaghetti从原版海、河、湖的连续水柱底部横向
                                 * 切开。官方Aquifer依靠同一密度场和压力边界保留此类岩层；迁移
                                 * 架构中仅以真实Primer水体补足该约束，且不对已知入口分支回填。 */
                                if (!narrowSurfaceEntrance && surfaceFluidBottom >= 0
                                        && primerY >= surfaceFluidBottom - Configuration.CAVE_GENERATION.surfaceFluidProtectionDepth
                                        && primerY < actualSurface) {
                                    topLayerRejected++;
                                    continue;
                                }

                                /* 近地表空腔可保持干燥，但Aquifer不允许向洞口或表层灌水。 */
                                if (depthBelowActualSurface < Configuration.CAVE_GENERATION.aquiferMinimumDepth) {
                                    if (density <= -0.02D || narrowSurfaceEntrance) {
                                        primer.setBlockState(localX, primerY, localZ, Blocks.AIR.getDefaultState());
                                        if (depthBelowActualSurface <= Configuration.CAVE_GENERATION.surfaceEntranceDepth) {
                                            shallowAirWritten++;
                                            entranceAirColumns[surfaceIndex] = true;
                                            entranceMinimumDepth = Math.min(entranceMinimumDepth, depthBelowActualSurface);
                                            entranceMaximumDepth = Math.max(entranceMaximumDepth, depthBelowActualSurface);
                                            if (depthBelowActualSurface <= 4) {
                                                entranceMouthAirWritten++;
                                                entranceMouthColumns[surfaceIndex] = true;
                                                WorldgenDiagnostics118.recordEntranceMouth(noiseRouter.getWorldSeed(),
                                                        originX + localX, primerY, originZ + localZ);
                                                if (caveEntranceControls) {
                                                    entranceMouthCaveEntranceSignal++;
                                                } else {
                                                    entranceMouthSpaghettiSignal++;
                                                }
                                            }
                                        }
                                    }
                                    continue;
                                }

                                IBlockState substance = aquifer.computeSubstance(originX + localX, primerY,
                                        originZ + localZ, density);
                                if (substance != null) {
                                    primer.setBlockState(localX, primerY, localZ, substance);
                                }
                                /* 与官方MaterialRule一致：Aquifer压力屏障返回null时维持实心材料；
                                 * 不使用density<=-0.18的非官方二次空气回退。 */
                            }
                        }
                    }
                }
            }
        }
        /* 不执行洞室填石、Chunk局部流体封口或其他后处理。官方NoiseChunk只以统一密度
         * 与Aquifer材料选择决定空腔；1.12适配仅在上方循环中读取真实Primer地表/水体边界。 */
        WorldgenDiagnostics118.recordEntrance(noiseRouter.getWorldSeed(), shallowNegativeCandidates, entranceAccepted,
                entranceRejected, topLayerRejected, shallowAirWritten,
                countMarkedColumns(entranceAirColumns), entranceMouthAirWritten,
                countMarkedColumns(entranceMouthColumns),
                entranceMouthCaveEntranceSignal, entranceMouthSpaghettiSignal,
                entranceMinimumDepth == Integer.MAX_VALUE ? -1 : entranceMinimumDepth, entranceMaximumDepth);
        if (diagnostic) {
            LOGGER.info("[RetroFutureLushCave][ENTRANCE_DIAG] chunk={},{} shallowNegative={} entranceAccepted={} entranceRejected={} topLayerRejected={} shallowAirWritten={} threshold={} depth={}",
                    originX >> 4, originZ >> 4, shallowNegativeCandidates, entranceAccepted, entranceRejected, topLayerRejected, shallowAirWritten,
                    Configuration.CAVE_GENERATION.entranceDensityThreshold, Configuration.CAVE_GENERATION.surfaceEntranceDepth);
        }
    }

    private static int countMarkedColumns(boolean[] columns) {
        int count = 0;
        for (boolean marked : columns) {
            if (marked) count++;
        }
        return count;
    }

    private static boolean isSurfaceFluid(IBlockState state) {
        return state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER
                || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA;
    }

    /** 返回真实原版地表连续液柱的最低Y；无水体列时为-1。只读取当前Primer列。 */
    private static int findSurfaceFluidBottom(ChunkPrimer primer, int localX, int actualSurface, int localZ) {
        int topY = Math.min(NoiseGeneratorSettings118.PRIMER_HEIGHT - 1, actualSurface - 1);
        if (topY < 0 || !isSurfaceFluid(primer.getBlockState(localX, topY, localZ))) return -1;
        int bottomY = topY;
        while (bottomY > 0 && isSurfaceFluid(primer.getBlockState(localX, bottomY - 1, localZ))) {
            bottomY--;
        }
        return bottomY;
    }

    private void fillAllInterpolators(int cellX, boolean first) {
        finalDensityInterpolator.fillSlice(cellX, first);
        noodleToggleInterpolator.fillSlice(cellX, first);
        noodleThicknessInterpolator.fillSlice(cellX, first);
        noodleRidgeAInterpolator.fillSlice(cellX, first);
        noodleRidgeBInterpolator.fillSlice(cellX, first);
    }

    private void swapAllInterpolators() {
        finalDensityInterpolator.swapSlices();
        noodleToggleInterpolator.swapSlices();
        noodleThicknessInterpolator.swapSlices();
        noodleRidgeAInterpolator.swapSlices();
        noodleRidgeBInterpolator.swapSlices();
    }

    /*
     * 官方NoiseChunk对整个区块使用一套连续的三维router坐标。不得按真实表面逐列平移或
     * 缩放Y，否则相邻列会采到不同高度切片并把Cheese/Aquifer重排成异常大空腔。真实
     * 1.12地形仅用于浅层入口资格、水体列与装饰的边界判定，不参与噪声坐标变换。
     */
    private int mapRouterYAtCorner(int worldX, int rawRouterY, int worldZ) {
        return rawRouterY;
    }

    private int mapRouterYAtBlock(int localX, int primerY, int localZ) {
        return primerY + NoiseGeneratorSettings118.ROUTER_MIN_Y;
    }

    private static final class NoiseInterpolator118 {
        @FunctionalInterface
        interface NoiseSampler { double sample(int x, int routerY, int z); }

        private final NoiseSampler sampler;
        private final int originX;
        private final int originZ;
        private double[][] slice0 = allocateSlice();
        private double[][] slice1 = allocateSlice();

        NoiseInterpolator118(NoiseSampler sampler, int originX, int originZ) {
            this.sampler = sampler;
            this.originX = originX;
            this.originZ = originZ;
        }

        private static double[][] allocateSlice() {
            return new double[CELL_COUNT_XZ + 1][CELL_COUNT_Y + 1];
        }

        void fillSlice(int cellX, boolean first) {
            double[][] target = first ? slice0 : slice1;
            int worldX = originX + cellX * NoiseGeneratorSettings118.CELL_WIDTH;
            for (int cellZ = 0; cellZ <= CELL_COUNT_XZ; cellZ++) {
                int worldZ = originZ + cellZ * NoiseGeneratorSettings118.CELL_WIDTH;
                for (int cellY = 0; cellY <= CELL_COUNT_Y; cellY++) {
                    int rawRouterY = NoiseGeneratorSettings118.ROUTER_MIN_Y
                            + cellY * NoiseGeneratorSettings118.CELL_HEIGHT;
                    target[cellZ][cellY] = sampler.sample(worldX, rawRouterY, worldZ);
                }
            }
        }

        void swapSlices() {
            double[][] swap = slice0;
            slice0 = slice1;
            slice1 = swap;
        }

        double getValue(int cellZ, int cellY, double tx, double ty, double tz) {
            double a00 = lerp(ty, slice0[cellZ][cellY], slice0[cellZ][cellY + 1]);
            double a10 = lerp(ty, slice1[cellZ][cellY], slice1[cellZ][cellY + 1]);
            double a01 = lerp(ty, slice0[cellZ + 1][cellY], slice0[cellZ + 1][cellY + 1]);
            double a11 = lerp(ty, slice1[cellZ + 1][cellY], slice1[cellZ + 1][cellY + 1]);
            return lerp(tz, lerp(tx, a00, a10), lerp(tx, a01, a11));
        }

        private static double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }
    }
}
