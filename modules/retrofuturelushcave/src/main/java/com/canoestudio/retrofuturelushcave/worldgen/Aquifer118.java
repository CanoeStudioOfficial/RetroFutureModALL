package com.canoestudio.retrofuturelushcave.worldgen;

import com.canoestudio.retrofuturelushcave.worldgen.noise.NormalNoise118;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Random;

/**
 * 1.18.2 {@code Aquifer}/{@code Aquifer.NoiseBasedAquifer} 的1.12.2移植。
 *
 * <p>核心思路：每个方块位置不是直接按density&lt;0判定为AIR，而是先看它离哪几个
 * "含水层网格点"最近，每个网格点各自独立算出一个局部水位/流体类型；点与点之间用
 * BARRIER噪声做出一层"压力屏障"，避免不同水位/水与岩浆的交界处出现瞬间反应或者
 * 生硬的直线边界。</p>
 *
 * <p>调用方式：原来"density&lt;0就设AIR"的地方，改成调用
 * {@link #computeSubstance(int, int, int, double)}——返回null表示维持原有实心方块
 * 不动，返回非null表示应该设置成这个流体/空气状态。</p>
 */
public final class Aquifer118 {
    private static final int X_RANGE = 10;
    private static final int Y_RANGE = 9;
    private static final int Z_RANGE = 10;
    private static final int X_SEPARATION = 6;
    private static final int Y_SEPARATION = 3;
    private static final int Z_SEPARATION = 6;
    private static final int X_SPACING = 16;
    private static final int Y_SPACING = 12;
    private static final int Z_SPACING = 16;
    private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;

    private static final double FLOWING_UPDATE_SIMILARITY = similarity(square(10), square(12));

    public static final int NEVER_FLOODED_LEVEL = Integer.MIN_VALUE;
    /** 1.18 routerY=-10经primerY=routerY+64迁移后的物理高度。 */
    private static final int MIGRATED_LAVA_NOISE_MAX_Y = -10 - NoiseGeneratorSettings118.ROUTER_MIN_Y;

    public interface SurfaceHeightProvider {
        int getSurfaceHeight(int worldX, int worldZ);
    }

    public interface FluidPicker {
        FluidStatus computeFluid(int x, int y, int z);
    }

    public static final class FluidStatus {
        final int fluidLevel;
        final IBlockState fluidType;

        public FluidStatus(int fluidLevel, IBlockState fluidType) {
            this.fluidLevel = fluidLevel;
            this.fluidType = fluidType;
        }

        public IBlockState at(int y) {
            return y < this.fluidLevel ? this.fluidType : Blocks.AIR.getDefaultState();
        }
    }

    public static FluidPicker createOverworldFluidPicker(int seaLevelY, int lavaThresholdY) {
        final FluidStatus lava = new FluidStatus(lavaThresholdY, Blocks.LAVA.getDefaultState());
        final FluidStatus water = new FluidStatus(seaLevelY, Blocks.WATER.getDefaultState());
        final int cutoff = Math.min(lavaThresholdY, seaLevelY);
        return (x, y, z) -> y < cutoff ? lava : water;
    }

        private final long positionalSeed;

    private final SurfaceHeightProvider surfaceHeightProvider;
    private final FluidPicker globalFluidPicker;
    private final NormalNoise118 barrierNoise;
    private final NormalNoise118 fluidLevelFloodednessNoise;
    private final NormalNoise118 fluidLevelSpreadNoise;
        private final NormalNoise118 lavaNoise;
    /** true时跳过地表邻接/海面泛洪，只保留地下局部水位、压力隔墙和熔岩。 */
    private final boolean undergroundOnly;

    private final int minGridX, minGridY, minGridZ;

    private final int gridSizeX, gridSizeZ;
    private final FluidStatus[] aquiferCache;
    private final long[] aquiferLocationCache;

    private boolean shouldScheduleFluidUpdate;

