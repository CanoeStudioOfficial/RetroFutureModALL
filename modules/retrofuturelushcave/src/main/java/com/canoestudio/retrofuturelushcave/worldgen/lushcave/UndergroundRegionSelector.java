package com.canoestudio.retrofuturelushcave.worldgen.lushcave;

import com.canoestudio.retrofuturelushcave.config.Configuration;
import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;

/**
 * 1.12没有垂直BiomeProvider时供地下装饰使用的1.18地下生物群系选择器。
 *
 * <p>它不调用或替换World#getBiome。每一个候选洞穴表面都独立按原版
 * OverworldBiomeBuilder#addUndergroundBiomes的NoiseRouter参数判断，因而同一X/Z列可以
 * 在不同高度属于普通洞穴或地下生物群系；不再使用二维噪声整柱无限延申。</p>
 */
public final class UndergroundRegionSelector {
    public enum Region {
        NONE,
        LUSH,
        DRIPSTONE
    }

    private static final boolean DEBUG_FORCE_LUSH_CAVES = false;
    private static final boolean DEBUG_LUSH_CLIMATE_DISTRIBUTION = false;
    private static final int DIAGNOSTIC_REPORT_INTERVAL_CHUNKS = 1024;
    private static final int[] DIAGNOSTIC_SAMPLE_OFFSETS = {2, 6, 10, 14};
    private static final double[] DIAGNOSTIC_THRESHOLDS =
            {0.70D, 0.60D, 0.50D, 0.40D, 0.30D, 0.20D, 0.00D};

    private static long diagnosticSeed = Long.MIN_VALUE;
    private static long diagnosticChunks;
    private static long diagnosticHumiditySamples;
    private static long diagnosticDepthSamples;
    private static long diagnosticJointSamples;
    private static long diagnosticDepthInRange;
    private static final long[] diagnosticHumidityAtLeast = new long[DIAGNOSTIC_THRESHOLDS.length];
    private static final long[] diagnosticJointAtLeast = new long[DIAGNOSTIC_THRESHOLDS.length];
    private static final long[] diagnosticHumidityHistogram = new long[20];
    private static double diagnosticHumidityMin = Double.POSITIVE_INFINITY;
    private static double diagnosticHumidityMax = Double.NEGATIVE_INFINITY;
    private static double diagnosticDepthMin = Double.POSITIVE_INFINITY;
    private static double diagnosticDepthMax = Double.NEGATIVE_INFINITY;

    private final DensityCave118Generator densityGenerator;

    public UndergroundRegionSelector(DensityCave118Generator densityGenerator) {
        if (densityGenerator == null) {
            throw new IllegalArgumentException("densityGenerator must not be null");
        }
        this.densityGenerator = densityGenerator;
    }

    /**
     * 原版1.18参数：
     * DRIPSTONE = continents 0.8..1.0，depth 0.2..0.9；
     * LUSH = humidity 0.7..1.0，depth 0.2..0.9。LUSH在源码注册顺序中后加入，
     * 在两者同时命中时优先使用LUSH，避免同一面被两套植被覆盖。
     */
    public Region select(int blockX, int primerY, int blockZ) {
        if (DEBUG_FORCE_LUSH_CAVES) {
            return Region.LUSH;
        }
        double depth = densityGenerator.sampleUndergroundBiomeDepth(blockX, primerY, blockZ);
        if (depth < Configuration.LUSH_CAVES.minimumDepth || depth > Configuration.LUSH_CAVES.maximumDepth) {
            return Region.NONE;
        }
        if (densityGenerator.sampleUndergroundHumidity(blockX, blockZ) >= lushHumidityMinimum()) {
            return Region.LUSH;
        }
        if (densityGenerator.sampleUndergroundContinentalness(blockX, blockZ)
                >= Configuration.UNDERGROUND_FEATURES.dripstoneContinentalnessThreshold) {
            return Region.DRIPSTONE;
        }
        return Region.NONE;
    }

