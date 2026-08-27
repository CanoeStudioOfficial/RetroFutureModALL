package com.canoestudio.retrofuturelushcave.worldgen.cave;

import com.canoestudio.retrofuturelushcave.contents.blocks.AmethystClusterBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.utils.FluidloggedCompat;
import com.canoestudio.retrofuturelushcave.worldgen.noise.LegacyNormalNoise118;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 1.18.2 CaveFeatures.AMETHYST_GEODE + CavePlacements.AMETHYST_GEODE 的1.12安全移植。
 *
 * <p>所有写入都经Chunk#setBlockState完成；不会强制加载相邻区块，也不触发世界级流体递归。
 * 当33³特征范围所需的相邻区块尚未加载时，整次候选安全跳过，以避免生成半个紫水晶洞。</p>
 */
public final class Geode118Generator {
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 255;
    private static final int MIN_OFFSET = -16;
    private static final int MAX_OFFSET = 16;
    private static final int INVALID_THRESHOLD = 1;

    private static final double FILLING = 1.7D;
    private static final double INNER_LAYER = 2.2D;
    private static final double MIDDLE_LAYER = 3.2D;
    private static final double OUTER_LAYER = 4.2D;
    private static final double CRACK_CHANCE = 0.95D;
    private static final double BASE_CRACK_SIZE = 2.0D;
    private static final int CRACK_POINT_OFFSET = 2;
    private static final double POTENTIAL_PLACEMENTS_CHANCE = 0.35D;
    private static final double ALTERNATE_INNER_CHANCE = 0.083D;
    private static final double NOISE_MULTIPLIER = 0.05D;

    private static long noiseSeed = Long.MIN_VALUE;
    private static LegacyNormalNoise118 geodeNoise;

    private Geode118Generator() {
    }

    /**
     * 在1.12 decorate之前调用。随机顺序复刻：Rarity(1/24) → InSquare → Uniform Y[6,94]。
     * 原版高度为aboveBottom(6)..absolute(30)，经routerY=primerY-64映射后即6..94。
     */
    public static void populateBeforeBiomeDecorate(World world, int chunkX, int chunkZ) {
        if (world.isRemote || world.provider.getDimension() != 0) return;
        Chunk current = world.getChunk(chunkX, chunkZ);
        Random random = createDecorationRandom(world.getSeed(), chunkX, chunkZ);
        if (random.nextFloat() >= (1.0F / 24.0F)) return;

        int x = (chunkX << 4) + random.nextInt(16);
        int z = (chunkZ << 4) + random.nextInt(16);
        int y = 6 + random.nextInt(89); // inclusive 6..94
        placeGeode(world, current, chunkX, chunkZ, new BlockPos(x, y, z), random);
    }

    private static Random createDecorationRandom(long worldSeed, int chunkX, int chunkZ) {
        Random random = new Random(worldSeed);
        long xSeed = random.nextLong() / 2L * 2L + 1L;
        long zSeed = random.nextLong() / 2L * 2L + 1L;
        random.setSeed((long) chunkX * xSeed + (long) chunkZ * zSeed ^ worldSeed);
        return random;
    }

    private static void placeGeode(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                   BlockPos origin, Random random) {
        int minX = origin.getX() + MIN_OFFSET;
        int maxX = origin.getX() + MAX_OFFSET;
        int minZ = origin.getZ() + MIN_OFFSET;
        int maxZ = origin.getZ() + MAX_OFFSET;
        if (!areAllChunksLoaded(world, sourceChunk, sourceChunkX, sourceChunkZ, minX, maxX, minZ, maxZ)) return;

        ensureNoise(world.getSeed());
        int pointCount = 3 + random.nextInt(2); // UniformInt[3,4]
        List<GeodePoint> points = new ArrayList<GeodePoint>();
        int invalid = 0;
        double pointCountOverMaxDistance = pointCount / 6.0D;

        for (int i = 0; i < pointCount; i++) {
            BlockPos point = origin.add(4 + random.nextInt(3), 4 + random.nextInt(3), 4 + random.nextInt(3));
            Chunk pointChunk = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, point.getX(), point.getZ());
            IBlockState pointState = state(pointChunk, point);
            if (isAir(pointState) || isGeodeInvalid(pointState)) {
                if (++invalid > INVALID_THRESHOLD) return;
            }
            points.add(new GeodePoint(point, 1 + random.nextInt(2))); // UniformInt[1,2]
        }

