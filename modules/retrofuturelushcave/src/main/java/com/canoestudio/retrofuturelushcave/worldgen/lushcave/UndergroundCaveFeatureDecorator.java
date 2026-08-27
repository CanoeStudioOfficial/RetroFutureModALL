package com.canoestudio.retrofuturelushcave.worldgen.lushcave;

import com.canoestudio.retrofuturelushcave.config.Configuration;
import com.canoestudio.retrofuturelushcave.contents.blocks.CaveVine.CaveVine;
import com.canoestudio.retrofuturelushcave.contents.blocks.GlowLichenBlock;
import com.canoestudio.retrofuturelushcave.utils.FluidloggedCompat;

import com.canoestudio.retrofuturelushcave.contents.blocks.CaveVine.CaveVinePlant;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.blocks.PointedDripstoneBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.BigDripleaf;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.DripleafStem;
import com.canoestudio.retrofuturelushcave.contents.blocks.dripLeaf.SmallDripleaf;
import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;
import com.canoestudio.retrofuturelushcave.worldgen.WorldgenDiagnostics118;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;


import net.minecraftforge.fml.common.Loader;

import static com.canoestudio.retrofuturelushcave.RetroFutureLushCave.LOGGER;

/**
 * 1.12适配的1.18地下洞穴特征装饰器。
 *
 * <p>所有放置都通过当前Chunk的{@link Chunk#setBlockState(BlockPos, IBlockState)}进行，绝不在
 * Populate阶段调用World#setBlockState；这样繁茂植物、滴水石和水下岩浆块不会触发邻居/流体更新
 * 链。它不修改World#getBiome，而是用{@link UndergroundRegionSelector}在地下空腔内选择区域。</p>
 */
public final class UndergroundCaveFeatureDecorator {
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 255;
    private static final long FEATURE_SALT = 0x4C55534843415645L;
    private static final long GLOW_LICHEN_SALT = 0x474C4F574C494348L;
    private static final long UNDERWATER_MAGMA_SALT = 0x554E4445524D4147L;

    /**
     * 临时排障开关：记录新生成区块中有限数量的裸露天然洞穴地板。
     * false时不扫描、不分配、不输出；用于判断裸岩是否处在严格LUSH口袋之外，或是苔藓补丁漏覆盖。
     */
    public static final boolean DEBUG_LUSH_BARE_FLOOR_DIAGNOSTIC = false;
    /** 设为截图/F3坐标右移4位得到的Chunk坐标；保留MIN_VALUE时诊断不运行。 */
    public static final int DEBUG_LUSH_BARE_FLOOR_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_LUSH_BARE_FLOOR_CHUNK_Z = Integer.MIN_VALUE;
    private static final int BARE_FLOOR_DIAGNOSTIC_MAX_LINES = 96;
    private static long bareFloorDiagnosticSeed = Long.MIN_VALUE;
    private static int bareFloorDiagnosticLines;

    /** 默认关闭：只在指定区块输出滴水石候选、区域命中和实际写入统计。 */
    public static final boolean DEBUG_DRIPSTONE_DIAGNOSTIC = false;
    /** 默认关闭：仅在指定调试区块跳过continentalness/depth区域筛选，仍保留洞穴扫描与放置条件。 */
    public static final boolean DEBUG_FORCE_DRIPSTONE_REGION = false;
    public static final int DEBUG_DRIPSTONE_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_DRIPSTONE_CHUNK_Z = Integer.MIN_VALUE;

    /** 默认关闭：仅统计指定Chunk的官方UNDERWATER_MAGMA候选和实际放置数。 */
    public static final boolean DEBUG_UNDERWATER_MAGMA_DIAGNOSTIC = false;
    public static final int DEBUG_UNDERWATER_MAGMA_CHUNK_X = Integer.MIN_VALUE;
    public static final int DEBUG_UNDERWATER_MAGMA_CHUNK_Z = Integer.MIN_VALUE;

    private static long cachedSeed = Long.MIN_VALUE;
    private static DensityCave118Generator generator;

    private static UndergroundRegionSelector regions;

    /**
     * 由统一Mixin在 biome.decorate() 后直接调用。该路径不依赖 TerrainGen 事件注册，
     * 因而不会因装饰器漏注册而跳过全部滴水石候选。
     */
    public static void populateDripstoneFeatures(World world, int chunkX, int chunkZ) {
        if (world.isRemote || world.provider.getDimension() != 0) {
            return;
        }
        ensureGenerator(world.getSeed());
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        regions.recordClimateDiagnostic(world.getSeed(), chunkX, chunkZ);
        boolean debugDripstone = isDebugDripstoneChunk(chunkX, chunkZ);
        decorateDripstone(world, chunk, baseX, baseZ,
                new Random(mixSeed(world.getSeed(), chunkX, chunkZ, FEATURE_SALT)), debugDripstone);
        decorateUnderwaterMagma(world, chunk, baseX, baseZ,
                new Random(mixSeed(world.getSeed(), chunkX, chunkZ, UNDERWATER_MAGMA_SALT)),
                DEBUG_UNDERWATER_MAGMA_DIAGNOSTIC
                        && chunkX == DEBUG_UNDERWATER_MAGMA_CHUNK_X
                        && chunkZ == DEBUG_UNDERWATER_MAGMA_CHUNK_Z);
    }

    /**
     * 在ChunkGeneratorOverworld#populate的biome.decorate之后运行LUSH_CAVES的PlacedFeature链。
     * 与1.18一样，62次候选、全高度范围和12格环境扫描属于特征调度层，而非Post补丁。
     */
    public static void populateLushPlacedFeatures(World world, int chunkX, int chunkZ) {
        if (world.isRemote || world.provider.getDimension() != 0) {
            return;
        }
        /* 单一地下特征入口：即使旧统一Mixin只调用本方法，滴水石也不会被遗漏。 */
        populateDripstoneFeatures(world, chunkX, chunkZ);
        /* 当前populate目标已由调用方加载；这里只读取它，不触发任何相邻区块加载。 */
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        ensureGenerator(world.getSeed());
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        /* globalOverworldGeneration中的GLOW_LICHEN先于LUSH_CAVES的Biome特征执行。 */
        decorateGlowLichen(world, chunk, baseX, baseZ,
                new Random(mixSeed(world.getSeed(), chunkX, chunkZ, GLOW_LICHEN_SALT)));
        List<BlockPos> placedClassicVines = new ArrayList<BlockPos>();
        decorateLush(world, chunk, baseX, baseZ,
                new Random(mixSeed(world.getSeed(), chunkX, chunkZ, FEATURE_SALT)), placedClassicVines);
        removeUnsupportedClassicVines(world, chunk, placedClassicVines);
    }