    private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = {
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1},
            {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0},
            {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

        public Aquifer118(long positionalSeed,

                      int chunkX, int chunkZ, int minY, int height,
                      SurfaceHeightProvider surfaceHeightProvider,
                      FluidPicker globalFluidPicker,
                      NormalNoise118 barrierNoise,
                                            NormalNoise118 fluidLevelFloodednessNoise,
                      NormalNoise118 fluidLevelSpreadNoise,
                      NormalNoise118 lavaNoise,
                      boolean undergroundOnly) {

                this.positionalSeed = positionalSeed;

        this.surfaceHeightProvider = surfaceHeightProvider;
        this.globalFluidPicker = globalFluidPicker;
        this.barrierNoise = barrierNoise;
        this.fluidLevelFloodednessNoise = fluidLevelFloodednessNoise;
        this.fluidLevelSpreadNoise = fluidLevelSpreadNoise;
                this.lavaNoise = lavaNoise;
        this.undergroundOnly = undergroundOnly;

        int minBlockX = chunkX << 4;

        int maxBlockX = minBlockX + 15;
        int minBlockZ = chunkZ << 4;
        int maxBlockZ = minBlockZ + 15;

        this.minGridX = gridX(minBlockX) - 1;
        int maxGridX = gridX(maxBlockX) + 1;
        this.gridSizeX = maxGridX - this.minGridX + 1;

        this.minGridY = gridY(minY) - 1;
        int maxGridY = gridY(minY + height) + 1;
        int gridSizeY = maxGridY - this.minGridY + 1;

        this.minGridZ = gridZ(minBlockZ) - 1;
        int maxGridZ = gridZ(maxBlockZ) + 1;
        this.gridSizeZ = maxGridZ - this.minGridZ + 1;

        int total = this.gridSizeX * gridSizeY * this.gridSizeZ;
        this.aquiferCache = new FluidStatus[total];
        this.aquiferLocationCache = new long[total];
        java.util.Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
    }

    public boolean shouldScheduleFluidUpdate() {
        return this.shouldScheduleFluidUpdate;
    }

    public IBlockState computeSubstance(int x, int y, int z, double density) {
        if (density > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }

        FluidStatus globalFluid = this.globalFluidPicker.computeFluid(x, y, z);
        if (globalFluid.at(y).getBlock() == Blocks.LAVA) {
            this.shouldScheduleFluidUpdate = false;
            return Blocks.LAVA.getDefaultState();
        }

        int l = Math.floorDiv(x - 5, X_SPACING);
        int i1 = Math.floorDiv(y + 1, Y_SPACING);
        int j1 = Math.floorDiv(z - 5, Z_SPACING);

        int dist1 = Integer.MAX_VALUE, dist2 = Integer.MAX_VALUE, dist3 = Integer.MAX_VALUE;
        long point1 = 0L, point2 = 0L, point3 = 0L;

        for (int di = 0; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                for (int dk = 0; dk <= 1; dk++) {
                    int gx = l + di;
                    int gy = i1 + dj;
                    int gz = j1 + dk;
                    long packed = getOrComputeJitteredPoint(gx, gy, gz);

                    int px = unpackX(packed);
                    int py = unpackY(packed);
                    int pz = unpackZ(packed);
                    int dx = px - x, dy = py - y, dz = pz - z;
                    int distSq = dx * dx + dy * dy + dz * dz;

                    if (dist1 >= distSq) {
                        point3 = point2; point2 = point1; point1 = packed;
                        dist3 = dist2; dist2 = dist1; dist1 = distSq;
                    } else if (dist2 >= distSq) {
                        point3 = point2; point2 = packed;
                        dist3 = dist2; dist2 = distSq;
                    } else if (dist3 >= distSq) {
                        point3 = packed;
                        dist3 = distSq;
                    }
                }
            }
        }

        FluidStatus status1 = getAquiferStatus(point1);
        double similarity12 = similarity(dist1, dist2);
        IBlockState resultState = status1.at(y);

        if (similarity12 <= 0.0D) {
            this.shouldScheduleFluidUpdate = similarity12 >= FLOWING_UPDATE_SIMILARITY;
            return resultState;
        }

        if (resultState.getBlock() == Blocks.WATER
                && this.globalFluidPicker.computeFluid(x, y - 1, z).at(y - 1).getBlock() == Blocks.LAVA) {
            this.shouldScheduleFluidUpdate = true;
            return resultState;
        }

        FluidStatus status2 = getAquiferStatus(point2);
        FluidStatus status3 = getAquiferStatus(point3);

        double d10_12 = computeD10(y, status1, status2);
        double d10_13 = computeD10(y, status1, status3);
        double d10_23 = computeD10(y, status2, status3);

        boolean needsBarrier = isBarrierRequired(d10_12)
                || isBarrierRequired(d10_13)
                || isBarrierRequired(d10_23);

        double barrierValue = Double.NaN;
        if (needsBarrier) {
            barrierValue = this.barrierNoise.getValue(x * 0.5D, y * 0.5D, z * 0.5D);
        }

        double pressure12 = similarity12 * pressureFromD10(d10_12, barrierValue);
        if (density + pressure12 > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }

        double similarity13 = similarity(dist1, dist3);
        if (similarity13 > 0.0D) {
            double pressure13 = similarity12 * similarity13 * pressureFromD10(d10_13, barrierValue);
            if (density + pressure13 > 0.0D) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
        }

        double similarity23 = similarity(dist2, dist3);
        if (similarity23 > 0.0D) {
            double pressure23 = similarity12 * similarity23 * pressureFromD10(d10_23, barrierValue);
            if (density + pressure23 > 0.0D) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
        }

        this.shouldScheduleFluidUpdate = true;
        return resultState;
    }

