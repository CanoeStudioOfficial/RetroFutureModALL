package com.canoestudio.retrofuturelushcave.worldgen;


import com.canoestudio.retrofuturelushcave.worldgen.noise.NormalNoise118;
import java.util.Random;

/**
 * 1.18.2 Overworld 密度路由的1.12.2移植。
 * 只负责密度函数组合，不涉及区块循环或方块写入。
 */
public final class NoiseRouter118 {

    private enum DebugView { ALL, CHEESE_ONLY, SPAGHETTI_ONLY, NOODLE_ONLY, NONE }
    private static final DebugView DEBUG_VIEW = DebugView.ALL;

    // 洞穴噪声
    private final NormalNoise118 pillar;
    private final NormalNoise118 pillarRareness;
    private final NormalNoise118 pillarThickness;

    private final NormalNoise118 spaghetti2D;
    private final NormalNoise118 spaghetti2DElevation;
    private final NormalNoise118 spaghetti2DModulator;
    private final NormalNoise118 spaghetti2DThickness;

    private final NormalNoise118 spaghetti3D1;
    private final NormalNoise118 spaghetti3D2;
    private final NormalNoise118 spaghetti3DRarity;
    private final NormalNoise118 spaghetti3DThickness;
    private final NormalNoise118 spaghettiRoughness;
    private final NormalNoise118 spaghettiRoughnessModulator;
    private final NormalNoise118 caveEntrance;

    private final NormalNoise118 caveLayer;
    private final NormalNoise118 caveCheese;

    private final NormalNoise118 noodleToggle;
    private final NormalNoise118 noodleThickness;
    private final NormalNoise118 noodleRidgeA;
    private final NormalNoise118 noodleRidgeB;

    // 地表/气候噪声
    private final NormalNoise118 vegetation;
    private final NormalNoise118 continentalness;
    private final NormalNoise118 erosion;
    private final NormalNoise118 ridge;
    private final NormalNoise118 shift;
    private final NormalNoise118 jagged;

    // 含水层噪声
    private final NormalNoise118 aquiferBarrier;
    private final NormalNoise118 aquiferFloodedness;
    private final NormalNoise118 aquiferSpread;
    private final NormalNoise118 aquiferLava;

    private final TerrainShaper118 terrainShaper;
    private final long worldSeed;

    public static final class ClimateSample {
        public final double factor;
        public final double offset;
        public final double jaggedness;

        ClimateSample(double factor, double offset, double jaggedness) {
            this.factor = factor;
            this.offset = offset;
            this.jaggedness = jaggedness;
        }
    }

