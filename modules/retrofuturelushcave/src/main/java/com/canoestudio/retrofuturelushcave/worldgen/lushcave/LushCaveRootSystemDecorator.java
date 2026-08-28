package com.canoestudio.retrofuturelushcave.worldgen.lushcave;

import com.canoestudio.retrofuturelushcave.config.Configuration;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.worldgen.WorldgenDiagnostics118;
import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * 1.12适配的1.18.2 RootSystemFeature。
 *
 * <p>官方PlacedFeature每个Chunk只给ROOTED_AZALEA_TREE一到两次候选；候选先寻找洞顶，
 * 再从该处向上寻找可生成杜鹃树的地表。只有杜鹃树实际生成成功时，才写ROOTED_DIRT主根列
 * 与洞顶HANGING_ROOTS。因此海洋、裸岩、树冠等无法生成杜鹃树的地表下方不应出现这条根柱型
 * 垂根链。</p>
 */
public final class LushCaveRootSystemDecorator {
    private static final long ROOT_SYSTEM_SALT = 0x524F4F54415A414CL;
    private static final int ROOT_COLUMN_MAX_HEIGHT = 100;
    private static final int ROOT_RADIUS = 3;
    private static final int ROOT_ATTEMPTS_PER_Y = 3;
    private static final int HANGING_ROOT_RADIUS = 2;
    private static final int HANGING_ROOT_SPAN = 2;
    private static final int HANGING_ROOT_ATTEMPTS = 20;
    /** CavePlacements.ROOTED_AZALEA_TREE EnvironmentScanPlacement的官方扫描上限。 */
    private static final int CEILING_SCAN_DISTANCE = 12;

    /** 默认关闭：仅在指定区块输出ROOTED_AZALEA_TREE候选和垂根写入统计。 */
    public static final boolean DEBUG_ROOT_SYSTEM_DIAGNOSTIC = false;
    /** 默认关闭：只在指定调试区块跳过LUSH区域筛选，并在命中洞顶后直接放置一个垂根。 */
    public static final boolean DEBUG_FORCE_HANGING_ROOTS = false;
    public static final int DEBUG_ROOT_SYSTEM_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_ROOT_SYSTEM_CHUNK_Z = Integer.MIN_VALUE;

    private static long cachedSeed = Long.MIN_VALUE;
    private static DensityCave118Generator generator;
    private static UndergroundRegionSelector regions;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPopulatePost(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0) return;
        ensureGenerator(world.getSeed());

        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        Random random = new Random(mixSeed(world.getSeed(), chunkX, chunkZ, ROOT_SYSTEM_SALT));
        boolean debug = isDebugRootSystemChunk(chunkX, chunkZ);
        RootSystemDiagnostic diagnostic = new RootSystemDiagnostic();

        /* CavePlacements.ROOTED_AZALEA_TREE: CountPlacement UniformInt(1,2)。 */
        int attempts = Configuration.LUSH_CAVES.minimumTreeAttempts
                + random.nextInt(Configuration.LUSH_CAVES.additionalTreeAttempts + 1);
        for (int i = 0; i < attempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            /* RangeBottomToMaxTerrainHeight + EnvironmentScanPlacement.UP(12)。 */
            int originY = scanUpForCeilingAir(chunk, x, randomY(random), z, CEILING_SCAN_DISTANCE);
            diagnostic.attempts++;
            if (originY < 0) continue;
            diagnostic.ceilingHits++;

            boolean lush = regions.isLushAt(x, originY, z) && isEnclosedUndergroundRootCavity(world, chunk, x, originY, z);
            if (lush) diagnostic.lushHits++;
            if (!lush && !(debug && DEBUG_FORCE_HANGING_ROOTS)) continue;

            if (debug && DEBUG_FORCE_HANGING_ROOTS) {
                if (placeDebugHangingRoot(chunk, x, originY, z)) diagnostic.hangingRoots++;
                continue;
            }

            /* 与官方RootSystemFeature一致：树失败时不生成主根列及树下洞顶垂根。 */
            RootSystemResult result = placeRootSystem(world, chunk, baseX, baseZ, x, originY, z, random);
            if (result.treeSuccess) diagnostic.treeSuccesses++;
            diagnostic.rootColumnBlocks += result.rootColumnBlocks;
            diagnostic.hangingRoots += result.hangingRoots;
        }