    private static double computeD10(int y, FluidStatus a, FluidStatus b) {
        IBlockState stateA = a.at(y);
        IBlockState stateB = b.at(y);
        boolean lavaWaterPair = (stateA.getBlock() == Blocks.LAVA && stateB.getBlock() == Blocks.WATER)
                || (stateA.getBlock() == Blocks.WATER && stateB.getBlock() == Blocks.LAVA);
        if (lavaWaterPair) {
            return Double.POSITIVE_INFINITY;
        }

        long levelDiffLong = Math.abs((long) a.fluidLevel - (long) b.fluidLevel);
        if (levelDiffLong == 0L) {
            return Double.NaN; // 等级相等，压力为 0，不需要 barrier
        }
        double levelDiff = (double) levelDiffLong;

        double midpoint = 0.5D * ((double) a.fluidLevel + (double) b.fluidLevel);
        double offsetFromMid = (double) y + 0.5D - midpoint;
        double halfDiff = levelDiff / 2.0D;
        double band = halfDiff - Math.abs(offsetFromMid);

        double d10;
        if (offsetFromMid > 0.0D) {
            double v = band;
            d10 = v > 0.0D ? v / 1.5D : v / 2.5D;
        } else {
            double v = 3.0D + band;
            d10 = v > 0.0D ? v / 3.0D : v / 10.0D;
        }
        return d10;
    }

    private static boolean isBarrierRequired(double d10) {
        return !Double.isNaN(d10) && d10 != Double.POSITIVE_INFINITY
                && d10 >= -2.0D && d10 <= 2.0D;
    }

    private static double pressureFromD10(double d10, double barrierValue) {
        if (Double.isNaN(d10)) {
            return 0.0D;
        }
        if (d10 == Double.POSITIVE_INFINITY) {
            return 2.0D;
        }
        if (d10 >= -2.0D && d10 <= 2.0D) {
            return 2.0D * (barrierValue + d10);
        }
        return 2.0D * d10;
    }

    // ---------------- 网格 / 缓存 ----------------

    private static int gridX(int blockX) { return Math.floorDiv(blockX, X_SPACING); }
    private static int gridY(int blockY) { return Math.floorDiv(blockY, Y_SPACING); }
    private static int gridZ(int blockZ) { return Math.floorDiv(blockZ, Z_SPACING); }

    private int cacheIndex(int gridX, int gridY, int gridZ) {
        int i = gridX - this.minGridX;
        int j = gridY - this.minGridY;
        int k = gridZ - this.minGridZ;
        return (j * this.gridSizeZ + k) * this.gridSizeX + i;
    }

    private long getOrComputeJitteredPoint(int gridX, int gridY, int gridZ) {
        int idx = cacheIndex(gridX, gridY, gridZ);
        long cached = this.aquiferLocationCache[idx];
        if (cached != Long.MAX_VALUE) {
            return cached;
        }
                Random random = new Random(gridSeed(gridX, gridY, gridZ));

        int px = gridX * X_SPACING + random.nextInt(X_RANGE);
        int py = gridY * Y_SPACING + random.nextInt(Y_RANGE);
        int pz = gridZ * Z_SPACING + random.nextInt(Z_RANGE);
        long packed = packPos(px, py, pz);
        this.aquiferLocationCache[idx] = packed;
        return packed;
    }

