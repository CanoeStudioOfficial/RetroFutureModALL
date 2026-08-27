package com.canoestudio.retrofuturelushcave.worldgen.cave;

import java.util.Arrays;
import java.util.Random;

import com.canoestudio.retrofuturelushcave.config.Configuration;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.worldgen.WorldgenDiagnostics118;

import static com.canoestudio.retrofuturelushcave.RetroFutureLushCave.LOGGER;

import net.minecraft.block.Block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 对应 Minecraft 1.18.2 {@code CanyonWorldCarver} 的 1.12 ChunkPrimer 移植。
 *
 * <p>它不是旧版 {@code MapGenRavine}：路径推进、横向宽度因子、垂直半径和默认参数均按
 * 1.18 Canyon 的算法实现。由于当前世界采用 routerY = primerY - 64 的1:1映射，
  * 1.18 默认峡谷高度分布 -54..67 映射为 primer Y=10..131。</p>

 *
 * <p>该生成器只能在 ChunkPrimer 阶段运行。它不会调用 World#setBlockState，也不会触发
 * 流体邻居更新；当前版本不参与地下Aquifer或流体状态计算，雕刻结果恒为空气。
 </p>
 */
public final class MapGen118Canyon extends MapGenBase {
    /*
     * MapGenBase会对一个目标Primer枚举range=4的9x9源Chunk。直接使用0.01会得到
     * 每目标约0.81条起峡谷，过于密集；完全除以81又会使大范围样本长期为零。取0.0025，
     * 对应每目标约0.20条起峡谷，实际日志仍达0.21–0.63，地表断崖过密；降为0.00075，
     * 目标是每区块约0.06条起峡谷，仅保留稀有的干燥地表裂谷。
     */
    /* 0.00075在连续十批(共1280目标Chunk)中均为零，已低于“稀有但可遇见”的目标；
     * 0.001保持远低于先前0.0025的断崖密度，同时每128个Chunk约有8–11个源起点。 */
    private static final int RANGE = 4;

    /** 默认关闭：只记录指定目标Chunk的全部峡谷源候选与成功起点。 */
    public static final boolean DEBUG_CANYON_DIAGNOSTIC = false;
    public static final int DEBUG_CANYON_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_CANYON_CHUNK_Z = Integer.MIN_VALUE;
    /** 缩短相邻源Chunk路径重叠，避免多个罕见起点仍拼成连续超大地表断崖。 */


    

    private static final float MIN_VERTICAL_ROTATION = -0.125F;
    private static final float MAX_VERTICAL_ROTATION = 0.125F;
    private static final float MIN_DISTANCE_FACTOR = 0.75F;
    private static final float MAX_DISTANCE_FACTOR = 1.0F;
    private static final float MIN_HORIZONTAL_RADIUS_FACTOR = 0.75F;
    private static final float MAX_HORIZONTAL_RADIUS_FACTOR = 1.0F;
    private static final int WIDTH_SMOOTHNESS = 3;
    /** 真实地表边界：允许雕刻最顶层天然方块以形成峡谷开口，不越过地表上方空气。 */
    private static final int SURFACE_PROTECTION_DEPTH = 0;
    private static final int OFFICIAL_START_Y_MIN = 10;
    private static final int OFFICIAL_START_Y_MAX = 131;

    /** 仅用于替换原版CAVE；不干预第三方或其他维度的自定义Carver。 */
    private static final MapGenBase DISABLED_VANILLA_CAVES = new MapGenBase() {
        @Override
        protected void recursiveGenerate(World world, int sourceChunkX, int sourceChunkZ,
                                         int targetChunkX, int targetChunkZ, ChunkPrimer primer) {
            // Intentionally empty: modern noise caves are invoked by the ChunkGeneratorOverworld Mixin.
        }
    };

    /**
     * 统一的Forge Carver接管入口。注册本类即可：原版CAVE被禁用，原版RAVINE换为本峡谷。
     */
    @SubscribeEvent
    public static void replaceVanillaCarvers(InitMapGenEvent event) {
        MapGenBase original = event.getOriginalGen();
        if (original instanceof MapGenCaves) {
            event.setNewGen(DISABLED_VANILLA_CAVES);
        } else if (original instanceof MapGenRavine) {
            event.setNewGen(new MapGen118Canyon());
        }
    }