    /**
     * 仅在DEBUG_LUSH_CLIMATE_DISTRIBUTION=true时调用。
     * 每个已实际Populate的Chunk取16个X/Z点、每点16个高度，共256个三维样本；连续1024个Chunk
     * 已有262,144个joint样本，能反映当前世界种子而非少数手工坐标。
     */
    public void recordClimateDiagnostic(long worldSeed, int chunkX, int chunkZ) {
        if (!DEBUG_LUSH_CLIMATE_DISTRIBUTION) {
            return;
        }
        synchronized (UndergroundRegionSelector.class) {
            if (diagnosticSeed != worldSeed) {
                resetDiagnostic(worldSeed);
            }
            int baseX = chunkX << 4;
            int baseZ = chunkZ << 4;
            for (int offsetX : DIAGNOSTIC_SAMPLE_OFFSETS) {
                for (int offsetZ : DIAGNOSTIC_SAMPLE_OFFSETS) {
                    int x = baseX + offsetX;
                    int z = baseZ + offsetZ;
                    double humidity = densityGenerator.sampleUndergroundHumidity(x, z);
                    diagnosticHumiditySamples++;
                    diagnosticHumidityMin = Math.min(diagnosticHumidityMin, humidity);
                    diagnosticHumidityMax = Math.max(diagnosticHumidityMax, humidity);
                    diagnosticHumidityHistogram[histogramIndex(humidity)]++;
                    for (int i = 0; i < DIAGNOSTIC_THRESHOLDS.length; i++) {
                        if (humidity >= DIAGNOSTIC_THRESHOLDS[i]) {
                            diagnosticHumidityAtLeast[i]++;
                        }
                    }
                    for (int y = 8; y <= 248; y += 16) {
                        double depth = densityGenerator.sampleUndergroundBiomeDepth(x, y, z);
                        boolean depthValid = depth >= Configuration.LUSH_CAVES.minimumDepth
                                && depth <= Configuration.LUSH_CAVES.maximumDepth;
                        diagnosticDepthSamples++;
                        diagnosticJointSamples++;
                        diagnosticDepthMin = Math.min(diagnosticDepthMin, depth);
                        diagnosticDepthMax = Math.max(diagnosticDepthMax, depth);
                        if (depthValid) {
                            diagnosticDepthInRange++;
                        }
                        for (int i = 0; i < DIAGNOSTIC_THRESHOLDS.length; i++) {
                            if (depthValid && humidity >= DIAGNOSTIC_THRESHOLDS[i]) {
                                diagnosticJointAtLeast[i]++;
                            }
                        }
                    }
                }
            }
            diagnosticChunks++;
            if (diagnosticChunks % DIAGNOSTIC_REPORT_INTERVAL_CHUNKS == 0) {
                printDiagnostic();
            }
        }
    }

    private static void resetDiagnostic(long worldSeed) {
        diagnosticSeed = worldSeed;
        diagnosticChunks = 0L;
        diagnosticHumiditySamples = 0L;
        diagnosticDepthSamples = 0L;
        diagnosticJointSamples = 0L;
        diagnosticDepthInRange = 0L;
        diagnosticHumidityMin = Double.POSITIVE_INFINITY;
        diagnosticHumidityMax = Double.NEGATIVE_INFINITY;
        diagnosticDepthMin = Double.POSITIVE_INFINITY;
        diagnosticDepthMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < DIAGNOSTIC_THRESHOLDS.length; i++) {
            diagnosticHumidityAtLeast[i] = 0L;
            diagnosticJointAtLeast[i] = 0L;
        }
        for (int i = 0; i < diagnosticHumidityHistogram.length; i++) {
            diagnosticHumidityHistogram[i] = 0L;
        }
    }

    private static int histogramIndex(double humidity) {
        int index = (int) Math.floor((humidity + 1.0D) * 10.0D);
        return Math.max(0, Math.min(diagnosticHumidityHistogram.length - 1, index));
    }

    private static void printDiagnostic() {
        StringBuilder output = new StringBuilder(1024);
        output.append("[RetroFutureLushCave][LUSH_DIAG] seed=").append(diagnosticSeed)
                .append(" chunks=").append(diagnosticChunks)
                .append(" humiditySamples=").append(diagnosticHumiditySamples)
                .append(" humidityRange=").append(diagnosticHumidityMin).append("..").append(diagnosticHumidityMax)
                .append(" depthSamples=").append(diagnosticDepthSamples)
                .append(" depthRange=").append(diagnosticDepthMin).append("..").append(diagnosticDepthMax)
                .append(" depth[0.2,0.9]=").append(percent(diagnosticDepthInRange, diagnosticDepthSamples));
        for (int i = 0; i < DIAGNOSTIC_THRESHOLDS.length; i++) {
            output.append(" h>=").append(DIAGNOSTIC_THRESHOLDS[i])
                    .append('=').append(percent(diagnosticHumidityAtLeast[i], diagnosticHumiditySamples))
                    .append(" joint=").append(percent(diagnosticJointAtLeast[i], diagnosticJointSamples));
        }
        output.append(" humidityBins[-1..1)=");
        for (int i = 0; i < diagnosticHumidityHistogram.length; i++) {
            if (i > 0) output.append(',');
            output.append(diagnosticHumidityHistogram[i]);
        }
        System.out.println(output.toString());
    }

    private static String percent(long count, long total) {
        if (total <= 0L) return "n/a";
        long scaled = count * 1000000L / total;
        return (scaled / 10000L) + "." + pad4(scaled % 10000L) + "%";
    }

    private static String pad4(long value) {
        if (value < 10L) return "000" + value;
        if (value < 100L) return "00" + value;
        if (value < 1000L) return "0" + value;
        return String.valueOf(value);
    }

    /** 只检查与Y无关的原版LUSH humidity门槛，用于避免普通列的无效全高度扫描。 */
    public boolean hasLushHumidity(int blockX, int blockZ) {
        return DEBUG_FORCE_LUSH_CAVES
                || densityGenerator.sampleUndergroundHumidity(blockX, blockZ) >= lushHumidityMinimum();
    }

    private static double lushHumidityMinimum() {
        return Configuration.LUSH_CAVES.vanillaHumidityThreshold;
    }

    public boolean isLushAt(int blockX, int primerY, int blockZ) {
        return select(blockX, primerY, blockZ) == Region.LUSH;
    }

    public boolean isDripstoneAt(int blockX, int primerY, int blockZ) {
        return select(blockX, primerY, blockZ) == Region.DRIPSTONE;
    }
}