        List<BlockPos> crackPoints = new ArrayList<BlockPos>();
        boolean generateCrack = random.nextFloat() < CRACK_CHANCE;
        if (generateCrack) {
            int direction = random.nextInt(4);
            int distance = pointCount * 2 + 1;
            if (direction == 0) {
                addCrackPoints(crackPoints, origin, distance, 0);
            } else if (direction == 1) {
                addCrackPoints(crackPoints, origin, 0, distance);
            } else if (direction == 2) {
                addCrackPoints(crackPoints, origin, distance, distance);
            } else {
                addCrackPoints(crackPoints, origin, 0, 0);
            }
        }

        double fillingThreshold = inverseSqrt(FILLING);
        double innerThreshold = inverseSqrt(INNER_LAYER + pointCountOverMaxDistance);
        double middleThreshold = inverseSqrt(MIDDLE_LAYER + pointCountOverMaxDistance);
        double outerThreshold = inverseSqrt(OUTER_LAYER + pointCountOverMaxDistance);
        double crackThreshold = inverseSqrt(BASE_CRACK_SIZE + random.nextDouble() / 2.0D
                + (pointCount > 3 ? pointCountOverMaxDistance : 0.0D));

        List<BlockPos> buddingPositions = new ArrayList<BlockPos>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(MIN_Y, origin.getY() + MIN_OFFSET); y <= Math.min(MAX_Y, origin.getY() + MAX_OFFSET); y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    double noise = geodeNoise.getValue(x, y, z) * NOISE_MULTIPLIER;
                    double density = 0.0D;
                    for (GeodePoint point : points) {
                        density += inverseSqrt(distanceSquared(pos, point.pos) + point.offset) + noise;
                    }
                    if (density < outerThreshold) continue;

                    double crackDensity = 0.0D;
                    for (BlockPos crackPoint : crackPoints) {
                        crackDensity += inverseSqrt(distanceSquared(pos, crackPoint) + CRACK_POINT_OFFSET) + noise;
                    }
                    Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
                    IBlockState existing = state(target, pos);
                    if (isCannotReplace(existing)) continue;