    public NoiseRouter118(long worldSeed) {
        this.worldSeed = worldSeed;

        pillar = createNoise(worldSeed, "minecraft:pillar", -7, 1.0D, 1.0D);
        pillarRareness = createNoise(worldSeed, "minecraft:pillar_rareness", -8, 1.0D);
        pillarThickness = createNoise(worldSeed, "minecraft:pillar_thickness", -8, 1.0D);

        spaghetti2D = createNoise(worldSeed, "minecraft:spaghetti_2d", -7, 1.0D);
        spaghetti2DElevation = createNoise(worldSeed, "minecraft:spaghetti_2d_elevation", -8, 1.0D);
        spaghetti2DModulator = createNoise(worldSeed, "minecraft:spaghetti_2d_modulator", -11, 1.0D);
        spaghetti2DThickness = createNoise(worldSeed, "minecraft:spaghetti_2d_thickness", -11, 1.0D);

        spaghetti3D1 = createNoise(worldSeed, "minecraft:spaghetti_3d_1", -7, 1.0D);
        spaghetti3D2 = createNoise(worldSeed, "minecraft:spaghetti_3d_2", -7, 1.0D);
        spaghetti3DRarity = createNoise(worldSeed, "minecraft:spaghetti_3d_rarity", -11, 1.0D);
        spaghetti3DThickness = createNoise(worldSeed, "minecraft:spaghetti_3d_thickness", -8, 1.0D);
        spaghettiRoughness = createNoise(worldSeed, "minecraft:spaghetti_roughness", -5, 1.0D);
        spaghettiRoughnessModulator = createNoise(worldSeed, "minecraft:spaghetti_roughness_modulator", -8, 1.0D);
        caveEntrance = createNoise(worldSeed, "minecraft:cave_entrance", -7, 0.4D, 0.5D, 1.0D);

        caveLayer = createNoise(worldSeed, "minecraft:cave_layer", -8, 1.0D);
        caveCheese = createNoise(worldSeed, "minecraft:cave_cheese", -8,
                0.5D, 1.0D, 2.0D, 1.0D, 2.0D, 1.0D, 0.0D, 2.0D, 0.0D);

        noodleToggle = createNoise(worldSeed, "minecraft:noodle", -8, 1.0D);
        noodleThickness = createNoise(worldSeed, "minecraft:noodle_thickness", -8, 1.0D);
        noodleRidgeA = createNoise(worldSeed, "minecraft:noodle_ridge_a", -7, 1.0D);
        noodleRidgeB = createNoise(worldSeed, "minecraft:noodle_ridge_b", -7, 1.0D);

        vegetation = createNoise(worldSeed, "minecraft:vegetation", -8,
                1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        continentalness = createNoise(worldSeed, "minecraft:continentalness", -9,
                1.0D, 1.0D, 2.0D, 2.0D, 2.0D, 1.0D, 1.0D, 1.0D, 1.0D);
        erosion = createNoise(worldSeed, "minecraft:erosion", -9,
                1.0D, 1.0D, 0.0D, 1.0D, 1.0D);
        ridge = createNoise(worldSeed, "minecraft:ridge", -7,
                1.0D, 2.0D, 1.0D, 0.0D, 0.0D, 0.0D);
        shift = createNoise(worldSeed, "minecraft:offset", -3,
                1.0D, 1.0D, 1.0D, 0.0D);
        jagged = createNoise(worldSeed, "minecraft:jagged", -16,
                1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D,
                1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);

        
        terrainShaper = new TerrainShaper118();

        aquiferBarrier = createNoise(worldSeed, "minecraft:aquifer_barrier", -3, 1.0D);
        aquiferFloodedness = createNoise(worldSeed, "minecraft:aquifer_fluid_level_floodedness", -7, 1.0D);
        aquiferSpread = createNoise(worldSeed, "minecraft:aquifer_fluid_level_spread", -5, 1.0D);
        aquiferLava = createNoise(worldSeed, "minecraft:aquifer_lava", -1, 1.0D);
    }

    private static NormalNoise118 createNoise(long worldSeed, String resourceName,
                                              int firstOctave, double... amplitudes) {
        return new NormalNoise118(new Random(noiseSeed(worldSeed, resourceName)), firstOctave, amplitudes);
    }

    private static long noiseSeed(long worldSeed, String resourceName) {
        long value = worldSeed ^ ((long) resourceName.hashCode() * 341873128712L);
        value ^= value >>> 27;
        value *= 0x3C79AC492BA7B653L;
        value ^= value >>> 33;
        value *= 0x1C69B3F74AC4AE35L;
        return value ^ value >>> 27;
    }

        public long getWorldSeed() {
        return worldSeed;
    }

    public ClimateSample sampleClimate(int x, int z) {

        TerrainShaper118.Point point = climatePoint(x, z);
        return new ClimateSample(
                clamp(terrainShaper.factor(point), 0.0D, 8.0D),
                clamp(terrainShaper.offset(point), -0.81D, 2.5D),
                clamp(terrainShaper.jaggedness(point), 0.0D, 1.28D)
        );
    }

    /**
     * 使用当前 ChunkGeneratorOverworld 的真实heightMap密度作为地形项；洞穴分支仍保持
     * 1.18的Cheese、Spaghetti和Noodle组合。这里不再采样1.18的BASE_3D_NOISE地形。
     */
    public double getCaveDensity(int x, int y, int z, double vanillaTerrainDensity) {
        return fullCaveDensity(x, y, z, vanillaTerrainDensity);
    }

    /**
     * 官方 {@code NoiseRouterData#entrances()} 的直接采样值。它由窄的3D Spaghetti
     * 与 {@code CAVE_ENTRANCE} 噪声取最小值构成；不包含Cheese，因此可作为真实地表
     * 附近的入口资格判断，避免大型Cheese空腔被直接切开成矩形天坑。
     */
        public double getEntrancesDensity(int x, int y, int z) {
        return entrancesDensity(x, y, z);
    }

    public double getCaveEntranceDensity(int x, int y, int z) {
        return caveEntranceDensity(x, y, z);
    }

    public double getEntranceSpaghettiDensity(int x, int y, int z) {
        return entranceSpaghettiDensity(x, y, z);
    }


    public double sampleUndergroundBiomeDepth(int blockX, int primerY, int blockZ, ClimateSample climate) {
        int routerY = toRouterY(primerY);
        double offset = climate.offset;
        return yClampedGradient(routerY, -64, 320, 1.5D, -1.5D) + offset;
    }

    private double fullCaveDensity(int x, int y, int z, double terrainDensity) {
        if (!allowsUndergroundBranches()) {
            return 64.0D;
        }
        double entrances = allowsSpaghetti() ? entrancesDensity(x, y, z) : 64.0D;
        if (terrainDensity < 1.5625D) {
            return Math.min(terrainDensity, 5.0D * entrances);
        }
        return undergroundDensity(x, y, z, terrainDensity, entrances);
    }

    public double sampleUndergroundHumidity(int blockX, int blockZ) {
        return sampleClimateNoise(vegetation, blockX, blockZ);
    }

    public double sampleUndergroundContinentalness(int blockX, int blockZ) {
        return sampleClimateNoise(continentalness, blockX, blockZ);
    }

    

    private double undergroundDensity(int x, int y, int z, double slopedCheese, double entrances) {
        double cheese = allowsCheese() ? cheeseDensity(x, y, z, slopedCheese) : 64.0D;
        double spaghetti2DWithRoughness = allowsSpaghetti()
                ? spaghetti2DDensity(x, y, z) + spaghettiRoughness(x, y, z)
                : 64.0D;
        double result = Math.min(Math.min(cheese, entrances), spaghetti2DWithRoughness);

        if (DEBUG_VIEW == DebugView.ALL) {
            double pillar = pillarDensity(x, y, z);
            /* 官方Pillar本身由罕见噪声控制。当前1.12高度域更短，若直接在所有负密度空腔
             * 注入会把细小通道也填成柱林；只在强Cheese空腔内保留明显的柱核心。 */
            /* 1.12物理Y=0..23对应官方深板岩/熔岩含水层；在该短高度域直接回填柱子
             * 会把熔岩大空腔变成柱林。仅在其上方的强Cheese核心保留极少数柱心。 */
            /* 仅从物理Y=44（routerY=-20）以上的中层Cheese开始保留柱心，
             * 避开深层熔岩空腔；阈值介于此前的过密0.12与全零0.24之间。 */
            if (y >= -20 && result < -0.35D && pillar >= 0.16D) {
                result = Math.max(result, pillar);
            }
        }
        return result;
    }

    private double cheeseDensity(int x, int y, int z, double slopedCheese) {
        double layer = sample(caveLayer, x, y, z, 1.0D, 8.0D);
        double layerTerm = 4.0D * layer * layer;
        double cheeseNoise = sample(caveCheese, x, y, z, 1.0D, 2.0D / 3.0D);
        double depthBias = clamp(1.5D - 0.64D * slopedCheese, 0.0D, 0.5D);
        return layerTerm + clamp(0.27D + cheeseNoise, -1.0D, 1.0D) + depthBias;
    }

    private double spaghettiRoughness(int x, int y, int z) {
        double roughness = sample(spaghettiRoughness, x, y, z, 1.0D, 1.0D);
        double modulator = mapFromUnit(sample(spaghettiRoughnessModulator, x, y, z, 1.0D, 1.0D), 0.0D, -0.1D);
        return modulator * (Math.abs(roughness) - 0.4D);
    }

    private double entrancesDensity(int x, int y, int z) {
        return Math.min(caveEntranceDensity(x, y, z), entranceSpaghettiDensity(x, y, z));
    }

    private double caveEntranceDensity(int x, int y, int z) {
        double entranceNoise = sample(caveEntrance, x, y, z, 0.75D, 0.5D);
        double verticalGate = yClampedGradient(y, -10, 30, 0.3D, 0.0D);
        return entranceNoise + 0.37D + verticalGate;
    }

    private double entranceSpaghettiDensity(int x, int y, int z) {
        double rarity = sample(spaghetti3DRarity, x, y, z, 2.0D, 1.0D);
        double thickness = mapFromUnit(sample(spaghetti3DThickness, x, y, z, 1.0D, 1.0D), -0.065D, -0.088D);
        double a = weirdScaledSample(spaghetti3D1, x, y, z, rarity, false);
        double b = weirdScaledSample(spaghetti3D2, x, y, z, rarity, false);
        double spaghetti3D = clamp(Math.max(a, b) + thickness, -1.0D, 1.0D);
        return spaghettiRoughness(x, y, z) + spaghetti3D;
    }

    private double spaghetti2DDensity(int x, int y, int z) {
        double modulator = sample(spaghetti2DModulator, x, y, z, 2.0D, 1.0D);
        double scaled = weirdScaledSample(spaghetti2D, x, y, z, modulator, true);

        double elevation = mapFromUnit(sample(spaghetti2DElevation, x, y, z, 1.0D, 0.0D), -8.0D, 8.0D);
        double thickness = mapFromUnit(sample(spaghetti2DThickness, x, y, z, 1.0D, 1.0D), -0.6D, -1.3D);

        double verticalDistance = Math.abs(elevation + yClampedGradient(y, -64, 320, 8.0D, -40.0D));
        double elevationLimit = cube(verticalDistance + thickness);
        double primary = scaled + 0.083D * thickness;
        return clamp(Math.max(primary, elevationLimit), -1.0D, 1.0D);
    }

    private static double weirdScaledSample(NormalNoise118 noise,
                                            int x, int y, int z,
                                            double rarity,
                                            boolean type2) {
        double scale = type2 ? spaghettiRarity2D(rarity) : spaghettiRarity3D(rarity);
        return scale * Math.abs(noise.getValue((double) x / scale, (double) y / scale, (double) z / scale));
    }

    private static double spaghettiRarity2D(double value) {
        if (value < -0.75D) return 0.5D;
        if (value < -0.5D) return 0.75D;
        if (value < 0.5D) return 1.0D;
        return value < 0.75D ? 2.0D : 3.0D;
    }

    private static double spaghettiRarity3D(double value) {
        if (value < -0.5D) return 0.75D;
        if (value < 0.0D) return 1.0D;
        return value < 0.5D ? 1.5D : 2.0D;
    }

    private double limitedNoodleToggle(int x, int y, int z) {
        return y >= -60 && y <= 320 ? sample(noodleToggle, x, y, z, 1.0D, 1.0D) : -1.0D;
    }

    private double limitedNoodleThickness(int x, int y, int z) {
        return y >= -60 && y <= 320
                ? mapFromUnit(sample(noodleThickness, x, y, z, 1.0D, 1.0D), -0.05D, -0.1D)
                : 0.0D;
    }

    private double limitedNoodleRidgeA(int x, int y, int z) {
        return y >= -60 && y <= 320
                ? sample(noodleRidgeA, x, y, z, 8.0D / 3.0D, 8.0D / 3.0D)
                : 0.0D;
    }

    private double limitedNoodleRidgeB(int x, int y, int z) {
        return y >= -60 && y <= 320
                ? sample(noodleRidgeB, x, y, z, 8.0D / 3.0D, 8.0D / 3.0D)
                : 0.0D;
    }

    private static double noodleDensityFromInterpolated(double toggle,
                                                        double thickness,
                                                        double ridgeA,
                                                        double ridgeB) {
        return toggle + NoiseGeneratorSettings118.NOODLE_TOGGLE_BIAS < 0.0D ? 64.0D
                : thickness + NoiseGeneratorSettings118.NOODLE_THICKNESS_BIAS
                + NoiseGeneratorSettings118.NOODLE_RIDGE_MULTIPLIER * Math.max(Math.abs(ridgeA), Math.abs(ridgeB));
    }

    private double pillarDensity(int x, int y, int z) {
        double pillarField = sample(pillar, x, y, z, 25.0D, 0.3D);
        double rareness = mapFromUnit(sample(pillarRareness, x, y, z, 1.0D, 1.0D), 0.0D, -2.0D);
        double thickness = mapFromUnit(sample(pillarThickness, x, y, z, 1.0D, 1.0D), 0.0D, 1.1D);
        return (pillarField * 2.0D + rareness) * cube(thickness);
    }

    private TerrainShaper118.Point climatePoint(int x, int z) {
        return TerrainShaper118.point(
                sampleClimateNoise(continentalness, x, z),
                sampleClimateNoise(erosion, x, z),
                sampleClimateNoise(ridge, x, z));
    }

    private double sampleClimateNoise(NormalNoise118 noise, int x, int z) {
        double shiftX = shift.getValue(x * 0.25D, 0.0D, z * 0.25D) * 4.0D;
        double shiftZ = shift.getValue(z * 0.25D, x * 0.25D, 0.0D) * 4.0D;
        return noise.getValue(x * 0.25D + shiftX, 0.0D, z * 0.25D + shiftZ);
    }

    private static double sample(NormalNoise118 noise, int x, int y, int z, double xzScale, double yScale) {
        return noise.getValue(x * xzScale, y * yScale, z * xzScale);
    }

    private static double mapFromUnit(double noise, double min, double max) {
        return (min + max) * 0.5D + (max - min) * 0.5D * noise;
    }

    private static double yClampedGradient(int y, int fromY, int toY, double fromValue, double toValue) {
        if (y <= fromY) return fromValue;
        if (y >= toY) return toValue;
        return lerp((double) (y - fromY) / (double) (toY - fromY), fromValue, toValue);
    }

    private static double squeeze(double value) {
        double c = clamp(value, -1.0D, 1.0D);
        return c * 0.5D - c * c * c / 24.0D;
    }

    private static double cube(double value) {
        return value * value * value;
    }

    private static double halfNegative(double value) {
        return value > 0.0D ? value : value * 0.5D;
    }

    private static double quarterNegative(double value) {
        return value > 0.0D ? value : value * 0.25D;
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static boolean allowsCheese() {
        return DEBUG_VIEW == DebugView.ALL || DEBUG_VIEW == DebugView.CHEESE_ONLY;
    }

    private static boolean allowsSpaghetti() {
        return DEBUG_VIEW == DebugView.ALL || DEBUG_VIEW == DebugView.SPAGHETTI_ONLY;
    }

    private static boolean allowsNoodle() {
        return DEBUG_VIEW == DebugView.ALL || DEBUG_VIEW == DebugView.NOODLE_ONLY;
    }

    private static boolean allowsUndergroundBranches() {
        return allowsCheese() || allowsSpaghetti();
    }

    private static int toRouterY(int primerY) {
        return NoiseGeneratorSettings118.ROUTER_MIN_Y + primerY;
    }

    private static double applySlideRouter(double density, int routerY) {
        int minCellY = Math.floorDiv(NoiseGeneratorSettings118.ROUTER_MIN_Y, NoiseGeneratorSettings118.CELL_HEIGHT);
        int routerCellY = Math.floorDiv(routerY, NoiseGeneratorSettings118.CELL_HEIGHT);
        int cellIndex = routerCellY - minCellY;
        int cellCountY = NoiseGeneratorSettings118.ROUTER_HEIGHT / NoiseGeneratorSettings118.CELL_HEIGHT;
        double topCoordinate = (double) (cellCountY - cellIndex);
        density = applySlide(density, topCoordinate, NoiseGeneratorSettings118.TOP_SLIDE_TARGET,
                NoiseGeneratorSettings118.TOP_SLIDE_SIZE, NoiseGeneratorSettings118.TOP_SLIDE_OFFSET);
        return applySlide(density, (double) cellIndex, NoiseGeneratorSettings118.BOTTOM_SLIDE_TARGET,
                NoiseGeneratorSettings118.BOTTOM_SLIDE_SIZE, NoiseGeneratorSettings118.BOTTOM_SLIDE_OFFSET);
    }

    private static double applySlide(double value, double coordinate, double target, int size, int offset) {
        if (size <= 0) return value;
        double t = (coordinate - (double) offset) / (double) size;
        return clampedLerp(target, value, t);
    }

    private static double clampedLerp(double start, double end, double t) {
        return lerp(clamp(t, 0.0D, 1.0D), start, end);
    }

    public double getNoodleToggle(int x, int y, int z) {
        return limitedNoodleToggle(x, y, z);
    }

    public double getNoodleThickness(int x, int y, int z) {
        return limitedNoodleThickness(x, y, z);
    }

    public double getNoodleRidgeA(int x, int y, int z) {
        return limitedNoodleRidgeA(x, y, z);
    }

    public double getNoodleRidgeB(int x, int y, int z) {
        return limitedNoodleRidgeB(x, y, z);
    }

    public double getNoodleDensityFromInterpolated(double toggle, double thickness, double ridgeA, double ridgeB) {
        return noodleDensityFromInterpolated(toggle, thickness, ridgeA, ridgeB);
    }

    public double getSqueeze(double value) {
        return squeeze(value);
    }

    public double getApplySlideRouter(double density, int routerY) {
        return applySlideRouter(density, routerY);
    }

    public NormalNoise118 getAquiferBarrierNoise() {
        return aquiferBarrier;
    }

    public NormalNoise118 getAquiferFloodednessNoise() {
        return aquiferFloodedness;
    }

    public NormalNoise118 getAquiferSpreadNoise() {
        return aquiferSpread;
    }

    public NormalNoise118 getAquiferLavaNoise() {
        return aquiferLava;
    }
}