package com.canoestudio.retrofuturelushcave.worldgen;

import com.canoestudio.retrofuturelushcave.config.Configuration;

import static com.canoestudio.retrofuturelushcave.RetroFutureLushCave.LOGGER;

/**
 * 可选的世界生成采样诊断。
 *
 * <p>不记录方块坐标，不影响随机数，也不对每个Chunk逐行输出。每累计
 * {@value #SAMPLE_CHUNKS} 个新生成Chunk，输出一条跨区域汇总，用于将当前有效密度与
 * 1.18.2 PlacedFeature/Carver配置对照。所有方法同步，以兼容可能的异步地形生成环境。</p>
 */
public final class WorldgenDiagnostics118 {
    public static final int SAMPLE_CHUNKS = 128;

    private static long activeSeed = Long.MIN_VALUE;
    private static int sampledChunks;

    private static long entranceShallowNegative;
    private static long entranceAccepted;
    private static long entranceRejected;
    private static long entranceTopRejected;
    private static long entranceAirWritten;
    private static long entranceColumns;
    private static long entranceMouthAirWritten;
    private static long entranceMouthColumns;
    private static long entranceMouthCaveEntranceSignal;
    private static long entranceMouthSpaghettiSignal;
    private static long entranceNearestMouthDistanceSq = Long.MAX_VALUE;
    private static int entranceNearestMouthX;
    private static int entranceNearestMouthY;
    private static int entranceNearestMouthZ;
    private static int entranceMinimumDepth = Integer.MAX_VALUE;
    private static int entranceMaximumDepth = -1;

    private static long canyonSourceCalls;
    private static long canyonAccepted;
    private static long canyonSurfaceBlocks;
    private static long canyonSurfaceColumns;

    private static long dripstoneSmallAttempts;
    private static long dripstoneSmallFloorHits;
    private static long dripstoneSmallRegionHits;
    private static long dripstoneSmallCalls;
    private static long dripstoneLargeAttempts;
    private static long dripstoneLargeFloorHits;
    private static long dripstoneLargeRegionHits;
    private static long dripstoneLargeCalls;
    private static long dripstonePools;

    private static long lushCeilingCandidates;
    private static long lushCeilingScanHits;
    private static long lushCeilingEnclosedHits;
    private static long lushCeilingRegionHits;
    private static long lushFloorCandidates;
    private static long lushFloorScanHits;
    private static long lushFloorEnclosedHits;
    private static long lushFloorRegionHits;
    private static long lushClayCandidates;
    private static long lushClayScanHits;
    private static long lushClayEnclosedHits;
    private static long lushClayRegionHits;

    private static long magmaAttempts;
    private static long magmaBelowOceanFloor;
    private static long magmaFloorHits;
    private static long magmaPlaced;

    /* RootSystemFeature：所有计数均是已进入当前Chunk的候选，未记录坐标。 */
    private static long rootAttempts;
    private static long rootCeilingHits;
    private static long rootLushHits;
    private static long rootTreeSuccesses;
    private static long rootColumnBlocks;
    private static long hangingRootsWritten;

    /* 1.12 primer阶段流体本身是静态状态；这些计数记录载入后被安排重新计算的暴露流体。 */
    private static long fluidUpdateChunks;
    private static long fluidUpdateWaterSources;
    private static long fluidUpdateLavaSources;

    private WorldgenDiagnostics118() {
    }