    private int cachedTargetChunkX = Integer.MIN_VALUE;
    private int cachedTargetChunkZ = Integer.MIN_VALUE;
    private int[] cachedSurfaceHeights;
    /** 当前目标Chunk中由原版实际水体构成的列；峡谷不得掏空这些列。 */
    private boolean[] cachedSurfaceFluidColumns;

    /**
     * 无参构造使本类可以直接作为 InitMapGenEvent.EventType.RAVINE 的替换生成器注册。
     * 实际world seed只能在 MapGenBase#recursiveGenerate 收到World后取得。
     */
    public MapGen118Canyon() {
        this.range = RANGE;
    }

    @Override
    protected void recursiveGenerate(World world, int sourceChunkX, int sourceChunkZ,
                                     int targetChunkX, int targetChunkZ, ChunkPrimer primer) {
        boolean diagnostic = DEBUG_CANYON_DIAGNOSTIC
                && targetChunkX == DEBUG_CANYON_CHUNK_X && targetChunkZ == DEBUG_CANYON_CHUNK_Z;
        float roll = this.rand.nextFloat();
        if (diagnostic) {
            LOGGER.info("[RetroFutureLushCave][CANYON_DIAG] target={},{} source={},{} roll={} probability={}",
                    targetChunkX, targetChunkZ, sourceChunkX, sourceChunkZ, roll, Configuration.CAVE_GENERATION.canyonProbability);
        }
        boolean accepted = roll <= (float) Configuration.CAVE_GENERATION.canyonProbability;
        WorldgenDiagnostics118.recordCanyonSource(world.getSeed(), accepted);
        if (!accepted) {
            return;
        }

        double startX = (double) ((sourceChunkX << 4) + this.rand.nextInt(16));
        double startZ = (double) ((sourceChunkZ << 4) + this.rand.nextInt(16));
                int[] surfaceHeights = getOrBuildSurfaceHeights(targetChunkX, targetChunkZ, primer);
        boolean[] surfaceFluidColumns = getOrBuildSurfaceFluidColumns(targetChunkX, targetChunkZ, primer);
                /* CanyonCarver 的官方起点高度为 router Y=-54..67；在本项目的 1:1 Primer
         * 映射中恰为 Y=10..131。起点不能由当前真实地表反推，否则每条峡谷都会被人为
         * 锚定到表层并形成长条裂缝。 */
        double startY = (double) sampleOfficialStartY(this.rand);

        float yaw = this.rand.nextFloat() * ((float) Math.PI * 2.0F);

        float verticalRotation = randomBetween(this.rand, MIN_VERTICAL_ROTATION, MAX_VERTICAL_ROTATION);
        float thickness = sampleDefaultThickness(this.rand);
        int length = (int) ((float) Configuration.CAVE_GENERATION.canyonPathLength
                * randomBetween(this.rand, MIN_DISTANCE_FACTOR, MAX_DISTANCE_FACTOR));
        long pathSeed = this.rand.nextLong();

        if (diagnostic) {
                        LOGGER.info("[RetroFutureLushCave][CANYON_DIAG] accepted target={},{} source={},{} start=({}, {}, {}) length={} thickness={}",
                    targetChunkX, targetChunkZ, sourceChunkX, sourceChunkZ, startX, startY, startZ,
                    length, thickness);

        }
        carvePath(world, targetChunkX, targetChunkZ, primer, surfaceHeights, surfaceFluidColumns, pathSeed,
                startX, startY, startZ, thickness, yaw, verticalRotation, length);

    }