        private long gridSeed(int gridX, int gridY, int gridZ) {
        long value = positionalSeed;
        value ^= (long) gridX * 341873128712L;
        value ^= (long) gridY * 132897987541L;
        value ^= (long) gridZ * 42317861L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private FluidStatus getAquiferStatus(long packedPos) {

        int x = unpackX(packedPos);
        int y = unpackY(packedPos);
        int z = unpackZ(packedPos);
        int gx = gridX(x), gy = gridY(y), gz = gridZ(z);
        int idx = cacheIndex(gx, gy, gz);
        FluidStatus cached = this.aquiferCache[idx];
        if (cached != null) {
            return cached;
        }
        FluidStatus computed = computeFluidAt(x, y, z);
        this.aquiferCache[idx] = computed;
        return computed;
    }

        private FluidStatus computeFluidAt(int x, int y, int z) {
        FluidStatus defaultFluid = this.globalFluidPicker.computeFluid(x, y, z);
        if (this.undergroundOnly) {
            return computeUndergroundFluidAt(x, y, z, defaultFluid);
        }

        int minSurface = Integer.MAX_VALUE;

        int upperBound = y + 12;
        int lowerBound = y - 12;
        boolean nearOwnSurfaceFlood = false;

        for (int[] offset : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
            int sampleX = x + offset[0] * 16;
            int sampleZ = z + offset[1] * 16;
            int surfaceY = this.surfaceHeightProvider.getSurfaceHeight(sampleX, sampleZ);
            int surfacePlus8 = surfaceY + 8;
            boolean isCenter = offset[0] == 0 && offset[1] == 0;

            if (isCenter && lowerBound > surfacePlus8) {
                return defaultFluid;
            }

            boolean aboveNeighborSurface = upperBound > surfacePlus8;
            if (aboveNeighborSurface || isCenter) {
                FluidStatus neighborFluid = this.globalFluidPicker.computeFluid(sampleX, surfacePlus8, sampleZ);
                if (neighborFluid.at(surfacePlus8).getBlock() != Blocks.AIR) {
                    if (isCenter) {
                        nearOwnSurfaceFlood = true;
                    }
                    if (aboveNeighborSurface) {
                        return neighborFluid;
                    }
                }
            }
            minSurface = Math.min(minSurface, surfaceY);
        }

        int depthBelowSurface = minSurface + 8 - y;
        double surfaceCloseness = nearOwnSurfaceFlood
                ? clampedMap(depthBelowSurface, 0.0D, 64.0D, 1.0D, 0.0D)
                : 0.0D;

        double floodedness = clamp(
                this.fluidLevelFloodednessNoise.getValue(x * 0.67D, y * 0.67D, z * 0.67D), -1.0D, 1.0D);

        double upperThreshold = map(surfaceCloseness, 1.0D, 0.0D, -0.3D, 0.8D);
        if (floodedness > upperThreshold) {
            return defaultFluid;
        }

        double lowerThreshold = map(surfaceCloseness, 1.0D, 0.0D, -0.8D, 0.4D);
        if (floodedness <= lowerThreshold) {
            return new FluidStatus(NEVER_FLOODED_LEVEL, defaultFluid.fluidType);
        }

        int gx16 = Math.floorDiv(x, 16);
        int gy40 = Math.floorDiv(y, 40);
        int gz16 = Math.floorDiv(z, 16);
        int bandCenter = gy40 * 40 + 20;
        double spread = this.fluidLevelSpreadNoise.getValue(
                gx16 * 0.7142857142857143D, gy40 * 0.7142857142857143D, gz16 * 0.7142857142857143D) * 10.0D;
        int quantizedSpread = quantize(spread, 3);
        int candidateLevel = bandCenter + quantizedSpread;
        int finalLevel = Math.min(minSurface, candidateLevel);

        if (finalLevel <= -10) {
            int lx64 = Math.floorDiv(x, 64);
            int ly40 = Math.floorDiv(y, 40);
            int lz64 = Math.floorDiv(z, 64);
            double lavaValue = this.lavaNoise.getValue(lx64 * 1.0D, ly40 * 1.0D, lz64 * 1.0D);
            if (Math.abs(lavaValue) > 0.3D) {
                return new FluidStatus(finalLevel, Blocks.LAVA.getDefaultState());
            }
        }

        return new FluidStatus(finalLevel, defaultFluid.fluidType);
    }

    /**
     * 迁移模式下的Aquifer不再读取地表高度，不会向原版海洋、河流或地表洞口延展水体。
     * 阈值采用原版“远离地表”分支，仍保留floodedness、spread及深层熔岩噪声。
     */
    private FluidStatus computeUndergroundFluidAt(int x, int y, int z, FluidStatus defaultFluid) {
        double floodedness = clamp(
                this.fluidLevelFloodednessNoise.getValue(x * 0.67D, y * 0.67D, z * 0.67D), -1.0D, 1.0D);
        if (floodedness > 0.8D) return defaultFluid;
        if (floodedness <= 0.4D) return new FluidStatus(NEVER_FLOODED_LEVEL, defaultFluid.fluidType);

        int gx16 = Math.floorDiv(x, 16);
        int gy40 = Math.floorDiv(y, 40);
        int gz16 = Math.floorDiv(z, 16);
        int bandCenter = gy40 * 40 + 20;
        double spread = this.fluidLevelSpreadNoise.getValue(
                gx16 * 0.7142857142857143D, gy40 * 0.7142857142857143D,
                gz16 * 0.7142857142857143D) * 10.0D;
        int finalLevel = bandCenter + quantize(spread, 3);

        if (finalLevel <= MIGRATED_LAVA_NOISE_MAX_Y) {
            double lavaValue = this.lavaNoise.getValue(Math.floorDiv(x, 64), Math.floorDiv(y, 40), Math.floorDiv(z, 64));
            if (Math.abs(lavaValue) > 0.3D) return new FluidStatus(finalLevel, Blocks.LAVA.getDefaultState());
        }
        return new FluidStatus(finalLevel, defaultFluid.fluidType);
    }

    // ---------------- 坐标打包 ----------------

    private static final int XZ_BITS = 26;
    private static final int Y_BITS = 9;
    private static final int Y_OFFSET = 256;
    private static final long XZ_OFFSET = 1L << (XZ_BITS - 1);
    private static final long XZ_MASK = (1L << XZ_BITS) - 1;
    private static final long Y_MASK = (1L << Y_BITS) - 1;

    private static long packPos(int x, int y, int z) {
        long lx = (x + XZ_OFFSET) & XZ_MASK;
        long ly = (y + Y_OFFSET) & Y_MASK;
        long lz = (z + XZ_OFFSET) & XZ_MASK;
        return (lx << (Y_BITS + XZ_BITS)) | (ly << XZ_BITS) | lz;
    }

    private static int unpackX(long packed) {
        long lx = (packed >>> (Y_BITS + XZ_BITS)) & XZ_MASK;
        return (int) (lx - XZ_OFFSET);
    }

    private static int unpackY(long packed) {
        return (int) (((packed >>> XZ_BITS) & Y_MASK) - Y_OFFSET);
    }

    private static int unpackZ(long packed) {
        long lz = packed & XZ_MASK;
        return (int) (lz - XZ_OFFSET);
    }

    // ---------------- 数学工具 ----------------

    private static double similarity(int a, int b) {
        return 1.0D - (double) Math.abs(b - a) / 25.0D;
    }

    private static int square(int v) { return v * v; }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static double map(double value, double oldMin, double oldMax, double newMin, double newMax) {
        return newMin + (value - oldMin) * (newMax - newMin) / (oldMax - oldMin);
    }

    private static double clampedMap(double value, double oldMin, double oldMax, double newMin, double newMax) {
        double t = clamp((value - oldMin) / (oldMax - oldMin), 0.0D, 1.0D);
        return newMin + t * (newMax - newMin);
    }

    private static int quantize(double value, int multiplier) {
        return (int) (value / (double) multiplier) * multiplier;
    }
}