        /**
     * CavePlacements.UNDERWATER_MAGMA / CaveFeatures.UNDERWATER_MAGMA 的1.12适配：
     * Count=Uniform[44,52]、bottom..max terrain、OCEAN_FLOOR_WG以下至少2格；配置为
     * floorSearchRange=5、placementRadiusAroundFloor=1、placementProbability=0.5。
     */
    private static void decorateUnderwaterMagma(World world, Chunk chunk, int baseX, int baseZ,
                                                Random random, boolean diagnostic) {
        int attempts = Configuration.UNDERGROUND_FEATURES.underwaterMagmaMinimumAttempts
                + random.nextInt(Configuration.UNDERGROUND_FEATURES.underwaterMagmaAdditionalAttempts + 1);
        int belowOceanFloor = 0;
        int floorHits = 0;
        int placed = 0;
        for (int i = 0; i < attempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int y = randomLushPlacementY(random);
            if (y < MIN_Y || y > oceanFloorHeight(chunk, x, z) - 2) continue;
            belowOceanFloor++;
            int floorY = findUnderwaterMagmaFloor(chunk, x, y, z);
            if (floorY < MIN_Y) continue;
            floorHits++;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (random.nextFloat() >= Configuration.UNDERGROUND_FEATURES.underwaterMagmaPlacementProbability) continue;
                        int targetX = x + dx;
                        int targetY = floorY + dy;
                        int targetZ = z + dz;
                        if (!inside(baseX, baseZ, targetX, targetZ)
                                || targetY <= MIN_Y || targetY >= MAX_Y
                                || !isValidUnderwaterMagmaPlacement(chunk, targetX, targetY, targetZ)) continue;
                        set(chunk, targetX, targetY, targetZ, Blocks.MAGMA.getDefaultState());
                        placed++;
                    }
                }
            }
        }
        WorldgenDiagnostics118.recordUnderwaterMagma(world.getSeed(), attempts, belowOceanFloor, floorHits, placed);
        if (diagnostic) {
            LOGGER.info("[RetroFutureLushCave][UNDERWATER_MAGMA_DIAG] chunk={},{} attempts={} belowOceanFloor={} floorHits={} placed={}",
                    baseX >> 4, baseZ >> 4, attempts, belowOceanFloor, floorHits, placed);
        }
    }

    private static int findUnderwaterMagmaFloor(Chunk chunk, int x, int startY, int z) {
        /* 对应Column.scan(... floorSearchRange=5, water, non-water)：起点必须在水中，
         * 再在上下各4格内寻找水柱边界；返回底部非水方块的Y。 */
        if (startY <= MIN_Y || startY > MAX_Y || !isWater(chunk, x, startY, z)) return -1;
        for (int offset = 0; offset < 5; offset++) {
            int belowY = startY - offset;
            if (belowY <= MIN_Y) break;
            if (!isWater(chunk, x, belowY, z)) return belowY;
        }
        return -1;
    }

    private static boolean isValidUnderwaterMagmaPlacement(Chunk chunk, int x, int y, int z) {
        if (isWater(chunk, x, y, z) || isAir(chunk, x, y, z)
                || isWater(chunk, x, y - 1, z) || isAir(chunk, x, y - 1, z)) {
            return false;
        }
        return !isWater(chunk, x - 1, y, z) && !isAir(chunk, x - 1, y, z)
                && !isWater(chunk, x + 1, y, z) && !isAir(chunk, x + 1, y, z)
                && !isWater(chunk, x, y, z - 1) && !isAir(chunk, x, y, z - 1)
                && !isWater(chunk, x, y, z + 1) && !isAir(chunk, x, y, z + 1);
    }

    /**
     * CavePlacements.GLOW_LICHEN：Count=Uniform[104,157]、bottom..256、
InSquare、
     * oceanFloor以下至少13格。它属于globalOverworldGeneration，而非LUSH专属特征。
     */
    private static void decorateGlowLichen(World world, Chunk sourceChunk, int baseX, int baseZ, Random random) {
        int attempts = Configuration.UNDERGROUND_FEATURES.glowLichenMinimumAttempts
                + random.nextInt(Configuration.UNDERGROUND_FEATURES.glowLichenAdditionalAttempts + 1);
        int sourceChunkX = baseX >> 4;
        int sourceChunkZ = baseZ >> 4;
        for (int i = 0; i < attempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int y = randomLushPlacementY(random);
            if (y < MIN_Y || y > oceanFloorHeight(sourceChunk, x, z)
                    - Configuration.UNDERGROUND_FEATURES.glowLichenMinimumOceanFloorDepth) continue;
            placeGlowLichenFeature(world, sourceChunk, sourceChunkX, sourceChunkZ, x, y, z, random);
        }
    }

    /** Heightmap.OCEAN_FLOOR_WG的1.12近似：最上方空气/水层下方的第一个实体方块上方。 */
    private static int oceanFloorHeight(Chunk chunk, int x, int z) {
        for (int y = MAX_Y; y >= MIN_Y; y--) {
            if (!isAir(chunk, x, y, z) && !isWater(chunk, x, y, z)) return y + 1;
        }
        return MIN_Y;
    }

    /** GlowLichenConfiguration：searchRange=20、ceiling+wall、floor=false、spreadChance=0.5。 */
    private static void placeGlowLichenFeature(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                               int x, int y, int z, Random random) {
        BlockPos origin = new BlockPos(x, y, z);
        Chunk originChunk = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
        if (originChunk == null || !isAir(originChunk, x, y, z) && !isWater(originChunk, x, y, z)
                || !isEnclosedLichenCavity(originChunk, x, y, z)) return;

        List<EnumFacing> directions = glowLichenInitialDirections(random);
        for (EnumFacing face : directions) {
            if (tryPlaceGlowLichenFace(world, sourceChunk, sourceChunkX, sourceChunkZ, origin, face, true)) {
                if (random.nextFloat() < 0.5F) {
                    spreadGlowLichenFromFace(world, sourceChunk, sourceChunkX, sourceChunkZ, origin, face, random);
                }
                return;
            }
        }

        /* GlowLichenFeature在20格搜索范围内沿各个方向寻找第一个可附着空格。 */
        for (EnumFacing search : directions) {
            List<EnumFacing> faces = glowLichenInitialDirectionsExcept(random, search.getOpposite());
            for (int distance = 1; distance <= 20; distance++) {
                BlockPos candidate = origin.offset(search, distance);
                Chunk candidateChunk = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ,
                        candidate.getX(), candidate.getZ());
                if (candidateChunk == null) break;
                IBlockState candidateState = state(candidateChunk, candidate.getX(), candidate.getY(), candidate.getZ());
                if (!isAir(candidateChunk, candidate.getX(), candidate.getY(), candidate.getZ())
                        && !isWater(candidateChunk, candidate.getX(), candidate.getY(), candidate.getZ())
                        && candidateState.getBlock() != ModBlocks.GLOW_LICHEN) break;
                for (EnumFacing face : faces) {
                    if (tryPlaceGlowLichenFace(world, sourceChunk, sourceChunkX, sourceChunkZ, candidate, face, true)) {
                        if (random.nextFloat() < 0.5F) {
                            spreadGlowLichenFromFace(world, sourceChunk, sourceChunkX, sourceChunkZ, candidate, face, random);
                        }
                        return;
                    }
                }
            }
        }
    }

    private static List<EnumFacing> glowLichenInitialDirections(Random random) {
        List<EnumFacing> directions = new ArrayList<EnumFacing>();
        directions.add(EnumFacing.UP); // can_place_on_ceiling=true
        directions.add(EnumFacing.NORTH);
        directions.add(EnumFacing.EAST);
        directions.add(EnumFacing.SOUTH);
        directions.add(EnumFacing.WEST); // can_place_on_wall=true; can_place_on_floor=false
        Collections.shuffle(directions, random);
        return directions;
    }

    private static List<EnumFacing> glowLichenInitialDirectionsExcept(Random random, EnumFacing excluded) {
        List<EnumFacing> directions = glowLichenInitialDirections(random);
        directions.remove(excluded);
        return directions;
    }

    /** 对应MultifaceBlock.spreadFromFaceTowardRandomDirection的三段传播路径。 */
    private static boolean spreadGlowLichenFromFace(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                                    BlockPos pos, EnumFacing sourceFace, Random random) {
        List<EnumFacing> directions = new ArrayList<EnumFacing>();
        Collections.addAll(directions, EnumFacing.values());
        Collections.shuffle(directions, random);
        for (EnumFacing direction : directions) {
            if (direction.getAxis() == sourceFace.getAxis()) continue;

            /* 1) 同格增加垂直面。 */
            if (tryPlaceGlowLichenFace(world, sourceChunk, sourceChunkX, sourceChunkZ, pos, direction, false)) {
                return true;
            }
            /* 2) 沿新方向移动后，沿原面继续。 */
            BlockPos forward = pos.offset(direction);
            if (tryPlaceGlowLichenFace(world, sourceChunk, sourceChunkX, sourceChunkZ, forward, sourceFace, false)) {
                return true;
            }
            /* 3) 绕过边缘，到对侧附着。 */
            BlockPos around = forward.offset(sourceFace);
            if (tryPlaceGlowLichenFace(world, sourceChunk, sourceChunkX, sourceChunkZ, around,
                    direction.getOpposite(), false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初放遵守GlowLichenConfiguration.canBePlacedOn；传播阶段遵守MultifaceBlock对任意牢固面的规则。
     */
    private static boolean tryPlaceGlowLichenFace(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                                  BlockPos pos, EnumFacing face, boolean initialPlacement) {
        if (pos.getY() < MIN_Y || pos.getY() > MAX_Y) return false;
        Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, pos.getX(), pos.getZ());
        if (target == null) return false;
        if (!isEnclosedLichenCavity(target, pos.getX(), pos.getY(), pos.getZ())) return false;
        IBlockState current = state(target, pos.getX(), pos.getY(), pos.getZ());
        boolean currentIsLichen = current.getBlock() == ModBlocks.GLOW_LICHEN;
        if (!currentIsLichen && !isAir(target, pos.getX(), pos.getY(), pos.getZ())
                && !isWater(target, pos.getX(), pos.getY(), pos.getZ())) return false;

        BlockPos supportPos = pos.offset(face);
        Chunk supportChunk = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ,
                supportPos.getX(), supportPos.getZ());
        if (supportChunk == null) return false;
        IBlockState support = state(supportChunk, supportPos.getX(), supportPos.getY(), supportPos.getZ());
        if (initialPlacement ? !isGlowLichenInitialSupport(support.getBlock())
                : !support.isSideSolid(world, supportPos, face.getOpposite())) return false;

        GlowLichenBlock lichen = (GlowLichenBlock) ModBlocks.GLOW_LICHEN;
        IBlockState placed = lichen.getStateWithFace(current, face);
        if (placed == null) return false;
        setFluidloggableGlowLichen(world, target, pos, placed, isWater(target, pos.getX(), pos.getY(), pos.getZ()));
        return true;
    }

    private static boolean isGlowLichenInitialSupport(Block block) {
        return block == Blocks.STONE || block == ModBlocks.DRIPSTONE_BLOCK || block == ModBlocks.CALCITE
                || block == ModBlocks.TUFF || block == ModBlocks.DeepSlate;
    }

    private static void setFluidloggableGlowLichen(World world, Chunk chunk, BlockPos pos, IBlockState state,
                                                   boolean preserveWater) {
        if (preserveWater && Loader.isModLoaded("fluidlogged_api")) {
            FluidloggedCompat.setFluidloggableBlock(world, pos, state, 2);
        } else {
            chunk.setBlockState(pos, state);
        }
    }

    private static void ensureGenerator(long worldSeed) {

        if (generator == null || cachedSeed != worldSeed) {
            cachedSeed = worldSeed;
            generator = new DensityCave118Generator(worldSeed);

            regions = new UndergroundRegionSelector(generator);
        }
    }

    /**
     * 逐项执行BiomeDefaultFeatures#addLushCavesVegetationFeatures中的可移植PlacedFeature。
     *
     * <p>顺序严格对应1.18.2：CEILING_VEGETATION(125)、CAVE_VINES(188)、CLAY(62)、
     * LUSH_CAVES_VEGETATION(125)、SPORE_BLOSSOM(25)。每次候选均遵循
     * Count -> InSquare -> HeightRange(bottom..256) -> EnvironmentScan(12) -> Offset -> BiomeFilter。
     * ROOTED_AZALEA_TREE仍由独立的LushCaveRootSystemDecorator处理。</p>
     */
        private static void decorateLush(World world, Chunk chunk, int baseX, int baseZ, Random random,
                                     List<BlockPos> placedClassicVines) {
        int ceilingScanHits = 0;
        int ceilingEnclosedHits = 0;
        int ceilingRegionHits = 0;
        int floorScanHits = 0;
        int floorEnclosedHits = 0;
        int floorRegionHits = 0;
        int clayScanHits = 0;
        int clayEnclosedHits = 0;
        int clayRegionHits = 0;
        for (int i = 0; i < Configuration.LUSH_CAVES.ceilingVegetationAttempts; i++) {

            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            if (startY < MIN_Y) continue;
                        int ceilingAirY = scanUpForCeilingAir(chunk, x, startY, z, 12);
            if (ceilingAirY < 0) continue;
            ceilingScanHits++;
            if (!isEnclosedLushCavity(chunk, x, ceilingAirY, z)) continue;
            ceilingEnclosedHits++;
            if (!regions.isLushAt(x, ceilingAirY, z)) continue;
            ceilingRegionHits++;
            placeCeilingMossAndVines(world, chunk, baseX, baseZ, x, ceilingAirY, z, random);

        }

        for (int i = 0; i < Configuration.LUSH_CAVES.caveVineAttempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            if (startY < MIN_Y) continue;
            int ceilingAirY = scanUpForCeilingAir(chunk, x, startY, z, 12);
            if (ceilingAirY >= 0 && regions.isLushAt(x, ceilingAirY, z)) {
                placeCaveVineColumn(chunk, x, ceilingAirY, z, random, false);
            }
        }

        for (int i = 0; i < Configuration.LUSH_CAVES.classicVineAttempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int y = randomLushPlacementY(random);
            if (y >= MIN_Y && isEnclosedLushCavity(chunk, x, y, z)
                    && regions.isLushAt(x, y, z)) {
                placeClassicVine(world, chunk, x, y, z, placedClassicVines);
            }
        }

        for (int i = 0; i < Configuration.LUSH_CAVES.clayAttempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            if (startY < MIN_Y) continue;
                        int floorAirY = scanDownForFloorAir(chunk, x, startY, z, 12);
            if (floorAirY < 0) continue;
            clayScanHits++;
            if (!isEnclosedLushCavity(chunk, x, floorAirY, z)) continue;
            clayEnclosedHits++;
            if (!regions.isLushAt(x, floorAirY, z)) continue;
            clayRegionHits++;
            placeClayPatch(world, chunk, baseX, baseZ, x, floorAirY, z, random);

        }

        for (int i = 0; i < Configuration.LUSH_CAVES.floorVegetationAttempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            if (startY < MIN_Y) continue;
                        int floorAirY = scanDownForFloorAir(chunk, x, startY, z, 12);
            if (floorAirY < 0) continue;
            floorScanHits++;
            if (!isEnclosedLushCavity(chunk, x, floorAirY, z)) continue;
            floorEnclosedHits++;
            if (!regions.isLushAt(x, floorAirY, z)) continue;
            floorRegionHits++;
            placeMossPatch(world, chunk, baseX, baseZ, x, floorAirY, z, random);

        }

        for (int i = 0; i < Configuration.LUSH_CAVES.sporeBlossomAttempts; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            if (startY < MIN_Y) continue;
            int ceilingAirY = scanUpForCeilingAir(chunk, x, startY, z, 12);
            if (ceilingAirY >= 0 && regions.isLushAt(x, ceilingAirY, z)
                    && isAir(chunk, x, ceilingAirY, z)) {
                set(chunk, x, ceilingAirY, z, ModBlocks.SPORE_BLOSSOM.getDefaultState());
            }
        }

                WorldgenDiagnostics118.recordLushVegetation(world.getSeed(),
                Configuration.LUSH_CAVES.ceilingVegetationAttempts, ceilingScanHits, ceilingEnclosedHits, ceilingRegionHits,
                Configuration.LUSH_CAVES.floorVegetationAttempts, floorScanHits, floorEnclosedHits, floorRegionHits,
                Configuration.LUSH_CAVES.clayAttempts, clayScanHits, clayEnclosedHits, clayRegionHits);
        diagnoseBareCaveFloors(world, chunk, baseX, baseZ);

    }

        /**
     * 繁茂特征必须落在封闭地下空腔中。短距离特征扫描本身会把露天凹地、河床和地表树冠下的
     * 空气误识别为洞穴；这里要求同列上方仍有至少12格天然岩层，且30格内同时存在地板与顶棚。
     */
    private static boolean isEnclosedLushCavity(Chunk chunk, int x, int airY, int z) {
        if (airY <= MIN_Y || airY >= MAX_Y || !isAir(chunk, x, airY, z)) return false;
        int surfaceY = MIN_Y;
        for (int y = MAX_Y; y > airY; y--) {
            if (!isAir(chunk, x, y, z) && !isWater(chunk, x, y, z)) {
                surfaceY = y + 1;
                break;
            }
        }
        if (surfaceY - airY < 12) return false;
        int floorAir = findFloorAir(null, chunk, x, airY, z, 30);
        int ceilingAir = findCeilingAir(null, chunk, x, airY, z, 30);
        if (floorAir < 0 || ceilingAir < 0) return false;
        int roofY = ceilingAir + 1;
        for (int y = roofY; y < roofY + 8 && y <= MAX_Y; y++) {
            if (!isNaturalLushRoof(chunk, x, y, z)) return false;
        }
        return true;
    }

    /** 发光地衣属全局洞穴特征，但不能把被打开的山体外侧空气误判为地下洞腔。 */
    private static boolean isEnclosedLichenCavity(Chunk chunk, int x, int y, int z) {
        if (y <= MIN_Y || y >= MAX_Y || (!isAir(chunk, x, y, z) && !isWater(chunk, x, y, z))) return false;
        int surfaceY = MIN_Y;
        for (int scanY = MAX_Y; scanY > y; scanY--) {
            if (!isAir(chunk, x, scanY, z) && !isWater(chunk, x, scanY, z)) {
                surfaceY = scanY + 1;
                break;
            }
        }
        if (surfaceY - y < 12) return false;
        for (int roofY = y + 1; roofY <= y + 8 && roofY <= MAX_Y; roofY++) {
            if (!isNaturalLushRoof(chunk, x, roofY, z)) return false;
        }
        return true;
    }

    private static boolean isNaturalLushRoof(Chunk chunk, int x, int y, int z) {
        Block block = state(chunk, x, y, z).getBlock();
        /* 1.12的花岗岩/闪长岩/安山岩是Blocks.STONE的VARIANT，不是独立Blocks字段。 */
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == ModBlocks.DRIPSTONE_BLOCK
                || block == ModBlocks.CALCITE || block == ModBlocks.TUFF;
    }

    /**
     * 对裸露STONE/DEEPSLATE洞穴地板作限量诊断。Region=LUSH说明该位置理论上属于繁茂口袋而
     * 仍未被MOSS_PATCH触及；Region=NONE/DRIPSTONE则是严格三维地下选择器的正常分界，而不应强铺苔藓。
     */
    private static void diagnoseBareCaveFloors(World world, Chunk chunk, int baseX, int baseZ) {
        if (!DEBUG_LUSH_BARE_FLOOR_DIAGNOSTIC
                || DEBUG_LUSH_BARE_FLOOR_CHUNK_X == Integer.MIN_VALUE
                || DEBUG_LUSH_BARE_FLOOR_CHUNK_Z == Integer.MIN_VALUE
                || (baseX >> 4) != DEBUG_LUSH_BARE_FLOOR_CHUNK_X
                || (baseZ >> 4) != DEBUG_LUSH_BARE_FLOOR_CHUNK_Z) return;
        synchronized (UndergroundCaveFeatureDecorator.class) {
            if (bareFloorDiagnosticSeed != world.getSeed()) {
                bareFloorDiagnosticSeed = world.getSeed();
                bareFloorDiagnosticLines = 0;
            }
            if (bareFloorDiagnosticLines >= BARE_FLOOR_DIAGNOSTIC_MAX_LINES) return;

            for (int localX = 0; localX < 16 && bareFloorDiagnosticLines < BARE_FLOOR_DIAGNOSTIC_MAX_LINES; localX += 2) {
                for (int localZ = 0; localZ < 16 && bareFloorDiagnosticLines < BARE_FLOOR_DIAGNOSTIC_MAX_LINES; localZ += 2) {
                    int x = baseX + localX;
                    int z = baseZ + localZ;
                    for (int y = MIN_Y + 1; y < MAX_Y && bareFloorDiagnosticLines < BARE_FLOOR_DIAGNOSTIC_MAX_LINES; y++) {
                        if (!isAir(chunk, x, y, z) || !isBareNaturalCaveFloor(chunk, x, y - 1, z)) continue;
                        UndergroundRegionSelector.Region region = regions.select(x, y, z);
                        double humidity = generator.sampleUndergroundHumidity(x, z);
                        double depth = generator.sampleUndergroundBiomeDepth(x, y, z);
                        System.out.println("[RetroFutureLushCave][LUSH_BARE_FLOOR] seed=" + world.getSeed()
                                + " pos=" + x + "," + y + "," + z
                                + " floor=" + state(chunk, x, y - 1, z).getBlock()
                                + " region=" + region
                                + " humidity=" + humidity
                                + " depth=" + depth);
                        bareFloorDiagnosticLines++;
                    }
                }
            }
        }
    }

    private static boolean isBareNaturalCaveFloor(Chunk chunk, int x, int y, int z) {
        Block block = state(chunk, x, y, z).getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate;
    }

    /**
     * VegetationFeatures.VINES的1.12适配。
     *
     * <p>1.18的VineBlock可持久化顶面连接；1.12的BlockVine却不把UP属性写入metadata。
     * 若把仅有UP=true的状态直接写入Chunk，重载后会成为没有横向连接的悬空藤蔓。因此这里
     * 仅选择四个水平支撑面，并以原版BlockVine.canPlaceBlockOnSide()复核存活条件。</p>
     */
    private static void placeClassicVine(World world, Chunk chunk, int x, int y, int z,
                                         List<BlockPos> placedClassicVines) {
        if (!isAir(chunk, x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        int sourceChunkX = x >> 4;
        int sourceChunkZ = z >> 4;
        EnumFacing[] horizontal = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
        for (EnumFacing supportDirection : horizontal) {
            BlockPos supportPos = pos.offset(supportDirection);
            Chunk supportChunk = loadedChunk(world, chunk, sourceChunkX, sourceChunkZ,
                    supportPos.getX(), supportPos.getZ());
            if (supportChunk == null) continue;
            /* 与BlockVine.canAttachTo相同：藤蔓面对支撑方块，支撑方块朝向藤蔓的一面必须坚固。 */
            if (!state(supportChunk, supportPos.getX(), supportPos.getY(), supportPos.getZ())
                    .isSideSolid(world, supportPos, supportDirection.getOpposite())) continue;
            /* 原版还要求上方为空、藤蔓或可作为顶面支撑；否则侧面连接会在下一次检查时失效。 */
            IBlockState above = state(chunk, x, y + 1, z);
            if (above.getBlock() != Blocks.AIR && above.getBlock() != Blocks.VINE
                    && !above.isSideSolid(world, pos.up(), EnumFacing.UP)) continue;
            set(chunk, x, y, z, Blocks.VINE.getDefaultState()
                    .withProperty(BlockVine.getPropertyFor(supportDirection), Boolean.valueOf(true)));
            placedClassicVines.add(pos);
            return;
        }
    }

    /**
     * Chunk#setBlockState不会触发BlockVine的邻居检查；因此对本装饰器写入的普通藤蔓做一次
     * 无更新校验。有效藤蔓必须有真实横向坚固面，或由上方同方向藤蔓继承连接。
     */
    private static void removeUnsupportedClassicVines(World world, Chunk chunk,
                                                      List<BlockPos> placedClassicVines) {
        EnumFacing[] horizontal = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
        for (BlockPos pos : placedClassicVines) {
            IBlockState vineState = state(chunk, pos.getX(), pos.getY(), pos.getZ());
            if (vineState.getBlock() != Blocks.VINE) continue;
            IBlockState above = state(chunk, pos.getX(), pos.getY() + 1, pos.getZ());
            boolean valid = false;
            for (EnumFacing direction : horizontal) {
                if (!vineState.getValue(BlockVine.getPropertyFor(direction))) continue;
                BlockPos supportPos = pos.offset(direction);
                Chunk supportChunk = loadedChunk(world, chunk, pos.getX() >> 4, pos.getZ() >> 4,
                        supportPos.getX(), supportPos.getZ());
                boolean directSupport = supportChunk != null
                        && state(supportChunk, supportPos.getX(), supportPos.getY(), supportPos.getZ())
                        .isSideSolid(world, supportPos, direction.getOpposite());
                boolean inheritedSupport = above.getBlock() == Blocks.VINE
                        && above.getValue(BlockVine.getPropertyFor(direction));
                if (directSupport || inheritedSupport) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                set(chunk, pos.getX(), pos.getY(), pos.getZ(), Blocks.AIR.getDefaultState());
            }
        }
    }

    /** CavePlacements：DRIPSTONE_CLUSTER 48..96、LARGE_DRIPSTONE 10..48。 */

    private static void decorateDripstone(World world, Chunk chunk, int baseX, int baseZ, Random random,
                                          boolean debug) {
        /* 全局汇总始终需要轻量计数；仅昂贵的方块总数扫描仍保留给定点调试。 */
        DripstoneDiagnostic diagnostic = new DripstoneDiagnostic();
        if (debug) diagnostic.beforePointed = countPointedDripstone(chunk);
        int clusters = Configuration.UNDERGROUND_FEATURES.smallDripstoneMinimumAttempts
                + random.nextInt(Configuration.UNDERGROUND_FEATURES.smallDripstoneAdditionalAttempts + 1);

        for (int i = 0; i < clusters; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            /* DripstoneClusterFeature#place：origin不是空格或水格时立即失败。 */
            if (startY < MIN_Y || (!isAir(chunk, x, startY, z) && !isWater(chunk, x, startY, z))) continue;
            int floorY = findFloorAir(world, chunk, x, startY, z, 12);
            if (diagnostic != null) diagnostic.smallAttempts++;
            if (floorY >= 0) {
                if (diagnostic != null) {
                    diagnostic.smallFloorHits++;
                    diagnostic.recordClimate(generator.sampleUndergroundBiomeDepth(x, floorY, z),
                            generator.sampleUndergroundContinentalness(x, z));
                }
                /* CavePlacements最后的BiomeFilter不是“所有主世界洞穴均执行”，而是仅在
                 * Dripstone Caves生物群系的特征表中执行。1.12没有垂直Biome，因此使用
                 * UndergroundRegionSelector的原版参数点等价选择；这避免每一个普通洞穴铺满钟乳石。 */
                boolean dripstoneRegion = (debug && DEBUG_FORCE_DRIPSTONE_REGION)
                        || regions.isDripstoneAt(x, floorY, z);
                if (!dripstoneRegion) continue;
                if (diagnostic != null) diagnostic.smallRegionHits++;
                if (diagnostic != null) diagnostic.smallPlacementCalls++;
                placeSmallDripstoneCluster(world, chunk, baseX, baseZ, x, floorY, z, random);
            }

        }

        int large = Configuration.UNDERGROUND_FEATURES.largeDripstoneMinimumAttempts
                + random.nextInt(Configuration.UNDERGROUND_FEATURES.largeDripstoneAdditionalAttempts + 1);
        for (int i = 0; i < large; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            int startY = randomLushPlacementY(random);
            /* LargeDripstoneFeature同样要求origin为空气或水，再做Column.scan。 */
            if (startY < MIN_Y || (!isAir(chunk, x, startY, z) && !isWater(chunk, x, startY, z))) continue;
            int floorY = findFloorAir(world, chunk, x, startY, z, 30);
            if (diagnostic != null) diagnostic.largeAttempts++;
            if (floorY >= 0) {
                if (diagnostic != null) {
                    diagnostic.largeFloorHits++;
                    diagnostic.recordClimate(generator.sampleUndergroundBiomeDepth(x, floorY, z),
                            generator.sampleUndergroundContinentalness(x, z));
                }
                boolean dripstoneRegion = (debug && DEBUG_FORCE_DRIPSTONE_REGION)
                        || regions.isDripstoneAt(x, floorY, z);
                if (!dripstoneRegion) continue;
                if (diagnostic != null) diagnostic.largeRegionHits++;
                if (diagnostic != null) diagnostic.largePlacementCalls++;
                placeLargeDripstone(world, chunk, baseX, baseZ, x, floorY, z, random);
            }
        }
        WorldgenDiagnostics118.recordDripstone(world.getSeed(), diagnostic.smallAttempts, diagnostic.smallFloorHits,
                diagnostic.smallRegionHits, diagnostic.smallPlacementCalls, diagnostic.largeAttempts,
                diagnostic.largeFloorHits, diagnostic.largeRegionHits, diagnostic.largePlacementCalls);
        if (debug) {
            diagnostic.afterPointed = countPointedDripstone(chunk);
            LOGGER.info("[RetroFutureLushCave][DRIPSTONE_DIAG] seed={} chunk={},{} forceRegion=" + DEBUG_FORCE_DRIPSTONE_REGION + " small={}/{}/{}/{} large={}/{}/{}/{} depth[0.2,0.9]={}/{} continents>=0.80={}/{} depthRange={}..{} continentsRange={}..{} pointedWritten={}", world.getSeed(), baseX >> 4, baseZ >> 4, diagnostic.smallAttempts, diagnostic.smallFloorHits, diagnostic.smallRegionHits, diagnostic.smallPlacementCalls, diagnostic.largeAttempts, diagnostic.largeFloorHits, diagnostic.largeRegionHits, diagnostic.largePlacementCalls, diagnostic.depthInVanillaRange, diagnostic.climateSamples, diagnostic.continentsAtVanillaThreshold, diagnostic.climateSamples, diagnostic.depthMin, diagnostic.depthMax, diagnostic.continentsMin, diagnostic.continentsMax, diagnostic.afterPointed - diagnostic.beforePointed);
        }
    }

    private static boolean isDebugDripstoneChunk(int chunkX, int chunkZ) {
        return DEBUG_DRIPSTONE_DIAGNOSTIC
                && DEBUG_DRIPSTONE_CHUNK_X != Integer.MIN_VALUE
                && DEBUG_DRIPSTONE_CHUNK_Z != Integer.MIN_VALUE
                && chunkX == DEBUG_DRIPSTONE_CHUNK_X
                && chunkZ == DEBUG_DRIPSTONE_CHUNK_Z;
    }

    private static int countPointedDripstone(Chunk chunk) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    if (chunk.getBlockState(new BlockPos(x, y, z)).getBlock() == ModBlocks.POINTED_DRIPSTONE) count++;
                }
            }
        }
        return count;
    }

    private static final class DripstoneDiagnostic {
        int beforePointed;
        int afterPointed;
        int smallAttempts;
        int smallFloorHits;
        int smallRegionHits;
        int smallPlacementCalls;
        int largeAttempts;
        int largeFloorHits;
        int largeRegionHits;
        int largePlacementCalls;
        int climateSamples;
        int depthInVanillaRange;
        int continentsAtVanillaThreshold;
        double depthMin = Double.POSITIVE_INFINITY;
        double depthMax = Double.NEGATIVE_INFINITY;
        double continentsMin = Double.POSITIVE_INFINITY;
        double continentsMax = Double.NEGATIVE_INFINITY;

        void recordClimate(double depth, double continentalness) {
            climateSamples++;
            if (depth >= 0.20D && depth <= 0.90D) depthInVanillaRange++;
            if (continentalness >= 0.80D) continentsAtVanillaThreshold++;
            depthMin = Math.min(depthMin, depth);
            depthMax = Math.max(depthMax, depth);
            continentsMin = Math.min(continentsMin, continentalness);
            continentsMax = Math.max(continentsMax, continentalness);
        }
    }

    /**
     * CaveFeatures.MOSS_PATCH的1.12移植。
     *
     * <p>原版参数为：depth=1、verticalRange=5、vegetationChance=0.8、
     * xzRadius=Uniform[4,7]（Feature内部会各加1）、extraEdgeColumnChance=0.3。
     * 它是矩形列补丁而非椭圆；不写入未加载邻块，但允许一个已加载候选自然延伸到已加载相邻块。</p>
     */
    private static void placeMossPatch(World world, Chunk sourceChunk, int baseX, int baseZ,
                                       int centerX, int floorAirY, int centerZ, Random random) {
        int radiusX = 5 + random.nextInt(4);
        int radiusZ = 5 + random.nextInt(4);
        int sourceChunkX = baseX >> 4;
        int sourceChunkZ = baseZ >> 4;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            boolean xEdge = dx == -radiusX || dx == radiusX;
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                boolean zEdge = dz == -radiusZ || dz == radiusZ;
                boolean corner = xEdge && zEdge;
                boolean edge = xEdge || zEdge;
                if (corner || (edge && random.nextFloat() > 0.3F)) continue;

                int x = centerX + dx;
                int z = centerZ + dz;
                Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
                if (target == null) continue;

                int groundY = findVegetationPatchGround(target, x, floorAirY, z, 5);
                if (groundY < MIN_Y || !isMossReplaceable(target, x, groundY, z)) continue;

                target.setBlockState(new BlockPos(x, groundY, z), ModBlocks.MOSS_BLOCK.getDefaultState());
                if (random.nextFloat() < 0.8F && groundY + 1 <= MAX_Y && isAir(target, x, groundY + 1, z)) {
                    placeMossVegetation(world, target, x, groundY + 1, z, random);
                }
            }
        }
    }

    /**
     * 精确移植VegetationPatchFeature / WaterloggedVegetationPatchFeature的平面算法。
     * 原版并非椭圆盆地：半径Uniform[4,7]+1，角落不放置，边缘列按extraEdgeColumnChance取舍；
     * 含水版本仅把五向都不暴露的内部黏土顶层替换为水，天然随洞穴地面起伏形成梯田水洼。
     */
    /**
     * 1.12适配：一个原版VegetationPatchFeature候选可跨Chunk延伸。这里绝不加载邻区块，
     * 只向当前已加载集合写入对应列；未加载列被省略，且水化时会视为暴露边界，避免产生半截源水或触发区块加载。
     */
    private static void placeClayPatch(World world, Chunk sourceChunk, int baseX, int baseZ,
                                       int centerX, int floorAirY, int centerZ, Random random) {
        /* RandomBooleanSelectorFeature：true=featureTrue=CLAY_WITH_DRIPLEAVES，false=含水池。 */
        boolean waterPool = !random.nextBoolean();
        int radiusX = 5 + random.nextInt(4);
        int radiusZ = 5 + random.nextInt(4);
        int verticalRange = waterPool ? 5 : 2;
        int sourceChunkX = baseX >> 4;
        int sourceChunkZ = baseZ >> 4;
        Map<BlockPos, Chunk> plannedClay = new HashMap<BlockPos, Chunk>();
        Map<BlockPos, Chunk> ground = new HashMap<BlockPos, Chunk>();

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            boolean xEdge = dx == -radiusX || dx == radiusX;
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                boolean zEdge = dz == -radiusZ || dz == radiusZ;
                boolean corner = xEdge && zEdge;
                boolean edge = xEdge || zEdge;
                /* CaveFeatures: corners排除，其余边缘列以extraEdgeColumnChance=0.7保留。 */
                if (corner || (edge && random.nextFloat() > 0.7F)) continue;
                int x = centerX + dx;
                int z = centerZ + dz;
                Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
                /* 1.12没有WorldGenRegion：未加载邻块仅跳过该列，不得放弃整个候选Patch。 */
                if (target == null) continue;

                int groundY = findVegetationPatchGround(target, x, floorAirY, z, verticalRange);
                if (groundY < MIN_Y || !isMossReplaceable(target, x, groundY, z)) continue;
                int depth = 3 + (random.nextFloat() < 0.8F ? 1 : 0);
                boolean placed = false;
                for (int d = 0; d < depth && groundY - d >= MIN_Y; d++) {
                    if (!isMossReplaceable(target, x, groundY - d, z)) break;
                    plannedClay.put(new BlockPos(x, groundY - d, z), target);
                    placed = true;
                }
                if (placed) ground.put(new BlockPos(x, groundY, z), target);
            }
        }

        /* 对整个拓扑先铺黏土，再按原版WaterloggedVegetationPatchFeature检查五个牢固方块面。 */
        for (Map.Entry<BlockPos, Chunk> entry : plannedClay.entrySet()) {
            BlockPos pos = entry.getKey();
            entry.getValue().setBlockState(pos, Blocks.CLAY.getDefaultState());
        }
        Map<BlockPos, Chunk> vegetationGround = ground;
        if (waterPool) {
            Map<BlockPos, Chunk> waterCells = new HashMap<BlockPos, Chunk>();
            for (Map.Entry<BlockPos, Chunk> entry : ground.entrySet()) {
                if (!isPatchGroundExposed(world, sourceChunk, sourceChunkX, sourceChunkZ, entry.getKey())) {
                    waterCells.put(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<BlockPos, Chunk> entry : waterCells.entrySet()) {
                entry.getValue().setBlockState(entry.getKey(), Blocks.WATER.getDefaultState());
            }
            vegetationGround = waterCells;
        }

        /* CaveFeatures：普通黏土层植被概率0.05，含水黏土池为0.10。 */
        float vegetationChance = waterPool ? 0.10F : 0.05F;
        for (Map.Entry<BlockPos, Chunk> entry : vegetationGround.entrySet()) {
            if (random.nextFloat() < vegetationChance) {
                BlockPos pos = entry.getKey();
                int plantY = waterPool ? pos.getY() : pos.getY() + 1;
                placeDripleaf(world, entry.getValue(), pos.getX(), plantY, pos.getZ(), random, waterPool);
            }
        }
    }

    /** 对应VegetationPatchFeature在surface=FLOOR、verticalRange=5下寻找局部地面。 */
    private static int findVegetationPatchGround(Chunk chunk, int x, int startAirY, int z, int verticalRange) {
        int y = clampY(startAirY);
        for (int step = 0; step < verticalRange && isAir(chunk, x, y, z); step++) y--;
        for (int step = 0; step < verticalRange && !isAir(chunk, x, y, z); step++) y++;
        return y > MIN_Y && isAir(chunk, x, y, z) && isSolid(chunk, x, y - 1, z) ? y - 1 : -1;
    }

    /**
     * WaterloggedVegetationPatchFeature的isExposed：在地面补丁完成后，检查五个相邻方块的牢固面。
     * Mojang源码的patch参数也未在isExposedDirection中使用；水化依据是生成后的实际方块面。
     */
    private static boolean isPatchGroundExposed(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                                BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return !isSolidAt(world, sourceChunk, sourceChunkX, sourceChunkZ, x - 1, y, z)
                || !isSolidAt(world, sourceChunk, sourceChunkX, sourceChunkZ, x + 1, y, z)
                || !isSolidAt(world, sourceChunk, sourceChunkX, sourceChunkZ, x, y, z - 1)
                || !isSolidAt(world, sourceChunk, sourceChunkX, sourceChunkZ, x, y, z + 1)
                || !isSolidAt(world, sourceChunk, sourceChunkX, sourceChunkZ, x, y - 1, z);
    }

    private static boolean isSolidAt(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ,
                                     int x, int y, int z) {
        Chunk chunk = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
        return chunk != null && isSolid(chunk, x, y, z);
    }

    private static Chunk loadedChunk(World world, Chunk sourceChunk, int sourceChunkX, int sourceChunkZ, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkX == sourceChunkX && chunkZ == sourceChunkZ) return sourceChunk;
        if (!(world instanceof WorldServer)) return null;
        return ((WorldServer) world).getChunkProvider().getLoadedChunk(chunkX, chunkZ);
    }

    /**
     * CaveFeatures.MOSS_PATCH_CEILING：surface=CEILING、depth=Uniform[1,2]、
     * verticalRange=5、vegetationChance=0.08、xzRadius=Uniform[4,7]+1、edge=0.3。
     */
    private static void placeCeilingMossAndVines(World world, Chunk sourceChunk, int baseX, int baseZ,
                                                 int centerX, int ceilingAirY, int centerZ, Random random) {
        int radiusX = 5 + random.nextInt(4);
        int radiusZ = 5 + random.nextInt(4);
        int sourceChunkX = baseX >> 4;
        int sourceChunkZ = baseZ >> 4;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            boolean xEdge = dx == -radiusX || dx == radiusX;
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                boolean zEdge = dz == -radiusZ || dz == radiusZ;
                boolean corner = xEdge && zEdge;
                boolean edge = xEdge || zEdge;
                if (corner || (edge && random.nextFloat() > 0.3F)) continue;

                int x = centerX + dx;
                int z = centerZ + dz;
                Chunk target = loadedChunk(world, sourceChunk, sourceChunkX, sourceChunkZ, x, z);
                if (target == null) continue;

                int supportY = findCeilingPatchSupport(target, x, ceilingAirY, z, 5);
                if (supportY < MIN_Y || !isMossReplaceable(target, x, supportY, z)) continue;

                /* CaveFeatures.MOSS_PATCH_CEILING depth=Uniform[1,2]，向上替换天花板岩层。 */
                int depth = 1 + random.nextInt(2);
                for (int layer = 0; layer < depth && supportY + layer <= MAX_Y; layer++) {
                    if (!isMossReplaceable(target, x, supportY + layer, z)) break;
                    target.setBlockState(new BlockPos(x, supportY + layer, z), ModBlocks.MOSS_BLOCK.getDefaultState());
                }

                /* inlinePlaced(CAVE_VINE_IN_MOSS)：仅8%顶面补丁列尝试短藤蔓。 */
                if (random.nextFloat() < 0.08F && supportY - 1 >= MIN_Y && isAir(target, x, supportY - 1, z)) {
                    placeCaveVineColumn(target, x, supportY - 1, z, random, true);
                }
            }
        }
    }

    /**
     * CaveFeatures.MOSS_VEGETATION的WeightedStateProvider：繁花杜鹃4、杜鹃7、
     * 覆地苔藓25、草50、高草10（总权重96）。
     *
     * <p>1.12没有独立的1.18单方块/双方块高草状态映射；最后10权重退化为蕨类，
     * 但绝不使用BlockTallGrass的默认DEAD_BUSH状态。</p>
     */
    private static void placeMossVegetation(World world, Chunk chunk, int x, int y, int z, Random random) {
        int value = random.nextInt(96);
        IBlockState state;
        if (value < 4) {
            state = ModBlocks.Flowering_Azalea.getDefaultState();
        } else if (value < 11) {
            state = ModBlocks.Azalea.getDefaultState();
        } else if (value < 36) {
            state = ModBlocks.MOSS_CARPET.getDefaultState();
        } else if (value < 86) {
            state = Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
        } else {
            state = Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN);
        }

        /* SimpleBlockFeature：只有目标方块能在当前位置存活时才实际写入。 */
        if (isAir(chunk, x, y, z) && canSimpleVegetationSurvive(world, chunk, x, y, z, state)) {
            set(chunk, x, y, z, state);
        }
    }

    private static boolean canSimpleVegetationSurvive(World world, Chunk chunk, int x, int y, int z, IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.TALLGRASS || block == ModBlocks.MOSS_CARPET) {
            return y > MIN_Y && isSolid(chunk, x, y - 1, z);
        }
        return block.canPlaceBlockAt(world, new BlockPos(x, y, z));
    }

    /**
     * CaveFeatures.CAVE_VINE / CAVE_VINE_IN_MOSS的BlockColumnFeature移植。
     * prioritizeTip=true会在空间不足时先截去主体、保留末端；主体与末端均以4:1生成浆果。
     */
    private static void placeCaveVineColumn(Chunk chunk, int x, int startY, int z, Random random, boolean inMoss) {
        if (startY < MIN_Y || !isAir(chunk, x, startY, z)) return;

        int bodyLength;
        if (inMoss) {
            /* WeightedListInt：Uniform[0,3]权重5；Uniform[1,7]权重1。 */
            bodyLength = random.nextInt(6) < 5 ? random.nextInt(4) : 1 + random.nextInt(7);
        } else {
            /* WeightedListInt：Uniform[0,19]权重2；Uniform[0,2]权重3；Uniform[0,6]权重10。 */
            int roll = random.nextInt(15);
            bodyLength = roll < 2 ? random.nextInt(20) : (roll < 5 ? random.nextInt(3) : random.nextInt(7));
        }

        int totalLength = bodyLength + 1; // 末端层固定1格
        int available = 0;
        for (int y = startY; available < totalLength && y >= MIN_Y && isAir(chunk, x, y, z); y--) {
            available++;
        }
        if (available == 0) return;

        /* BlockColumnFeature.truncate(..., prioritizeTip=true)：优先从主体层剔除超出部分。 */
        if (available < totalLength) {
            bodyLength = Math.max(0, bodyLength - (totalLength - available));
        }

        int y = startY;
        for (int i = 0; i < bodyLength; i++, y--) {
            set(chunk, x, y, z, ModBlocks.CAVE_VINE_PLANT.getDefaultState()
                    .withProperty(CaveVinePlant.BERRIES, Boolean.valueOf(random.nextInt(5) == 0)));
        }
        IBlockState head = ModBlocks.CAVE_VINE.getDefaultState()
                .withProperty(CaveVinePlant.BERRIES, Boolean.valueOf(random.nextInt(5) == 0))
                /* CaveVinesBlock生成状态：AGE_25范围内的23..25。 */
                .withProperty(CaveVine.AGE, Integer.valueOf(23 + random.nextInt(3)));
        set(chunk, x, y, z, head);
    }

    private static void placeDripleaf(World world, Chunk chunk, int x, int y, int z, Random random, boolean waterPool) {
        /* SimpleRandomSelectorFeature：先nextInt(5)，索引0为小叶，1..4固定对应东、西、南、北。 */
        int variant = random.nextInt(5);
        if (variant == 0) {
            /* makeSmallDripleaf中的WeightedStateProvider：四个朝向权重均为1。 */
            EnumFacing facing = EnumFacing.byHorizontalIndex(random.nextInt(4));
            if (y + 1 <= MAX_Y && canOccupyDripleafCell(chunk, x, y, z, waterPool)
                    && canOccupyDripleafCell(chunk, x, y + 1, z, false)) {
                setFluidloggableDripleaf(world, chunk, x, y, z, ModBlocks.SMALL_DRIPLEAF.getDefaultState()
                        .withProperty(SmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
                        .withProperty(SmallDripleaf.FACING, facing), waterPool);
                set(chunk, x, y + 1, z, ModBlocks.SMALL_DRIPLEAF.getDefaultState()
                        .withProperty(SmallDripleaf.FACING, facing));
            }
            return;
        }

        EnumFacing facing;
        switch (variant) {
            case 1: facing = EnumFacing.EAST; break;
            case 2: facing = EnumFacing.WEST; break;
            case 3: facing = EnumFacing.SOUTH; break;
            default: facing = EnumFacing.NORTH; break;
        }

        /* WeightedListInt：Uniform[0,4]的权重2、Constant(0)的权重1。 */
        int stemCount = random.nextInt(3) < 2 ? random.nextInt(5) : 0;
        if (y + stemCount > MAX_Y) return;
        for (int i = 0; i <= stemCount; i++) {
            if (!canOccupyDripleafCell(chunk, x, y + i, z, waterPool && i == 0)) return;
        }
        for (int i = 0; i < stemCount; i++) {
            IBlockState stem = ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(DripleafStem.FACING, facing);
            if (i == 0) setFluidloggableDripleaf(world, chunk, x, y + i, z, stem, waterPool);
            else set(chunk, x, y + i, z, stem);
        }
        IBlockState leaf = ModBlocks.BIG_DRIPLEAF.getDefaultState().withProperty(BigDripleaf.FACING, facing)
                .withProperty(BigDripleaf.TILT, BigDripleaf.EnumTilt.NONE);
        if (stemCount == 0) setFluidloggableDripleaf(world, chunk, x, y, z, leaf, waterPool);
        else set(chunk, x, y + stemCount, z, leaf);
    }

    private static boolean canOccupyDripleafCell(Chunk chunk, int x, int y, int z, boolean allowWater) {
        return isAir(chunk, x, y, z) || (allowWater && isWater(chunk, x, y, z));
    }

    /** Fluidlogged API存在时保留水；无该API时退化为1.12普通水中植物的视觉近似。 */
    private static void setFluidloggableDripleaf(World world, Chunk chunk, int x, int y, int z, IBlockState state,
                                                 boolean preserveWater) {
        BlockPos pos = new BlockPos(x, y, z);
        if (preserveWater && Loader.isModLoaded("fluidlogged_api")) {
            FluidloggedCompat.setFluidloggableBlock(world, pos, state, 2);
        } else {
            chunk.setBlockState(pos, state);
        }
    }

    /** 对应VegetationPatchFeature在surface=CEILING、verticalRange=5下寻找可替换天花板支撑方块。 */
    private static int findCeilingPatchSupport(Chunk chunk, int x, int startAirY, int z, int verticalRange) {
        int y = clampY(startAirY);
        for (int step = 0; step < verticalRange && isAir(chunk, x, y, z); step++) y++;
        for (int step = 0; step < verticalRange && !isAir(chunk, x, y, z); step++) y--;
        return y < MAX_Y && isAir(chunk, x, y, z) && isSolid(chunk, x, y + 1, z) ? y + 1 : -1;
    }

    /** 对应EnvironmentScanPlacement.scanningFor(DOWN, solid, ONLY_IN_AIR, 12)后再+1。 */
    private static int scanDownForFloorAir(Chunk chunk, int x, int startY, int z, int maxSteps) {
        for (int step = 0, y = clampY(startY); step <= maxSteps && y > MIN_Y; step++, y--) {
            if (isSolid(chunk, x, y, z)) {
                return y + 1 <= MAX_Y && isAir(chunk, x, y + 1, z) ? y + 1 : -1;
            }
            if (!isAir(chunk, x, y, z)) return -1;
        }
        return -1;
    }

    /** 对应EnvironmentScanPlacement.scanningFor(UP, solid, ONLY_IN_AIR, 12)后再-1。 */
    private static int scanUpForCeilingAir(Chunk chunk, int x, int startY, int z, int maxSteps) {
        for (int step = 0, y = clampY(startY); step <= maxSteps && y < MAX_Y; step++, y++) {
            if (isSolid(chunk, x, y, z)) {
                return y - 1 >= MIN_Y && isAir(chunk, x, y - 1, z) ? y - 1 : -1;
            }
            if (!isAir(chunk, x, y, z)) return -1;
        }
        return -1;
    }


    private static void placeSmallDripstoneCluster(World world, Chunk chunk, int baseX, int baseZ,
                                                   int x, int floorY, int z, Random random) {
        /* CaveFeatures.DRIPSTONE_CLUSTER：height=Uniform[3,6]，wetness=Uniform[0.3,0.7]，
         * density=ClampedNormal(0.1,0.3,[0.1,0.9])，两条半径均为Uniform[2,8]。 */
        int maxHeight = 3 + random.nextInt(4);
        float wetness = 0.3F + random.nextFloat() * 0.4F;
        double density = Math.max(0.1D, Math.min(0.9D, 0.1D + random.nextGaussian() * 0.3D));
        int radiusX = 2 + random.nextInt(7);
        int radiusZ = 2 + random.nextInt(7);

        boolean poolPlaced = false;
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                int blockX = x + dx;
                int blockZ = z + dz;
                if (!inside(baseX, baseZ, blockX, blockZ)) continue;

                /* 官方按每列重新Column.scan；不能把中心洞顶/洞底复制到整个半径。 */
                int columnFloor = findFloorAir(world, chunk, blockX, floorY + 3, blockZ, 12);
                if (columnFloor < 0) continue;
                int columnCeiling = findCeilingAir(world, chunk, blockX, columnFloor + 3, blockZ, 12);
                if (columnCeiling < 0 || columnCeiling - columnFloor < 4
                        || !hasSubterraneanDripstoneRoof(chunk, blockX, columnCeiling, blockZ)) continue;

                int edge = Math.min(radiusX - Math.abs(dx), radiusZ - Math.abs(dz));
                double columnChance = edge <= 0 ? 0.1D : (edge >= 3 ? 1.0D : 0.1D + edge * 0.3D);
                int availableHeight = Math.min(maxHeight, (columnCeiling - columnFloor - 1) / 2);
                if (availableHeight <= 0) continue;

                /* 官方wetness会在可封闭的洞底把一个位置改为水，形成小水洼；本实现只写入
                 * 四周与底部均为天然岩层的单格水面，避免1.12动态水扩散。 */
                /* 官方每列独立抽湿度，但1.12无水含水方块；限制为每个簇至多一个不规则
                 * 封闭小水洼，防止静态水在平坦洞底形成规则网点。 */
                if (!poolPlaced && random.nextFloat() < wetness * 0.12F
                        && tryPlaceDripstonePool(chunk, blockX, columnFloor - 1, blockZ)) {
                    poolPlaced = true;
                    WorldgenDiagnostics118.recordDripstonePool(world.getSeed());
                    continue;
                }

                if (random.nextDouble() < columnChance && random.nextDouble() < density) {
                    int height = biasedDripstoneHeight(random, dx, dz, availableHeight);
                    if (height > 0) placeDripstoneColumn(chunk, blockX, columnFloor, blockZ, EnumFacing.UP, height);
                }
                if (random.nextDouble() < columnChance && random.nextDouble() < density) {
                    int height = biasedDripstoneHeight(random, dx, dz, availableHeight);
                    if (height > 0) placeDripstoneColumn(chunk, blockX, columnCeiling, blockZ, EnumFacing.DOWN, height);
                }
            }
        }
    }

    private static int biasedDripstoneHeight(Random random, int dx, int dz, int maxHeight) {
        int distance = Math.abs(dx) + Math.abs(dz);
        double bias = Math.max(0.0D, maxHeight * 0.5D - distance * 0.5D);
        return Math.max(0, Math.min(maxHeight, (int) Math.round(bias + random.nextGaussian() * 0.8D)));
    }

    private static boolean tryPlaceDripstonePool(Chunk chunk, int x, int floorBlockY, int z) {
        if (!isNaturalDripstoneBase(chunk, x, floorBlockY, z) || isWater(chunk, x, floorBlockY + 1, z)) return false;
        if (!isNaturalDripstoneBase(chunk, x - 1, floorBlockY, z)
                || !isNaturalDripstoneBase(chunk, x + 1, floorBlockY, z)
                || !isNaturalDripstoneBase(chunk, x, floorBlockY, z - 1)
                || !isNaturalDripstoneBase(chunk, x, floorBlockY, z + 1)
                || !isNaturalDripstoneBase(chunk, x, floorBlockY - 1, z)) return false;
        set(chunk, x, floorBlockY, z, Blocks.WATER.getDefaultState());
        return true;
    }

    private static void placeLargeDripstone(World world, Chunk chunk, int baseX, int baseZ,
                                            int x, int floorY, int z, Random random) {
        int ceilingY = findCeilingAir(world, chunk, x, floorY + 4, z, 30);
        int cavity = ceilingY - floorY;
        if (ceilingY < 0 || cavity < 8
                || !hasSubterraneanDripstoneRoof(chunk, x, ceilingY, z)
                || !isNaturalDripstoneBase(chunk, x, floorY - 1, z)) {
            return;
        }
        int radius = Math.min(7, Math.max(3, cavity / 6));
        int height = Math.min(cavity / 2 - 1, radius * 2 + random.nextInt(radius + 1));
        if (height < 3) {
            return;
        }
        placeLargeCone(chunk, baseX, baseZ, x, floorY - 1, z, radius, height, true);
        if (random.nextBoolean()) {
            placeLargeCone(chunk, baseX, baseZ, x, ceilingY + 1, z, radius, height, false);
        }
    }

    private static void placeLargeCone(Chunk chunk, int baseX, int baseZ, int centerX, int baseY, int centerZ,
                                       int radius, int height, boolean up) {
        for (int dy = 0; dy < height; dy++) {
            int y = up ? baseY + dy : baseY - dy;
            if (y <= MIN_Y || y >= MAX_Y) {
                break;
            }
            int layerRadius = Math.max(0, radius - (dy * radius / Math.max(1, height - 1)));
            for (int dx = -layerRadius; dx <= layerRadius; dx++) {
                for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    if (inside(baseX, baseZ, x, z) && dx * dx + dz * dz <= layerRadius * layerRadius
                            && (isAir(chunk, x, y, z) || isWater(chunk, x, y, z))) {
                        set(chunk, x, y, z, ModBlocks.DRIPSTONE_BLOCK.getDefaultState());
                    }
                }
            }
        }
        int tipY = up ? baseY + height : baseY - height;
        if (tipY >= MIN_Y && tipY <= MAX_Y && inside(baseX, baseZ, centerX, centerZ)
                && isAir(chunk, centerX, tipY, centerZ)) {
            set(chunk, centerX, tipY, centerZ, ModBlocks.POINTED_DRIPSTONE.getDefaultState()
                    .withProperty(PointedDripstoneBlock.VERTICAL_DIRECTION, up ? EnumFacing.UP : EnumFacing.DOWN)
                    .withProperty(PointedDripstoneBlock.THICKNESS, PointedDripstoneBlock.Thickness.TIP));
        }
    }

    /**
     * 排除地表、树冠与人造薄顶棚：官方Feature在世界生成阶段由完整地形包围；1.12的
     * Populate后处理必须显式验证至少8格天然岩层屋顶，否则EnvironmentScan会把树干
     * 等竖直方块误判为洞穴顶棚并生成浮空钟乳石。
     */
    private static boolean hasSubterraneanDripstoneRoof(Chunk chunk, int x, int ceilingAirY, int z) {
        int roofY = ceilingAirY + 1;
        for (int depth = 0; depth < 8; depth++) {
            if (!isNaturalDripstoneBase(chunk, x, roofY + depth, z)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNaturalDripstoneBase(Chunk chunk, int x, int y, int z) {
        if (y < MIN_Y || y > MAX_Y) return false;
        Block block = state(chunk, x, y, z).getBlock();
        /* 对应1.18的DRIPSTONE_REPLACEABLE/BASE_STONE思路：滴水石簇依附在岩层，
         * 不把泥土、草、砂、砂岩、砂砾或陶瓦表层误当作滴水石基础。沙漠地下仍会在
         * 暴露的stone/deepslate/calcite/tuff中生成，不由地表生物群系直接禁用。 */
        return block == Blocks.STONE || block == ModBlocks.DeepSlate
                || block == ModBlocks.DRIPSTONE_BLOCK || block == ModBlocks.CALCITE
                || block == ModBlocks.TUFF;
    }

    private static void placeDripstoneColumn(Chunk chunk, int x, int supportY, int z, EnumFacing direction, int length) {
        for (int i = 0; i < length; i++) {
            int y = direction == EnumFacing.UP ? supportY + i : supportY - i;
            if (y < MIN_Y || y > MAX_Y || !isAir(chunk, x, y, z)) {
                break;
            }
            /* 对应官方DripstoneUtils.buildBaseToTipColumn：BASE -> MIDDLE* -> FRUSTUM -> TIP。 */
            PointedDripstoneBlock.Thickness thickness;
            if (length <= 1 || i == length - 1) {
                thickness = PointedDripstoneBlock.Thickness.TIP;
            } else if (i == 0) {
                thickness = PointedDripstoneBlock.Thickness.BASE;
            } else if (i == length - 2) {
                thickness = PointedDripstoneBlock.Thickness.FRUSTUM;
            } else {
                thickness = PointedDripstoneBlock.Thickness.MIDDLE;
            }
            set(chunk, x, y, z, ModBlocks.POINTED_DRIPSTONE.getDefaultState()
                    .withProperty(PointedDripstoneBlock.VERTICAL_DIRECTION, direction)
                    .withProperty(PointedDripstoneBlock.THICKNESS, thickness));
        }
    }



    /**
     * HeightRangePlacement.uniform(bottom, absolute(256))在1.18.2中的采样域为router Y=-64..256。
     * 本项目采用primerY=routerY+64的1:1映射；映射到256以上的源高度在1.12世界中不存在，
     * 因而按EnvironmentScan的空结果安全丢弃，绝不能clamp到255并人为堆积顶层特征。
     */
    private static int randomLushPlacementY(Random random) {
        int primerY = random.nextInt(321); // (-64..256) + 64 -> 0..320
        return primerY <= MAX_Y ? primerY : -1;
    }

    private static int randomY(Random random) {
        return 4 + random.nextInt(248);
    }

    /**
     * 从一个确定性随机高度向下遍历整个有效高度域，查找带有30格内石质顶棚的洞穴地板。
     * 这会跳过露天表面，却不会因随机起点离洞壁超过12格而漏掉整个繁茂洞穴。
     */
    private static int findUndergroundFloorAir(Chunk chunk, int x, int startY, int z) {
        for (int y = clampY(startY); y >= 2; y--) {
            if (isAir(chunk, x, y, z) && isSolid(chunk, x, y - 1, z)
                    && findCeilingAir(null, chunk, x, y + 1, z, 30) >= 0) {
                return y;
            }
        }
        return -1;
    }

    /**
     * 从一个确定性随机高度向上遍历整个有效高度域，查找带有30格内石质地板的洞穴天花板。
     * 与地板搜索成对使用，使苔藓与藤蔓只落在封闭地下空腔，不落在地表露天区域。
     */
    private static int findUndergroundCeilingAir(Chunk chunk, int x, int startY, int z) {
        for (int y = clampY(startY); y <= MAX_Y - 2; y++) {
            if (isAir(chunk, x, y, z) && isSolid(chunk, x, y + 1, z)
                    && findFloorAir(null, chunk, x, y - 1, z, 30) >= 0) {
                return y;
            }
        }
        return -1;
    }

    private static int findFloorAir(World world, Chunk chunk, int x, int startY, int z, int range) {
        for (int y = clampY(startY); y >= Math.max(1, startY - range); y--) {
            if (isAir(chunk, x, y, z) && isSolid(chunk, x, y - 1, z)) {
                return y;
            }
        }
        return -1;
    }

    private static int findCeilingAir(World world, Chunk chunk, int x, int startY, int z, int range) {
        for (int y = clampY(startY); y <= Math.min(MAX_Y - 1, startY + range); y++) {
            if (isAir(chunk, x, y, z) && isSolid(chunk, x, y + 1, z)) {
                return y;
            }
        }
        return -1;
    }

    private static boolean isMossReplaceable(Chunk chunk, int x, int y, int z) {
        Block block = state(chunk, x, y, z).getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == Blocks.DIRT
                || block == Blocks.GRASS || block == Blocks.CLAY || block == Blocks.GRAVEL
                || block == Blocks.SAND;

    }

    private static boolean isSolid(Chunk chunk, int x, int y, int z) {
        return y >= MIN_Y && y <= MAX_Y && state(chunk, x, y, z).getMaterial().isSolid();
    }

    private static boolean isAir(Chunk chunk, int x, int y, int z) {
        return y >= MIN_Y && y <= MAX_Y && state(chunk, x, y, z).getBlock() == Blocks.AIR;
    }

    private static boolean isWater(Chunk chunk, int x, int y, int z) {
        Block block = state(chunk, x, y, z).getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    private static IBlockState state(Chunk chunk, int x, int y, int z) {
        return chunk.getBlockState(new BlockPos(x, y, z));
    }

    private static void set(Chunk chunk, int x, int y, int z, IBlockState state) {
        if (y >= MIN_Y && y <= MAX_Y) {
            chunk.setBlockState(new BlockPos(x, y, z), state);
        }
    }

    private static boolean inside(int baseX, int baseZ, int x, int z) {
        return x >= baseX && x < baseX + 16 && z >= baseZ && z < baseZ + 16;
    }

    private static int clampY(int y) {
        return Math.max(MIN_Y + 1, Math.min(MAX_Y - 1, y));
    }

    private static double normalizedDistance(int dx, int dz, int radiusX, int radiusZ) {
        double x = (double) dx / (double) radiusX;
        double z = (double) dz / (double) radiusZ;
        return Math.sqrt(x * x + z * z);
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