    private void carvePath(World world, int targetChunkX, int targetChunkZ, ChunkPrimer primer,
                           int[] surfaceHeights, boolean[] surfaceFluidColumns, long pathSeed,

                           double x, double y, double z, float thickness, float yaw,
                           float verticalRotation, int length) {
        Random random = new Random(pathSeed);
        float[] widthFactors = initWidthFactors(random);
        float yawVelocity = 0.0F;
        float verticalVelocity = 0.0F;

        for (int step = 0; step < length; step++) {
            double horizontalRadius = 1.5D + (double) ((float) Math.sin((double) step * Math.PI / (double) length) * thickness);
            double verticalRadius = horizontalRadius * Configuration.CAVE_GENERATION.canyonVerticalScale;
            horizontalRadius *= (double) randomBetween(random, MIN_HORIZONTAL_RADIUS_FACTOR, MAX_HORIZONTAL_RADIUS_FACTOR);
            verticalRadius = updateVerticalRadius(random, verticalRadius, length, step);

            float verticalCos = (float) Math.cos(verticalRotation);
            float verticalSin = (float) Math.sin(verticalRotation);
            x += (double) ((float) Math.cos(yaw) * verticalCos);
            y += (double) verticalSin;
            z += (double) ((float) Math.sin(yaw) * verticalCos);

            verticalRotation *= 0.7F;
            verticalRotation += verticalVelocity * 0.05F;
            yaw += yawVelocity * 0.05F;
            verticalVelocity *= 0.8F;
            yawVelocity *= 0.5F;
            verticalVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (random.nextInt(4) != 0) {
                if (!canReach(targetChunkX, targetChunkZ, x, z, step, length, thickness)) {
                    return;
                }
                carveEllipsoid(world, targetChunkX, targetChunkZ, primer, surfaceHeights, surfaceFluidColumns,
                        x, y, z, horizontalRadius, verticalRadius, widthFactors);

            }
        }
    }

    private void carveEllipsoid(World world, int targetChunkX, int targetChunkZ, ChunkPrimer primer,
                                int[] surfaceHeights, boolean[] surfaceFluidColumns,
                                double centerX, double centerY, double centerZ,

                                double horizontalRadius, double verticalRadius,
                                float[] widthFactors) {
        int baseX = targetChunkX << 4;
        int baseZ = targetChunkZ << 4;
        int minLocalX = Math.max(floor(centerX - horizontalRadius) - baseX - 1, 0);
        int maxLocalX = Math.min(floor(centerX + horizontalRadius) - baseX, 15);
        int minLocalZ = Math.max(floor(centerZ - horizontalRadius) - baseZ - 1, 0);
        int maxLocalZ = Math.min(floor(centerZ + horizontalRadius) - baseZ, 15);
                int minY = Math.max(floor(centerY - verticalRadius) - 1, 1);
        int maxY = Math.min(floor(centerY + verticalRadius) + 1, 248);
        int surfaceBlocks = 0;
        boolean[] surfaceColumns = new boolean[256];

        for (int localX = minLocalX; localX <= maxLocalX; localX++) {

            int blockX = baseX + localX;
            double normalizedX = ((double) blockX + 0.5D - centerX) / horizontalRadius;
            for (int localZ = minLocalZ; localZ <= maxLocalZ; localZ++) {
                int blockZ = baseZ + localZ;
                double normalizedZ = ((double) blockZ + 0.5D - centerZ) / horizontalRadius;
                if (normalizedX * normalizedX + normalizedZ * normalizedZ >= 1.0D) {
                    continue;
                }

                                int surfaceIndex = (localZ << 4) | localX;
                /* 河流、湖泊、海岸水柱由原版基础地形保留；峡谷不能在其正下方开洞。 */
                if (surfaceFluidColumns[surfaceIndex]) continue;
                int protectedTopY = surfaceHeights[surfaceIndex] - SURFACE_PROTECTION_DEPTH;
                for (int blockY = maxY; blockY > minY; blockY--) {

                    /* surfaceHeights是顶层天然方块上方一格；只排除其上方空气，保留表层开口。 */
                    if (blockY >= protectedTopY) continue;
                    double normalizedY = ((double) blockY - 0.5D - centerY) / verticalRadius;

                    if (shouldSkip(widthFactors, normalizedX, normalizedY, normalizedZ, blockY)) {
                        continue;
                    }
                    IBlockState previous = primer.getBlockState(localX, blockY, localZ);
                    if (!canReplace(previous)) {
                        continue;
                    }

                    /* 当前版本移除了Aquifer与峡谷内嵌熔岩；仅在Primer写入空气。 */
                                        primer.setBlockState(localX, blockY, localZ, Blocks.AIR.getDefaultState());
                    if (blockY >= surfaceHeights[surfaceIndex] - 4) {
                        surfaceBlocks++;
                        surfaceColumns[surfaceIndex] = true;
                    }

                }
            }
                }
        if (surfaceBlocks > 0) {
            int surfaceColumnCount = 0;
            for (boolean carved : surfaceColumns) {
                if (carved) surfaceColumnCount++;
            }
            WorldgenDiagnostics118.recordCanyonSurfaceCarve(world.getSeed(), surfaceBlocks, surfaceColumnCount);
        }
    }