        if (debug && DEBUG_FORCE_HANGING_ROOTS) {
            sweepDebugHangingRoots(chunk, baseX, baseZ, diagnostic);
        }
        WorldgenDiagnostics118.recordRootSystem(world.getSeed(), diagnostic.attempts, diagnostic.ceilingHits,
                diagnostic.lushHits, diagnostic.treeSuccesses, diagnostic.rootColumnBlocks, diagnostic.hangingRoots);
        if (debug) {
            System.out.println("[RetroFutureLushCave][ROOT_SYSTEM_DIAG] seed=" + world.getSeed()
                    + " chunk=" + chunkX + "," + chunkZ
                    + " forceHangingRoots=" + DEBUG_FORCE_HANGING_ROOTS
                    + " attempts=" + diagnostic.attempts
                    + " ceilingHits=" + diagnostic.ceilingHits
                    + " lushHits=" + diagnostic.lushHits
                    + " treeSuccesses=" + diagnostic.treeSuccesses
                    + " rootColumnBlocks=" + diagnostic.rootColumnBlocks
                    + " hangingRoots=" + diagnostic.hangingRoots
                    + " debugSweepCeilings=" + diagnostic.debugSweepCeilings);
        }
    }

    private static boolean isDebugRootSystemChunk(int chunkX, int chunkZ) {
        return DEBUG_ROOT_SYSTEM_DIAGNOSTIC
                && DEBUG_ROOT_SYSTEM_CHUNK_X != Integer.MIN_VALUE
                && DEBUG_ROOT_SYSTEM_CHUNK_Z != Integer.MIN_VALUE
                && chunkX == DEBUG_ROOT_SYSTEM_CHUNK_X
                && chunkZ == DEBUG_ROOT_SYSTEM_CHUNK_Z;
    }

    /** 强制调试只验证洞顶支撑和Chunk直写，不生成树或根柱。 */
    private static boolean placeDebugHangingRoot(Chunk chunk, int x, int ceilingAirY, int z) {
        if (ceilingAirY < 0 || ceilingAirY >= 255
                || !isAir(chunk, x, ceilingAirY, z)
                || !isSolid(chunk, x, ceilingAirY + 1, z)) return false;
        chunk.setBlockState(new BlockPos(x, ceilingAirY, z), ModBlocks.HANGING_ROOTS.getDefaultState());
        return true;
    }

    /** 仅调试：4格网格扫描当前区块的真实空气-固体洞顶，不依赖1-2次随机RootSystem候选。 */
    private static void sweepDebugHangingRoots(Chunk chunk, int baseX, int baseZ, RootSystemDiagnostic diagnostic) {
        for (int localX = 1; localX < 16; localX += 4) {
            for (int localZ = 1; localZ < 16; localZ += 4) {
                int x = baseX + localX;
                int z = baseZ + localZ;
                for (int y = 1; y < 255; y++) {
                    if (isAir(chunk, x, y, z) && isSolid(chunk, x, y + 1, z)) {
                        diagnostic.debugSweepCeilings++;
                        if (placeDebugHangingRoot(chunk, x, y, z)) diagnostic.hangingRoots++;
                        break;
                    }
                }
            }
        }
    }

    private static final class RootSystemDiagnostic {
        int attempts;
        int ceilingHits;
        int lushHits;
        int treeSuccesses;
        int rootColumnBlocks;
        int hangingRoots;
        int debugSweepCeilings;
    }

    private static final class RootSystemResult {
        static final RootSystemResult FAILED = new RootSystemResult(false, 0, 0);
        final boolean treeSuccess;
        final int rootColumnBlocks;
        final int hangingRoots;

        RootSystemResult(boolean treeSuccess, int rootColumnBlocks, int hangingRoots) {
            this.treeSuccess = treeSuccess;
            this.rootColumnBlocks = rootColumnBlocks;
            this.hangingRoots = hangingRoots;
        }
    }

    private static void ensureGenerator(long worldSeed) {
        if (generator == null || cachedSeed != worldSeed) {
            cachedSeed = worldSeed;
            generator = new DensityCave118Generator(worldSeed);
            regions = new UndergroundRegionSelector(generator);
        }
    }

    private static RootSystemResult placeRootSystem(World world, Chunk chunk, int baseX, int baseZ,
                                                    int x, int originY, int z, Random random) {
        int surfaceY = findSurfaceAir(world, chunk, x, z);
        if (surfaceY < 2 || surfaceY - originY > Configuration.LUSH_CAVES.maximumRootColumnHeight
                || surfaceY <= originY) {
            return RootSystemResult.FAILED;
        }
        if (!isAir(chunk, x, originY, z) || !hasInitialTreeSpace(world, chunk, x, surfaceY, z)) {
            return RootSystemResult.FAILED;
        }

        BlockPos treeBase = new BlockPos(x, surfaceY, z);
        BlockPos oldSoil = treeBase.down();
        IBlockState oldSoilState = world.getBlockState(oldSoil);
        world.setBlockState(oldSoil, Blocks.DIRT.getDefaultState(), 2);
        if (!new WorldGenBigAzaleaTree(true).generate(world, random, treeBase)) {
            world.setBlockState(oldSoil, oldSoilState, 2);
            return RootSystemResult.FAILED;
        }

        int rootBlocks = 0;
        for (int y = originY + 1; y < surfaceY; y++) {
            if (isRootReplaceable(chunk, x, y, z)) {
                chunk.setBlockState(new BlockPos(x, y, z), ModBlocks.ROOTED_DIRT.getDefaultState());
                rootBlocks++;
            }
        }
        for (int y = originY; y < surfaceY; y++) {
            for (int attempt = 0; attempt < ROOT_ATTEMPTS_PER_Y; attempt++) {
                int rootX = x + random.nextInt(ROOT_RADIUS) - random.nextInt(ROOT_RADIUS);
                int rootZ = z + random.nextInt(ROOT_RADIUS) - random.nextInt(ROOT_RADIUS);
                if (inside(baseX, baseZ, rootX, rootZ) && isRootReplaceable(chunk, rootX, y, rootZ)) {
                    chunk.setBlockState(new BlockPos(rootX, y, rootZ), ModBlocks.ROOTED_DIRT.getDefaultState());
                    rootBlocks++;
                }
            }
        }

        int hangingRoots = 0;
        if (isAir(chunk, x, originY, z) && isSolid(chunk, x, originY + 1, z)) {
            world.setBlockState(new BlockPos(x, originY, z), ModBlocks.HANGING_ROOTS.getDefaultState(), 2);
            hangingRoots++;
        }
        hangingRoots += placeHangingRootsAroundCeiling(world, chunk, baseX, baseZ, x, originY, z, random,
                Configuration.LUSH_CAVES.hangingRootAttempts);
        return new RootSystemResult(true, rootBlocks, hangingRoots);
    }

    /** 在树下、已确认的繁茂洞顶附近放置RootSystemFeature配置中的垂根。 */
    private static int placeHangingRootsAroundCeiling(World world, Chunk chunk, int baseX, int baseZ,
                                                      int x, int originY, int z, Random random,
                                                      int attempts) {
        int placed = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int rootX = x + random.nextInt(HANGING_ROOT_RADIUS) - random.nextInt(HANGING_ROOT_RADIUS);
            int rootY = originY + random.nextInt(HANGING_ROOT_SPAN) - random.nextInt(HANGING_ROOT_SPAN);
            int rootZ = z + random.nextInt(HANGING_ROOT_RADIUS) - random.nextInt(HANGING_ROOT_RADIUS);
            if (inside(baseX, baseZ, rootX, rootZ) && rootY > 0 && rootY < 255
                    && isAir(chunk, rootX, rootY, rootZ) && isSolid(chunk, rootX, rootY + 1, rootZ)) {
                world.setBlockState(new BlockPos(rootX, rootY, rootZ), ModBlocks.HANGING_ROOTS.getDefaultState(), 2);
                placed++;
            }
        }
        return placed;
    }

    /** 根系只可从真正的地下洞顶向上连接；露天凹地、树冠下和浅层裂缝不能成为繁茂根系起点。 */
    private static boolean isEnclosedUndergroundRootCavity(World world, Chunk chunk, int x, int originY, int z) {
        if (originY <= 0 || originY >= 255 || !isAir(chunk, x, originY, z)
                || !isSolid(chunk, x, originY + 1, z)) return false;
        for (int y = originY + 1; y <= originY + 8 && y < 256; y++) {
            Block block = state(chunk, x, y, z).getBlock();
            /* 1.12的花岗岩、闪长岩和安山岩均属于Blocks.STONE的变种状态。 */
            if (block != Blocks.STONE && block != ModBlocks.DeepSlate && block != ModBlocks.DRIPSTONE_BLOCK
                    && block != ModBlocks.CALCITE && block != ModBlocks.TUFF) return false;
        }
        int surfaceY = findSurfaceAir(world, chunk, x, z);
        return surfaceY > originY + 12
                && surfaceY - originY <= Configuration.LUSH_CAVES.maximumRootColumnHeight;
    }

    /** 融合版本一的土壤检测：只承认能支撑树苗的方块为有效地表。 */
    private static int findSurfaceAir(World world, Chunk chunk, int x, int z) {
        for (int y = 254; y >= 1; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isTreeSoil(world, pos) && isAir(chunk, x, y + 1, z)) return y + 1;
        }
        return -1;
    }

    /** 版本二原本仅要求3格空气和固体地面；此处将固体地面改为“能支撑树苗的土壤”。 */
    private static boolean hasInitialTreeSpace(World world, Chunk chunk, int x, int y, int z) {
        for (int dy = 0; dy < 3 && y + dy <= 255; dy++) {
            if (!isAir(chunk, x, y + dy, z)) return false;
        }
        return isTreeSoil(world, new BlockPos(x, y - 1, z));
    }

    /** 检查方块是否能支撑原版树苗（即草方块、泥土等）。 */
    private static boolean isTreeSoil(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP,
                (BlockSapling) Blocks.SAPLING);
    }

    private static int scanUpForCeilingAir(Chunk chunk, int x, int startY, int z, int maxSteps) {
        for (int step = 0, y = clampY(startY); step <= maxSteps && y < 255; step++, y++) {
            if (isSolid(chunk, x, y, z)) return isAir(chunk, x, y - 1, z) ? y - 1 : -1;
            if (!isAir(chunk, x, y, z)) return -1;
        }
        return -1;
    }

    private static boolean isRootReplaceable(Chunk chunk, int x, int y, int z) {
        Block block = state(chunk, x, y, z).getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == Blocks.DIRT
                || block == Blocks.GRASS || block == Blocks.GRAVEL;
    }

    private static boolean isSolid(Chunk chunk, int x, int y, int z) {
        return y >= 0 && y < 256 && state(chunk, x, y, z).getMaterial().isSolid();
    }

    private static boolean isAir(Chunk chunk, int x, int y, int z) {
        return y >= 0 && y < 256 && state(chunk, x, y, z).getBlock() == Blocks.AIR;
    }

    private static IBlockState state(Chunk chunk, int x, int y, int z) {
        return chunk.getBlockState(new BlockPos(x, y, z));
    }

    private static boolean inside(int baseX, int baseZ, int x, int z) {
        return x >= baseX && x < baseX + 16 && z >= baseZ && z < baseZ + 16;
    }

    private static int randomY(Random random) {
        return 4 + random.nextInt(248);
    }

    private static int clampY(int y) {
        return Math.max(1, Math.min(254, y));
    }

    private static long mixSeed(long seed, int chunkX, int chunkZ, long salt) {
        long value = seed ^ salt;
        value ^= (long) chunkX * 341873128712L;
        value ^= (long) chunkZ * 132897987541L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return value;
    }
}