    public static synchronized void recordEntrance(long seed, int shallowNegative, int accepted, int rejected,
                                                    int topRejected, int airWritten, int columns,
                                                    int mouthAirWritten, int mouthColumns,
                                                    int mouthCaveEntranceSignal, int mouthSpaghettiSignal,
                                                    int minimumDepth, int maximumDepth) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        sampledChunks++;
        entranceShallowNegative += shallowNegative;
        entranceAccepted += accepted;
        entranceRejected += rejected;
        entranceTopRejected += topRejected;
        entranceAirWritten += airWritten;
        entranceColumns += columns;
        entranceMouthAirWritten += mouthAirWritten;
        entranceMouthColumns += mouthColumns;
        entranceMouthCaveEntranceSignal += mouthCaveEntranceSignal;
        entranceMouthSpaghettiSignal += mouthSpaghettiSignal;
        if (minimumDepth >= 0) entranceMinimumDepth = Math.min(entranceMinimumDepth, minimumDepth);
        if (maximumDepth >= 0) entranceMaximumDepth = Math.max(entranceMaximumDepth, maximumDepth);
        if (sampledChunks >= SAMPLE_CHUNKS) {
            logAndReset(seed);
        }
    }

    public static synchronized void recordEntranceMouth(long seed, int x, int y, int z) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        long distanceSq = (long) x * (long) x + (long) z * (long) z;
        if (distanceSq < entranceNearestMouthDistanceSq) {
            entranceNearestMouthDistanceSq = distanceSq;
            entranceNearestMouthX = x;
            entranceNearestMouthY = y;
            entranceNearestMouthZ = z;
        }
    }

    public static synchronized void recordCanyonSource(long seed, boolean accepted) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        canyonSourceCalls++;
        if (accepted) canyonAccepted++;
    }

    public static synchronized void recordCanyonSurfaceCarve(long seed, int surfaceBlocks, int surfaceColumns) {
        if (!Configuration.DEBUG.enableWorldgenDebug
                || (surfaceBlocks <= 0 && surfaceColumns <= 0)) return;
        resetIfSeedChanged(seed);
        canyonSurfaceBlocks += surfaceBlocks;
        canyonSurfaceColumns += surfaceColumns;
    }

    public static synchronized void recordLushVegetation(long seed,
                                                          int ceilingCandidates, int ceilingScanHits,
                                                          int ceilingEnclosedHits, int ceilingRegionHits,
                                                          int floorCandidates, int floorScanHits,
                                                          int floorEnclosedHits, int floorRegionHits,
                                                          int clayCandidates, int clayScanHits,
                                                          int clayEnclosedHits, int clayRegionHits) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        lushCeilingCandidates += ceilingCandidates;
        lushCeilingScanHits += ceilingScanHits;
        lushCeilingEnclosedHits += ceilingEnclosedHits;
        lushCeilingRegionHits += ceilingRegionHits;
        lushFloorCandidates += floorCandidates;
        lushFloorScanHits += floorScanHits;
        lushFloorEnclosedHits += floorEnclosedHits;
        lushFloorRegionHits += floorRegionHits;
        lushClayCandidates += clayCandidates;
        lushClayScanHits += clayScanHits;
        lushClayEnclosedHits += clayEnclosedHits;
        lushClayRegionHits += clayRegionHits;
    }

    public static synchronized void recordDripstone(long seed, int smallAttempts, int smallFloorHits,
                                                     int smallRegionHits, int smallCalls, int largeAttempts,
                                                     int largeFloorHits, int largeRegionHits, int largeCalls) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        dripstoneSmallAttempts += smallAttempts;
        dripstoneSmallFloorHits += smallFloorHits;
        dripstoneSmallRegionHits += smallRegionHits;
        dripstoneSmallCalls += smallCalls;
        dripstoneLargeAttempts += largeAttempts;
        dripstoneLargeFloorHits += largeFloorHits;
        dripstoneLargeRegionHits += largeRegionHits;
        dripstoneLargeCalls += largeCalls;
    }

    public static synchronized void recordDripstonePool(long seed) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        dripstonePools++;
    }

    public static synchronized void recordUnderwaterMagma(long seed, int attempts, int belowOceanFloor,
                                                            int floorHits, int placed) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        magmaAttempts += attempts;
        magmaBelowOceanFloor += belowOceanFloor;
        magmaFloorHits += floorHits;
        magmaPlaced += placed;
    }

    public static synchronized void recordRootSystem(long seed, int attempts, int ceilingHits, int lushHits,
                                                      int treeSuccesses, int columnBlocks, int hangingRoots) {
        if (!Configuration.DEBUG.enableWorldgenDebug) return;
        resetIfSeedChanged(seed);
        rootAttempts += attempts;
        rootCeilingHits += ceilingHits;
        rootLushHits += lushHits;
        rootTreeSuccesses += treeSuccesses;
        rootColumnBlocks += columnBlocks;
        hangingRootsWritten += hangingRoots;
    }

    public static synchronized void recordScheduledFluidUpdates(long seed, int waterSources, int lavaSources) {
        if (!Configuration.DEBUG.enableWorldgenDebug
                || (waterSources <= 0 && lavaSources <= 0)) return;
        resetIfSeedChanged(seed);
        fluidUpdateChunks++;
        fluidUpdateWaterSources += waterSources;
        fluidUpdateLavaSources += lavaSources;
    }

    private static void resetIfSeedChanged(long seed) {
        if (activeSeed == seed) return;
        activeSeed = seed;
        clearCounters();
    }

    private static void logAndReset(long seed) {
        LOGGER.info("[RetroFutureLushCave][WORLDGEN_SAMPLE] seed={} chunks={} "
                        + "entrance{negative={},accepted={},rejected={},topRejected={},air={},columns={},mouthAir={},mouthColumns={},mouthCave={},mouthSpaghetti={},depth={}..{},nearestMouth={}} "
                        + "canyon{sourceCalls={},accepted={},surfaceBlocks={},surfaceColumns={}} "
                        + "dripstone{small={}/{}/{}/{},large={}/{}/{}/{},pools={}} "
                        + "lush{ceiling={}/{}/{}/{},floor={}/{}/{}/{},clay={}/{}/{}/{}} "
                        + "underwaterMagma{attempts={},belowOceanFloor={},floorHits={},placed={}} "
                        + "rootSystem{attempts={},ceilingHits={},lushHits={},treeSuccesses={},columnBlocks={},hangingRoots={}} "
                        + "fluidUpdates{chunks={},water={},lava={}}",
                seed, sampledChunks,
                entranceShallowNegative, entranceAccepted, entranceRejected, entranceTopRejected, entranceAirWritten,
                entranceColumns, entranceMouthAirWritten, entranceMouthColumns,
                entranceMouthCaveEntranceSignal, entranceMouthSpaghettiSignal,
                entranceMinimumDepth == Integer.MAX_VALUE ? -1 : entranceMinimumDepth, entranceMaximumDepth,
                formatNearestMouth(),
                canyonSourceCalls, canyonAccepted, canyonSurfaceBlocks, canyonSurfaceColumns,
                dripstoneSmallAttempts, dripstoneSmallFloorHits, dripstoneSmallRegionHits, dripstoneSmallCalls,
                dripstoneLargeAttempts, dripstoneLargeFloorHits, dripstoneLargeRegionHits, dripstoneLargeCalls, dripstonePools,
                lushCeilingCandidates, lushCeilingScanHits, lushCeilingEnclosedHits, lushCeilingRegionHits,
                lushFloorCandidates, lushFloorScanHits, lushFloorEnclosedHits, lushFloorRegionHits,
                lushClayCandidates, lushClayScanHits, lushClayEnclosedHits, lushClayRegionHits,
                magmaAttempts, magmaBelowOceanFloor, magmaFloorHits, magmaPlaced,
                rootAttempts, rootCeilingHits, rootLushHits, rootTreeSuccesses, rootColumnBlocks, hangingRootsWritten,
                fluidUpdateChunks, fluidUpdateWaterSources, fluidUpdateLavaSources);
        clearCounters();
    }

    private static String formatNearestMouth() {
        return entranceNearestMouthDistanceSq == Long.MAX_VALUE
                ? "none"
                : entranceNearestMouthX + "," + entranceNearestMouthY + "," + entranceNearestMouthZ;
    }

    private static void clearCounters() {
        sampledChunks = 0;
        entranceShallowNegative = 0L;
        entranceAccepted = 0L;
        entranceRejected = 0L;
        entranceTopRejected = 0L;
        entranceAirWritten = 0L;
        entranceColumns = 0L;
        entranceMouthAirWritten = 0L;
        entranceMouthColumns = 0L;
        entranceMouthCaveEntranceSignal = 0L;
        entranceMouthSpaghettiSignal = 0L;
        entranceNearestMouthDistanceSq = Long.MAX_VALUE;
        entranceNearestMouthX = 0;
        entranceNearestMouthY = 0;
        entranceNearestMouthZ = 0;
        entranceMinimumDepth = Integer.MAX_VALUE;
        entranceMaximumDepth = -1;
        canyonSourceCalls = 0L;
        canyonAccepted = 0L;
        canyonSurfaceBlocks = 0L;
        canyonSurfaceColumns = 0L;
        dripstoneSmallAttempts = 0L;
        dripstoneSmallFloorHits = 0L;
        dripstoneSmallRegionHits = 0L;
        dripstoneSmallCalls = 0L;
        dripstoneLargeAttempts = 0L;
        dripstoneLargeFloorHits = 0L;
        dripstoneLargeRegionHits = 0L;
        dripstoneLargeCalls = 0L;
        dripstonePools = 0L;
        lushCeilingCandidates = 0L;
        lushCeilingScanHits = 0L;
        lushCeilingEnclosedHits = 0L;
        lushCeilingRegionHits = 0L;
        lushFloorCandidates = 0L;
        lushFloorScanHits = 0L;
        lushFloorEnclosedHits = 0L;
        lushFloorRegionHits = 0L;
        lushClayCandidates = 0L;
        lushClayScanHits = 0L;
        lushClayEnclosedHits = 0L;
        lushClayRegionHits = 0L;
        magmaAttempts = 0L;
        magmaBelowOceanFloor = 0L;
        magmaFloorHits = 0L;
        magmaPlaced = 0L;
        rootAttempts = 0L;
        rootCeilingHits = 0L;
        rootLushHits = 0L;
        rootTreeSuccesses = 0L;
        rootColumnBlocks = 0L;
        hangingRootsWritten = 0L;
        fluidUpdateChunks = 0L;
        fluidUpdateWaterSources = 0L;
        fluidUpdateLavaSources = 0L;
    }
}
