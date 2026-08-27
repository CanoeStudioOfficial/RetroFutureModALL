package com.canoestudio.retrofuturelushcave.worldgen;

/**
 * 读取 ChunkGeneratorOverworld 已生成的 5x5x33 heightMap 密度格点。
 * 该类只桥接原版基础地形密度，不重新生成地形，也不改变洞穴噪声坐标。
 */
public final class VanillaTerrainDensity112 {
    private static final int GRID_SIZE = 5;
    private static final int HEIGHT_SAMPLES = 33;
    private static final int HEIGHT_MAP_SIZE = GRID_SIZE * GRID_SIZE * HEIGHT_SAMPLES;

    private final double[] heightMap;
    private final int originX;
    private final int originZ;

    public VanillaTerrainDensity112(double[] heightMap, int chunkX, int chunkZ) {
        if (heightMap == null || heightMap.length != HEIGHT_MAP_SIZE) {
            throw new IllegalArgumentException("ChunkGeneratorOverworld heightMap must contain 5x5x33 samples");
        }
        this.heightMap = heightMap.clone();
        this.originX = chunkX << 4;
        this.originZ = chunkZ << 4;
    }

    /**
     * 返回当前上移 Primer 格点对应的原版地形密度。0..192 保持原版 0..128 的精确 +64
     * 映射；压缩后的上层和新增深层均沿原版 heightMap 的端点梯度连续外推。
     */
    public double sampleAtGridPoint(int worldX, int primerY, int worldZ) {
        int gridX = clamp((worldX - originX) / NoiseGeneratorSettings118.CELL_WIDTH, 0, GRID_SIZE - 1);
        int gridZ = clamp((worldZ - originZ) / NoiseGeneratorSettings118.CELL_WIDTH, 0, GRID_SIZE - 1);
        return sampleVertical(gridX, gridZ, toVanillaHeight(primerY));
    }

    private double sampleVertical(int gridX, int gridZ, double vanillaY) {
        double sampleY = vanillaY / NoiseGeneratorSettings118.CELL_HEIGHT;
        int lower = (int) Math.floor(sampleY);
        double fraction = sampleY - lower;
        double low = sampleHeightMap(gridX, gridZ, lower);
        double high = sampleHeightMap(gridX, gridZ, lower + 1);
        return low + (high - low) * fraction;
    }

    private double sampleHeightMap(int gridX, int gridZ, int yIndex) {
        if (yIndex >= 0 && yIndex < HEIGHT_SAMPLES) {
            return heightMap[((gridX * GRID_SIZE + gridZ) * HEIGHT_SAMPLES) + yIndex];
        }
        if (yIndex < 0) {
            double first = heightMap[(gridX * GRID_SIZE + gridZ) * HEIGHT_SAMPLES];
            double second = heightMap[((gridX * GRID_SIZE + gridZ) * HEIGHT_SAMPLES) + 1];
            return first + (second - first) * yIndex;
        }
        int base = (gridX * GRID_SIZE + gridZ) * HEIGHT_SAMPLES;
        double previous = heightMap[base + HEIGHT_SAMPLES - 2];
        double last = heightMap[base + HEIGHT_SAMPLES - 1];
        return last + (last - previous) * (yIndex - (HEIGHT_SAMPLES - 1));
    }

    private static double toVanillaHeight(int primerY) {
        if (primerY <= 192) {
            return primerY - 64.0D;
        }
        return 129.0D + (primerY - 193.0D) * 126.0D / 62.0D;
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