    private static float[] initWidthFactors(Random random) {

        float[] factors = new float[256];
        float factor = 1.0F;
        for (int y = 0; y < factors.length; y++) {
            if (y == 0 || random.nextInt(WIDTH_SMOOTHNESS) == 0) {
                factor = 1.0F + random.nextFloat() * random.nextFloat();
            }
            factors[y] = factor * factor;
        }
        return factors;
    }

    private static double updateVerticalRadius(Random random, double currentRadius, int length, int step) {
        float centerWeight = 1.0F - Math.abs(0.5F - (float) step / (float) length) * 2.0F;
        float factor = 1.0F + 0.0F * centerWeight;
        return (double) factor * currentRadius * (double) randomBetween(random, 0.75F, 1.0F);
    }

    private static boolean shouldSkip(float[] widthFactors, double normalizedX, double normalizedY,
                                      double normalizedZ, int blockY) {
        return (normalizedX * normalizedX + normalizedZ * normalizedZ) * (double) widthFactors[blockY - 1]
                + normalizedY * normalizedY / 6.0D >= 1.0D;
    }

    private static boolean canReach(int targetChunkX, int targetChunkZ, double pathX, double pathZ,
                                    int step, int length, float thickness) {
        double centerX = (double) (targetChunkX * 16 + 8);
        double centerZ = (double) (targetChunkZ * 16 + 8);
        double deltaX = pathX - centerX;
        double deltaZ = pathZ - centerZ;
        double remaining = (double) (length - step);
        double reach = (double) (thickness + 2.0F + 16.0F);
        return deltaX * deltaX + deltaZ * deltaZ - remaining * remaining <= reach * reach;
    }

    private int[] getOrBuildSurfaceHeights(int targetChunkX, int targetChunkZ, ChunkPrimer primer) {
        if (cachedSurfaceHeights != null && cachedTargetChunkX == targetChunkX && cachedTargetChunkZ == targetChunkZ) {
            return cachedSurfaceHeights;
        }
                int[] heights = new int[256];
        boolean[] fluidColumns = new boolean[256];
        Arrays.fill(heights, 0);

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                                int index = (localZ << 4) | localX;
                for (int y = 255; y >= 0; y--) {
                    if (primer.getBlockState(localX, y, localZ).getBlock() != Blocks.AIR) {
                        heights[index] = y + 1;
                        int fluidBottom = Math.max(0, y - 96);
                        for (int fluidY = y; fluidY >= fluidBottom; fluidY--) {
                            Block fluidBlock = primer.getBlockState(localX, fluidY, localZ).getBlock();
                            if (fluidBlock == Blocks.WATER || fluidBlock == Blocks.FLOWING_WATER
                                    || fluidBlock == Blocks.LAVA || fluidBlock == Blocks.FLOWING_LAVA) {
                                fluidColumns[index] = true;
                                break;
                            }
                        }
                        break;
                    }
                }

            }
        }
        cachedTargetChunkX = targetChunkX;
        cachedTargetChunkZ = targetChunkZ;
                cachedSurfaceHeights = heights;
        cachedSurfaceFluidColumns = fluidColumns;
        return heights;

    }

        private boolean[] getOrBuildSurfaceFluidColumns(int targetChunkX, int targetChunkZ, ChunkPrimer primer) {
        getOrBuildSurfaceHeights(targetChunkX, targetChunkZ, primer);
        return cachedSurfaceFluidColumns;
    }

        private static int sampleOfficialStartY(Random random) {
        return OFFICIAL_START_Y_MIN + random.nextInt(OFFICIAL_START_Y_MAX - OFFICIAL_START_Y_MIN + 1);
    }



    private static boolean canReplace(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.SANDSTONE
                || block == Blocks.RED_SANDSTONE || block == Blocks.HARDENED_CLAY
                || block == Blocks.STAINED_HARDENED_CLAY || block == Blocks.MYCELIUM;
    }



    private static float randomBetween(Random random, float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }

    /** 对应 TrapezoidFloat.of(0, 6, 2) 的采样。 */
    private static float sampleDefaultThickness(Random random) {
        return random.nextFloat() * 2.0F + random.nextFloat() * 4.0F;
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < (double) integer ? integer - 1 : integer;
    }
}