                    if (generateCrack && crackDensity >= crackThreshold && density < fillingThreshold) {
                        target.setBlockState(pos, Blocks.AIR.getDefaultState());
                    } else if (density >= fillingThreshold) {
                        target.setBlockState(pos, Blocks.AIR.getDefaultState());
                    } else if (density >= innerThreshold) {
                        boolean budding = random.nextFloat() < ALTERNATE_INNER_CHANCE;
                        target.setBlockState(pos, budding ? ModBlocks.BUDDING_AMETHYST.getDefaultState()
                                : ModBlocks.AMETHYST_BLOCK.getDefaultState());
                        /* placementsRequireLayer0Alternate=true：仅budding内层产生芽簇候选。 */
                        if (budding && random.nextFloat() < POTENTIAL_PLACEMENTS_CHANCE) {
                            buddingPositions.add(pos);
                        }
                    } else if (density >= middleThreshold) {
                        target.setBlockState(pos, ModBlocks.CALCITE.getDefaultState());
                    } else {
                        target.setBlockState(pos, ModBlocks.SMOOTH_BASALT.getDefaultState());
                    }
                }
            }
        }

        placeInnerClusters(world, sourceChunk, sourceChunkX, sourceChunkZ, buddingPositions, random);
    }

    private static void addCrackPoints(List<BlockPos> points, BlockPos origin, int xOffset, int zOffset) {
        points.add(origin.add(xOffset, 7, zOffset));
        points.add(origin.add(xOffset, 5, zOffset));
        points.add(origin.add(xOffset, 1, zOffset));
    }

    private static void placeInnerClusters(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                           List<BlockPos> buddingPositions, Random random) {
        for (BlockPos budding : buddingPositions) {
            Block cluster = randomCluster(random);
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos targetPos = budding.offset(facing);
                if (targetPos.getY() < MIN_Y || targetPos.getY() > MAX_Y) continue;
                Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ,
                        targetPos.getX(), targetPos.getZ());
                if (target == null) continue;
                IBlockState targetState = state(target, targetPos);
                if (!canClusterGrowAt(world, targetPos, targetState)) continue;
                IBlockState clusterState = cluster.getDefaultState()
                        .withProperty(AmethystClusterBlock.FACING, facing);
                setFluidloggableCluster(world, target, targetPos, clusterState, isWater(targetState));
                break;
            }
        }
    }

    private static Block randomCluster(Random random) {
        switch (random.nextInt(4)) {
            case 0:
                return ModBlocks.SMALL_AMETHYST_BUD;
            case 1:
                return ModBlocks.MEDIUM_AMETHYST_BUD;
            case 2:
                return ModBlocks.LARGE_AMETHYST_BUD;
            default:
                return ModBlocks.AMETHYST_CLUSTER;
        }
    }

    /** 与用户BuddingAmethystBlock#canClusterGrowAtState保持一致。 */
    private static boolean canClusterGrowAt(World world, BlockPos pos, IBlockState state) {
        if (!Loader.isModLoaded("fluidlogged_api")) {
            Block block = state.getBlock();
            return block.isReplaceable(world, pos) || isAir(state)
                    || block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        }
        return FluidloggedCompat.isWater(world, pos);
    }

    private static void setFluidloggableCluster(World world, Chunk chunk, BlockPos pos, IBlockState state, boolean water) {
        if (water && Loader.isModLoaded("fluidlogged_api")) {
            FluidloggedCompat.setFluidloggableBlock(world, pos, state, 2);
        } else {
            chunk.setBlockState(pos, state);
        }
    }

    private static void ensureNoise(long worldSeed) {
        if (geodeNoise == null || noiseSeed != worldSeed) {
            noiseSeed = worldSeed;
            geodeNoise = LegacyNormalNoise118.geodeNoise(worldSeed);
        }
    }

    private static boolean areAllChunksLoaded(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                              int minX, int maxX, int minZ, int maxZ) {
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                if (loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, chunkX << 4, chunkZ << 4) == null) return false;
            }
        }
        return true;
    }

    private static Chunk loadedChunk(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkX == sourceChunkX && chunkZ == sourceChunkZ) return sourceChunk;
        if (!(world instanceof WorldServer)) return null;
        return ((WorldServer) world).getChunkProvider().getLoadedChunk(chunkX, chunkZ);
    }

    private static IBlockState state(Chunk chunk, BlockPos pos) {
        return chunk.getBlockState(pos);
    }

    private static boolean isAir(IBlockState state) {
        return state.getBlock() == Blocks.AIR;
    }

    private static boolean isWater(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    private static boolean isGeodeInvalid(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.BEDROCK || isWater(state) || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA
                || block == Blocks.ICE || block == Blocks.PACKED_ICE;
    }

    private static boolean isCannotReplace(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.BEDROCK || block == Blocks.MOB_SPAWNER || block == Blocks.CHEST
                || block == Blocks.END_PORTAL_FRAME;
    }

    private static double distanceSquared(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dy = first.getY() - second.getY();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static double inverseSqrt(double value) {
        return 1.0D / Math.sqrt(value);
    }

    /** 对应GeodeFeature中的Pair<BlockPos, Integer>：点位与UniformInt[1,2]偏移。 */
    private static final class GeodePoint {
        private final BlockPos pos;
        private final int offset;

        private GeodePoint(BlockPos pos, int offset) {
            this.pos = pos;
            this.offset = offset;
        }
    }
}
