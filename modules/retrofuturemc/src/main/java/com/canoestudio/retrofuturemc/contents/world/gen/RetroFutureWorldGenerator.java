package com.canoestudio.retrofuturemc.contents.world.gen;

import com.canoestudio.retrofuturemc.contents.blocks.AmethystClusterBlock;
import com.canoestudio.retrofuturemc.contents.blocks.CaveVine.CaveVine;
import com.canoestudio.retrofuturemc.contents.blocks.CaveVine.CaveVinePlant;
import com.canoestudio.retrofuturemc.contents.blocks.GlowLichenBlock;
import com.canoestudio.retrofuturemc.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturemc.contents.blocks.PointedDripstoneBlock;
import com.canoestudio.retrofuturemc.contents.blocks.dripLeaf.BigDripleaf;
import com.canoestudio.retrofuturemc.contents.blocks.dripLeaf.DripleafStem;
import com.canoestudio.retrofuturemc.contents.blocks.dripLeaf.SmallDripleaf;
import com.canoestudio.retrofuturemc.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturemc.contents.mobs.glowsquid.EntityGlowSquid;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainAPI;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainCaveBiomeType;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainCaveDecorationContext;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainConfig;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainUndergroundBiomeDecorator;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainUndergroundBiomeDefinition;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainUndergroundBiomeSample;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RetroFutureWorldGenerator implements IWorldGenerator, ModernCaveTerrainUndergroundBiomeDecorator {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
    private static final IBlockState DEEPSLATE = ModBlocks.DeepSlate.getDefaultState();
    private static final ResourceLocation LUSH_CAVES_BIOME_ID = new ResourceLocation("retrofuturemc", "lush_caves");
    private static final ResourceLocation DRIPSTONE_CAVES_BIOME_ID = new ResourceLocation("retrofuturemc", "dripstone_caves");
    private static final int GEODE_RARITY = 24;
    private static final int GEODE_MIN_Y = 15;
    private static final int GEODE_MAX_Y = 35;
    private static final int GEODE_MIN_OFFSET = -16;
    private static final int GEODE_MAX_OFFSET = 16;
    private static final int GEODE_MIN_OUTER_WALL_DISTANCE = 4;
    private static final int GEODE_MAX_OUTER_WALL_DISTANCE = 6;
    private static final int GEODE_MIN_DISTRIBUTION_POINTS = 3;
    private static final int GEODE_MAX_DISTRIBUTION_POINTS = 4;
    private static final int GEODE_MIN_POINT_OFFSET = 1;
    private static final int GEODE_MAX_POINT_OFFSET = 2;
    private static final int GEODE_CRACK_POINT_OFFSET = 2;
    private static final int GEODE_INVALID_BLOCKS_THRESHOLD = 1;
    private static final double GEODE_FILLING = 1.7D;
    private static final double GEODE_INNER_LAYER = 2.2D;
    private static final double GEODE_MIDDLE_LAYER = 3.2D;
    private static final double GEODE_OUTER_LAYER = 4.2D;
    private static final double GEODE_CRACK_CHANCE = 0.95D;
    private static final double GEODE_BASE_CRACK_SIZE = 2.0D;
    private static final double GEODE_NOISE_MULTIPLIER = 0.05D;
    private static final double GEODE_BUDDING_AMETHYST_CHANCE = 0.083D;
    private static final double GEODE_CRYSTAL_PLACEMENT_CHANCE = 0.35D;
    public static final int WORLD_CAVE_DECORATION_ATTEMPTS = 72;
    public static final int WORLD_CAVE_AIR_SCAN_DISTANCE = 20;
    public static final int PRIMER_CAVE_AIR_SCAN_DISTANCE = 24;
    private static final int LUSH_SURFACE_SCAN_DISTANCE = 16;
    public static final int MODERN_CAVE_TERRAIN_DECORATION_ATTEMPTS = 128;
    private static final int MODERN_CAVE_TERRAIN_COLUMN_STEP = 4;
    private static final int MODERN_CAVE_TERRAIN_VERTICAL_STEP = 3;
    private static final long WORLD_CAVE_POSITION_SEED_SALT = 0x5246574341564550L;
    private static final long WORLD_CAVE_FEATURE_SEED_SALT = 0x5246574341564546L;
    private static final long MODERN_CAVE_POSITION_SEED_SALT = 0x4D435442494F4D45L;
    private static final long MODERN_CAVE_FEATURE_SEED_SALT = 0x52464D4346454154L;
    private static final long MODERN_LUSH_POSITION_SEED_SALT = 0x52464C555348504FL;
    private static final long MODERN_LUSH_FEATURE_SEED_SALT = 0x52464C5553484645L;
    private static final long MODERN_DRIPSTONE_POSITION_SEED_SALT = 0x524644524950504FL;
    private static final long MODERN_DRIPSTONE_FEATURE_SEED_SALT = 0x5246445249504645L;
    private static final int MODERN_UNDERGROUND_BIOME_MIN_Y = 4;
    private static final int MODERN_UNDERGROUND_BIOME_MAX_Y = 128;
    private static final int MODERN_UNDERGROUND_BIOME_PRIORITY = 80;
    private static final double MODERN_UNDERGROUND_MIN_DEPTH = 0.2D;
    private static final double MODERN_UNDERGROUND_MAX_DEPTH = 0.9D;
    private static final double MODERN_LUSH_SUPPORT_HUMIDITY = 0.30D;
    private static final double MODERN_LUSH_TARGET_HUMIDITY = 0.70D;
    private static final double MODERN_DRIPSTONE_SUPPORT_CONTINENTALNESS = 0.35D;
    private static final double MODERN_DRIPSTONE_TARGET_CONTINENTALNESS = 0.80D;
    private static final double MODERN_SELECTOR_MIN_WEIGHT = 0.06D;
    private static final long MODERN_UNDERGROUND_REGION_SEED_SALT = 0x5246554E44474249L;
    private static final boolean GENERATE_LUSH_CAVES = true;
    private static final boolean GENERATE_DRIPSTONE_CAVES = true;
    private static boolean modernCaveTerrainBiomesRegistered;

    public static void registerModernCaveTerrainBiomes() {
        if (!modernCaveTerrainBiomesRegistered) {
            RetroFutureWorldGenerator decorator = new RetroFutureWorldGenerator();
            if (GENERATE_LUSH_CAVES) {
                ModernCaveTerrainAPI.registerUndergroundBiome(createLushCavesDefinition(decorator));
            }
            if (GENERATE_DRIPSTONE_CAVES) {
                ModernCaveTerrainAPI.registerUndergroundBiome(createDripstoneCavesDefinition(decorator));
            }
            modernCaveTerrainBiomesRegistered = true;
        }
    }

    private static ModernCaveTerrainUndergroundBiomeDefinition createLushCavesDefinition(
            ModernCaveTerrainUndergroundBiomeDecorator decorator) {
        return ModernCaveTerrainUndergroundBiomeDefinition
                .builder(LUSH_CAVES_BIOME_ID)
                .caveBiomeType(ModernCaveTerrainCaveBiomeType.GENERIC_3D)
                .yRange(MODERN_UNDERGROUND_BIOME_MIN_Y, MODERN_UNDERGROUND_BIOME_MAX_Y)
                .priority(MODERN_UNDERGROUND_BIOME_PRIORITY)
                .selector((world, sample) -> getModernLushSelectionWeight(world, sample))
                .decorator(decorator)
                .build();
    }

    private static ModernCaveTerrainUndergroundBiomeDefinition createDripstoneCavesDefinition(
            ModernCaveTerrainUndergroundBiomeDecorator decorator) {
        return ModernCaveTerrainUndergroundBiomeDefinition
                .builder(DRIPSTONE_CAVES_BIOME_ID)
                .caveBiomeType(ModernCaveTerrainCaveBiomeType.GENERIC_3D)
                .yRange(MODERN_UNDERGROUND_BIOME_MIN_Y, MODERN_UNDERGROUND_BIOME_MAX_Y)
                .priority(MODERN_UNDERGROUND_BIOME_PRIORITY)
                .selector((world, sample) -> getModernDripstoneSelectionWeight(world, sample))
                .decorator(decorator)
                .build();
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        generateOres(world, random, blockX, blockZ);

        if (shouldGenerateAmethystGeode(world, random, blockX, blockZ)) {
            generateAmethystGeode(world, random, new BlockPos(
                    blockX + random.nextInt(16),
                    randomRange(random, GEODE_MIN_Y, GEODE_MAX_Y),
                    blockZ + random.nextInt(16)));
        }

        if (!modernCaveTerrainBiomesRegistered || !isModernCaveTerrainActive(world)) {
            decorateCavesInWorld(world, createWorldCavePositionRandom(world, chunkX, chunkZ),
                    createWorldCaveFeatureRandom(world, chunkX, chunkZ), blockX, blockZ);
        }
        spawnAquaticCaveMobs(world, random, blockX, blockZ);
    }

    @Override
    public void decorate(ModernCaveTerrainCaveDecorationContext context, ModernCaveTerrainUndergroundBiomeDefinition definition) {
        World world = context.getWorld();
        CaveStyle targetStyle = getModernDefinitionStyle(definition);
        if (targetStyle == CaveStyle.NORMAL) {
            return;
        }
        if (!context.getConfig().isMojang118StyleCavesEnabled()) {
            return;
        }

        Random positionRandom = createModernCavePositionRandom(world, context.getChunkX(), context.getChunkZ(), getModernPositionSalt(targetStyle));
        Random decorationRandom = createModernCaveFeatureRandom(world, context.getChunkX(), context.getChunkZ(), getModernFeatureSalt(targetStyle));
        int minY = Math.max(getModernCaveMinY(world, context.getConfig()), definition.getMinY());
        int maxY = Math.min(getModernCaveMaxY(world, context.getConfig()), definition.getMaxY());
        if (maxY < minY) {
            maxY = Math.min(definition.getMaxY(), Math.min(96, world.getActualHeight() - 1));
            minY = Math.max(definition.getMinY(), 4);
            if (maxY < minY) {
                return;
            }
        }

        decorateModernBiomeGrid(context, definition, targetStyle, decorationRandom, minY, maxY);

        for (int i = 0; i < MODERN_CAVE_TERRAIN_DECORATION_ATTEMPTS; i++) {
            int x = positionRandom.nextInt(16);
            int z = positionRandom.nextInt(16);
            int y = minY + positionRandom.nextInt(Math.max(1, maxY - minY + 1));
            y = findPrimerCaveAir(context, x, y, z, PRIMER_CAVE_AIR_SCAN_DISTANCE);
            if (y < 0) {
                continue;
            }

            tryDecorateModernPrimer(context, definition, targetStyle, decorationRandom, x, y, z);
        }
    }

    private void decorateModernBiomeGrid(ModernCaveTerrainCaveDecorationContext context,
                                         ModernCaveTerrainUndergroundBiomeDefinition definition,
                                         CaveStyle targetStyle, Random random, int minY, int maxY) {
        int maxPlacements = targetStyle == CaveStyle.LUSH ? 20 : 24;
        int placements = 0;
        int yOffset = random.nextInt(MODERN_CAVE_TERRAIN_VERTICAL_STEP);

        for (int xBase = 0; xBase < 16 && placements < maxPlacements; xBase += MODERN_CAVE_TERRAIN_COLUMN_STEP) {
            for (int zBase = 0; zBase < 16 && placements < maxPlacements; zBase += MODERN_CAVE_TERRAIN_COLUMN_STEP) {
                int x = Math.min(15, xBase + 1 + random.nextInt(Math.max(1, MODERN_CAVE_TERRAIN_COLUMN_STEP - 1)));
                int z = Math.min(15, zBase + 1 + random.nextInt(Math.max(1, MODERN_CAVE_TERRAIN_COLUMN_STEP - 1)));

                for (int y = minY + yOffset; y <= maxY && placements < maxPlacements; y += MODERN_CAVE_TERRAIN_VERTICAL_STEP) {
                    if (!isAir(context.getBlockState(x, y, z))) {
                        continue;
                    }
                    ModernCaveTerrainUndergroundBiomeSample sample = context.sampleUndergroundBiomeLocal(x, y, z);
                    if (!matchesModernDefinition(definition, sample)) {
                        continue;
                    }
                    double strength = getModernCaveStyleStrength(sample, targetStyle);
                    if (random.nextDouble() > 0.30D + strength * 0.42D) {
                        continue;
                    }
                    if (tryDecorateModernPrimer(context, definition, targetStyle, random, x, y, z)) {
                        placements++;
                    }
                    break;
                }
            }
        }
    }

    private boolean tryDecorateModernPrimer(ModernCaveTerrainCaveDecorationContext context,
                                            ModernCaveTerrainUndergroundBiomeDefinition definition,
                                            CaveStyle targetStyle, Random random, int x, int y, int z) {
        ModernCaveTerrainUndergroundBiomeSample biomeSample = context.sampleUndergroundBiomeLocal(x, y, z);
        if (!matchesModernDefinition(definition, biomeSample)) {
            return false;
        }

        double strength = getModernCaveStyleStrength(biomeSample, targetStyle);
        if (strength <= 0.0D) {
            return false;
        }
        if (targetStyle == CaveStyle.LUSH) {
            decorateLushPrimer(context, random, x, y, z, strength);
            return true;
        }
        if (targetStyle == CaveStyle.DRIPSTONE) {
            decorateDripstonePrimer(context, random, x, y, z, strength);
            return true;
        }
        return false;
    }

    public static boolean isModernCaveTerrainActive(World world) {
        int dimension = world.provider.getDimension();
        return ModernCaveTerrainAPI.canGenerateInDimension(dimension)
                && ModernCaveTerrainAPI.getConfigForDimension(dimension).isMojang118StyleCavesEnabled();
    }

    public static int getModernCaveMinY(World world, ModernCaveTerrainConfig config) {
        return Math.max(4, config.getMojang118StyleCaveBottom());
    }

    public static int getModernCaveMaxY(World world, ModernCaveTerrainConfig config) {
        return Math.min(world.getActualHeight() - 1, config.getMojang118StyleCaveTop());
    }

    public static Random createModernCavePositionRandom(World world, int chunkX, int chunkZ) {
        return new Random(getModernCaveSeed(world, chunkX, chunkZ, MODERN_CAVE_POSITION_SEED_SALT));
    }

    public static Random createModernCavePositionRandom(World world, int chunkX, int chunkZ, long salt) {
        return new Random(getModernCaveSeed(world, chunkX, chunkZ, salt));
    }

    public static Random createWorldCavePositionRandom(World world, int chunkX, int chunkZ) {
        return new Random(getWorldCaveSeed(world, chunkX, chunkZ, WORLD_CAVE_POSITION_SEED_SALT));
    }

    private static Random createWorldCaveFeatureRandom(World world, int chunkX, int chunkZ) {
        return new Random(getWorldCaveSeed(world, chunkX, chunkZ, WORLD_CAVE_FEATURE_SEED_SALT));
    }

    private static Random createModernCaveFeatureRandom(World world, int chunkX, int chunkZ) {
        return new Random(getModernCaveSeed(world, chunkX, chunkZ, MODERN_CAVE_FEATURE_SEED_SALT));
    }

    private static Random createModernCaveFeatureRandom(World world, int chunkX, int chunkZ, long salt) {
        return new Random(getModernCaveSeed(world, chunkX, chunkZ, salt));
    }

    private static long getModernCaveSeed(World world, int chunkX, int chunkZ, long salt) {
        return world.getSeed() ^ (long)chunkX * 341873128712L ^ (long)chunkZ * 132897987541L ^ salt;
    }

    private static long getWorldCaveSeed(World world, int chunkX, int chunkZ, long salt) {
        return world.getSeed() ^ (long)chunkX * 132897987541L ^ (long)chunkZ * 341873128712L ^ salt;
    }

    private static long getModernPositionSalt(CaveStyle style) {
        if (style == CaveStyle.LUSH) {
            return MODERN_LUSH_POSITION_SEED_SALT;
        }
        if (style == CaveStyle.DRIPSTONE) {
            return MODERN_DRIPSTONE_POSITION_SEED_SALT;
        }
        return MODERN_CAVE_POSITION_SEED_SALT;
    }

    private static long getModernFeatureSalt(CaveStyle style) {
        if (style == CaveStyle.LUSH) {
            return MODERN_LUSH_FEATURE_SEED_SALT;
        }
        if (style == CaveStyle.DRIPSTONE) {
            return MODERN_DRIPSTONE_FEATURE_SEED_SALT;
        }
        return MODERN_CAVE_FEATURE_SEED_SALT;
    }

    private static CaveStyle getModernDefinitionStyle(ModernCaveTerrainUndergroundBiomeDefinition definition) {
        if (definition == null) {
            return CaveStyle.NORMAL;
        }
        if (LUSH_CAVES_BIOME_ID.equals(definition.getId())) {
            return CaveStyle.LUSH;
        }
        if (DRIPSTONE_CAVES_BIOME_ID.equals(definition.getId())) {
            return CaveStyle.DRIPSTONE;
        }
        return CaveStyle.NORMAL;
    }

    private static boolean matchesModernDefinition(ModernCaveTerrainUndergroundBiomeDefinition definition,
                                                   ModernCaveTerrainUndergroundBiomeSample sample) {
        return definition != null && sample != null && definition.getId().equals(sample.getBiomeId());
    }

    public static CaveStyle classifyModernCaveStyle(ModernCaveTerrainUndergroundBiomeSample sample) {
        if (sample == null) {
            return CaveStyle.NORMAL;
        }
        if (LUSH_CAVES_BIOME_ID.equals(sample.getBiomeId())) {
            return CaveStyle.LUSH;
        }
        if (DRIPSTONE_CAVES_BIOME_ID.equals(sample.getBiomeId())) {
            return CaveStyle.DRIPSTONE;
        }
        if (sample.getCaveBiomeType() != ModernCaveTerrainCaveBiomeType.GENERIC_3D) {
            return CaveStyle.NORMAL;
        }
        if (sample.getDepth() < MODERN_UNDERGROUND_MIN_DEPTH || sample.getDepth() > MODERN_UNDERGROUND_MAX_DEPTH) {
            return CaveStyle.NORMAL;
        }

        double lushScore = getModernLushSelectionWeight(null, sample);
        double dripstoneScore = getModernDripstoneSelectionWeight(null, sample);
        if (lushScore <= 0.0D && dripstoneScore <= 0.0D) {
            return CaveStyle.NORMAL;
        }
        if (lushScore > 0.0D && lushScore >= dripstoneScore) {
            return CaveStyle.LUSH;
        }
        return CaveStyle.DRIPSTONE;
    }

    public static double getModernCaveStyleStrength(ModernCaveTerrainUndergroundBiomeSample sample, CaveStyle style) {
        if (style == CaveStyle.LUSH) {
            return getModernLushDecorationStrength(sample);
        }
        if (style == CaveStyle.DRIPSTONE) {
            return getModernDripstoneDecorationStrength(sample);
        }
        return 0.0D;
    }

    private static double getModernLushSelectionWeight(World world, ModernCaveTerrainUndergroundBiomeSample sample) {
        if (!canSelectModernUndergroundBiome(sample)) {
            return 0.0D;
        }

        double depth = getDepthBandStrength(sample);
        double humidity = getModernLushScore(sample);
        double highHumidity = clampedMap(sample.getHumidity(), MODERN_LUSH_TARGET_HUMIDITY, 1.0D, 0.0D, 1.0D);
        double region = getModernRegionStrength(world, sample, CaveStyle.LUSH);
        double surface = getLushSurfaceBias(sample.getSurfaceBiome());
        double caveShape = getEdgeStrength(sample);
        double competition = clampedMap(getModernDripstoneScore(sample), 0.45D, 1.0D, 1.0D, 0.68D);
        double climate = humidity * 0.68D + highHumidity * 0.22D + getWetSurfaceSupport(sample.getSurfaceBiome()) * 0.10D;
        double weight = depth * caveShape * region * surface * competition * climate;
        return weight >= MODERN_SELECTOR_MIN_WEIGHT ? clamp(weight, 0.0D, 1.0D) : 0.0D;
    }

    private static double getModernDripstoneSelectionWeight(World world, ModernCaveTerrainUndergroundBiomeSample sample) {
        if (!canSelectModernUndergroundBiome(sample)) {
            return 0.0D;
        }

        double depth = getDepthBandStrength(sample);
        double continentalness = getModernDripstoneScore(sample);
        double highContinentalness = clampedMap(sample.getContinentalness(), MODERN_DRIPSTONE_TARGET_CONTINENTALNESS, 1.0D, 0.0D, 1.0D);
        double region = getModernRegionStrength(world, sample, CaveStyle.DRIPSTONE);
        double surface = getDripstoneSurfaceBias(sample.getSurfaceBiome());
        double caveShape = getEdgeStrength(sample);
        double competition = clampedMap(getModernLushScore(sample), 0.52D, 1.0D, 1.0D, 0.66D);
        double climate = continentalness * 0.70D + highContinentalness * 0.22D + getDryStoneSurfaceSupport(sample.getSurfaceBiome()) * 0.08D;
        double weight = depth * caveShape * region * surface * competition * climate;
        return weight >= MODERN_SELECTOR_MIN_WEIGHT ? clamp(weight, 0.0D, 1.0D) : 0.0D;
    }

    private static boolean canSelectModernUndergroundBiome(ModernCaveTerrainUndergroundBiomeSample sample) {
        return sample != null
                && sample.getCaveBiomeType() == ModernCaveTerrainCaveBiomeType.GENERIC_3D
                && sample.getDepth() >= MODERN_UNDERGROUND_MIN_DEPTH
                && sample.getDepth() <= MODERN_UNDERGROUND_MAX_DEPTH;
    }

    private static double getModernLushScore(ModernCaveTerrainUndergroundBiomeSample sample) {
        return clampedMap(sample.getHumidity(), MODERN_LUSH_SUPPORT_HUMIDITY, MODERN_LUSH_TARGET_HUMIDITY, 0.0D, 1.0D);
    }

    private static double getModernDripstoneScore(ModernCaveTerrainUndergroundBiomeSample sample) {
        return clampedMap(sample.getContinentalness(), MODERN_DRIPSTONE_SUPPORT_CONTINENTALNESS, MODERN_DRIPSTONE_TARGET_CONTINENTALNESS, 0.0D, 1.0D);
    }

    private static double getModernLushDecorationStrength(ModernCaveTerrainUndergroundBiomeSample sample) {
        if (!canSelectModernUndergroundBiome(sample)) {
            return 0.0D;
        }

        double humidity = clampedMap(sample.getHumidity(), 0.05D, MODERN_LUSH_TARGET_HUMIDITY, 0.25D, 1.0D);
        return clamp(humidity * getDepthBandStrength(sample) * getEdgeStrength(sample) * getLushSurfaceBias(sample.getSurfaceBiome()),
                0.12D, 1.0D);
    }

    private static double getModernDripstoneDecorationStrength(ModernCaveTerrainUndergroundBiomeSample sample) {
        if (!canSelectModernUndergroundBiome(sample)) {
            return 0.0D;
        }

        double continentalness = clampedMap(sample.getContinentalness(), 0.10D, MODERN_DRIPSTONE_TARGET_CONTINENTALNESS, 0.25D, 1.0D);
        return clamp(continentalness * getDepthBandStrength(sample) * getEdgeStrength(sample) * getDripstoneSurfaceBias(sample.getSurfaceBiome()),
                0.12D, 1.0D);
    }

    private static double getModernRegionStrength(World world, ModernCaveTerrainUndergroundBiomeSample sample, CaveStyle style) {
        double region = getModernUndergroundRegion(world, sample);
        if (style == CaveStyle.LUSH) {
            double humidityPull = clampedMap(sample.getHumidity(), -0.10D, MODERN_LUSH_TARGET_HUMIDITY, -0.12D, 0.24D);
            return clampedMap(region + humidityPull, -0.28D, 0.58D, 0.18D, 1.0D);
        }
        if (style == CaveStyle.DRIPSTONE) {
            double stonePull = clampedMap(sample.getContinentalness(), 0.00D, MODERN_DRIPSTONE_TARGET_CONTINENTALNESS, -0.10D, 0.24D);
            return clampedMap(-region + stonePull, -0.30D, 0.58D, 0.18D, 1.0D);
        }
        return 0.0D;
    }

    private static double getModernUndergroundRegion(World world, ModernCaveTerrainUndergroundBiomeSample sample) {
        long seed = (world != null ? world.getSeed() : 0L) ^ MODERN_UNDERGROUND_REGION_SEED_SALT;
        double x = sample.getBlockX();
        double y = sample.getBlockY();
        double z = sample.getBlockZ();
        double broad = smoothNoise(seed, x * 0.0045D, 0.0D, z * 0.0045D);
        double middle = smoothNoise(seed ^ 0x4D435442494F4D32L, x * 0.010D, y * 0.011D, z * 0.010D);
        double detail = smoothNoise(seed ^ 0x4D435442494F4D33L, x * 0.024D, y * 0.018D, z * 0.024D);
        return clamp(broad * 0.62D + middle * 0.30D + detail * 0.08D, -1.0D, 1.0D);
    }

    private static double getDepthBandStrength(ModernCaveTerrainUndergroundBiomeSample sample) {
        double lower = clampedMap(sample.getDepth(), MODERN_UNDERGROUND_MIN_DEPTH, MODERN_UNDERGROUND_MIN_DEPTH + 0.12D, 0.0D, 1.0D);
        double upper = clampedMap(sample.getDepth(), MODERN_UNDERGROUND_MAX_DEPTH, MODERN_UNDERGROUND_MAX_DEPTH - 0.12D, 0.0D, 1.0D);
        return Math.min(lower, upper);
    }

    private static double getEdgeStrength(ModernCaveTerrainUndergroundBiomeSample sample) {
        return 0.35D + sample.getEdgeFactor() * 0.65D;
    }

    private static double getLushSurfaceBias(Biome biome) {
        double bias = 1.0D;
        if (hasBiomeType(biome, BiomeDictionary.Type.WET)
                || hasBiomeType(biome, BiomeDictionary.Type.JUNGLE)
                || hasBiomeType(biome, BiomeDictionary.Type.SWAMP)) {
            bias += 0.18D;
        }
        if (hasBiomeType(biome, BiomeDictionary.Type.DRY)
                || hasBiomeType(biome, BiomeDictionary.Type.MESA)
                || hasBiomeType(biome, BiomeDictionary.Type.SANDY)) {
            bias -= 0.18D;
        }
        if (hasBiomeType(biome, BiomeDictionary.Type.OCEAN)) {
            bias -= 0.22D;
        }
        return clamp(bias, 0.68D, 1.22D);
    }

    private static double getDripstoneSurfaceBias(Biome biome) {
        double bias = 1.0D;
        if (hasBiomeType(biome, BiomeDictionary.Type.DRY)
                || hasBiomeType(biome, BiomeDictionary.Type.MESA)
                || hasBiomeType(biome, BiomeDictionary.Type.SANDY)
                || hasBiomeType(biome, BiomeDictionary.Type.MOUNTAIN)
                || hasBiomeType(biome, BiomeDictionary.Type.HILLS)) {
            bias += 0.16D;
        }
        if (hasBiomeType(biome, BiomeDictionary.Type.WET)
                || hasBiomeType(biome, BiomeDictionary.Type.JUNGLE)
                || hasBiomeType(biome, BiomeDictionary.Type.SWAMP)
                || hasBiomeType(biome, BiomeDictionary.Type.OCEAN)) {
            bias -= 0.16D;
        }
        return clamp(bias, 0.7D, 1.2D);
    }

    private static double getWetSurfaceSupport(Biome biome) {
        return getLushSurfaceBias(biome) > 1.0D ? 1.0D : 0.0D;
    }

    private static double getDryStoneSurfaceSupport(Biome biome) {
        return getDripstoneSurfaceBias(biome) > 1.0D ? 1.0D : 0.0D;
    }

    private static boolean hasBiomeType(Biome biome, BiomeDictionary.Type type) {
        return biome != null && BiomeDictionary.hasType(biome, type);
    }

    private void generateOres(World world, Random random, int blockX, int blockZ) {
        generateOre(world, random, ModBlocks.COPPER_ORE.getDefaultState(), 10, 16, 0, 64, blockX, blockZ, STONE);
        generateOre(world, random, ModBlocks.DEEPSLATE_COPPER_ORE.getDefaultState(), 6, 10, 0, 28, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_IRON_ORE.getDefaultState(), 6, 8, 0, 24, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_GOLD_ORE.getDefaultState(), 2, 8, 0, 24, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_REDSTONE_ORE.getDefaultState(), 8, 7, 0, 16, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_LAPIS_ORE.getDefaultState(), 1, 6, 0, 24, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_DIAMOND_ORE.getDefaultState(), 1, 7, 0, 18, blockX, blockZ, DEEPSLATE);
        generateOre(world, random, ModBlocks.DEEPSLATE_EMERALD_ORE.getDefaultState(), 1, 3, 4, 28, blockX, blockZ, DEEPSLATE);

        if (random.nextInt(4) == 0) {
            replaceStonePatch(world, random, blockX + random.nextInt(16), 6 + random.nextInt(24), blockZ + random.nextInt(16), ModBlocks.TUFF.getDefaultState(), 24);
        }
    }

    private void generateOre(World world, Random random, IBlockState ore, int count, int size, int minY, int maxY, int blockX, int blockZ, IBlockState target) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = new BlockPos(blockX + random.nextInt(16), minY + random.nextInt(Math.max(1, maxY - minY)), blockZ + random.nextInt(16));
            new WorldGenMinable(ore, size, state -> state != null && state.getBlock() == target.getBlock()).generate(world, random, pos);
        }
    }

    private void replaceStonePatch(World world, Random random, int x, int y, int z, IBlockState state, int radius) {
        BlockPos center = new BlockPos(x, y, z);
        for (int i = 0; i < radius; i++) {
            BlockPos pos = center.add(random.nextInt(9) - 4, random.nextInt(7) - 3, random.nextInt(9) - 4);
            if (isNaturalStone(world.getBlockState(pos))) {
                world.setBlockState(pos, state, 2);
            }
        }
    }

    private boolean shouldGenerateAmethystGeode(World world, Random random, int blockX, int blockZ) {
        if (random.nextInt(GEODE_RARITY) != 0) {
            return false;
        }

        Biome biome = world.getBiome(new BlockPos(blockX + 8, 0, blockZ + 8));
        return !BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
    }

    private boolean generateAmethystGeode(World world, Random random, BlockPos origin) {
        int pointCount = randomRange(random, GEODE_MIN_DISTRIBUTION_POINTS, GEODE_MAX_DISTRIBUTION_POINTS);
        List<GeodePoint> shellPoints = new ArrayList<>();
        int invalidPoints = 0;

        for (int i = 0; i < pointCount; i++) {
            BlockPos point = origin.add(
                    randomRange(random, GEODE_MIN_OUTER_WALL_DISTANCE, GEODE_MAX_OUTER_WALL_DISTANCE),
                    randomRange(random, GEODE_MIN_OUTER_WALL_DISTANCE, GEODE_MAX_OUTER_WALL_DISTANCE),
                    randomRange(random, GEODE_MIN_OUTER_WALL_DISTANCE, GEODE_MAX_OUTER_WALL_DISTANCE));

            if (isInvalidGeodeSample(world.getBlockState(point))) {
                invalidPoints++;
                if (invalidPoints > GEODE_INVALID_BLOCKS_THRESHOLD) {
                    return false;
                }
            }

            shellPoints.add(new GeodePoint(point, randomRange(random, GEODE_MIN_POINT_OFFSET, GEODE_MAX_POINT_OFFSET)));
        }

        List<BlockPos> crackPoints = new ArrayList<>();
        boolean shouldGenerateCrack = random.nextDouble() < GEODE_CRACK_CHANCE;
        if (shouldGenerateCrack) {
            addGeodeCrackPoints(random, origin, pointCount, crackPoints);
        }

        double crackSizeAdjustment = (double)pointCount / (double)GEODE_MAX_OUTER_WALL_DISTANCE;
        double innerAir = inverseSqrt(GEODE_FILLING);
        double innermostBlockLayer = inverseSqrt(GEODE_INNER_LAYER + crackSizeAdjustment);
        double innerCrust = inverseSqrt(GEODE_MIDDLE_LAYER + crackSizeAdjustment);
        double outerCrust = inverseSqrt(GEODE_OUTER_LAYER + crackSizeAdjustment);
        double crackSize = inverseSqrt(GEODE_BASE_CRACK_SIZE + random.nextDouble() / 2.0D + (pointCount > 3 ? crackSizeAdjustment : 0.0D));
        List<BlockPos> potentialCrystalPlacements = new ArrayList<>();

        for (int x = GEODE_MIN_OFFSET; x <= GEODE_MAX_OFFSET; x++) {
            for (int y = GEODE_MIN_OFFSET; y <= GEODE_MAX_OFFSET; y++) {
                for (int z = GEODE_MIN_OFFSET; z <= GEODE_MAX_OFFSET; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    double noiseOffset = smoothNoise(world.getSeed() ^ 0x47454F44454CL, pos.getX() * 0.12D, pos.getY() * 0.12D, pos.getZ() * 0.12D) * GEODE_NOISE_MULTIPLIER;
                    double shellDistance = 0.0D;
                    double crackDistance = 0.0D;

                    for (GeodePoint point : shellPoints) {
                        shellDistance += inverseSqrt(distanceSq(pos, point.pos) + point.offset) + noiseOffset;
                    }

                    for (BlockPos crackPoint : crackPoints) {
                        crackDistance += inverseSqrt(distanceSq(pos, crackPoint) + GEODE_CRACK_POINT_OFFSET) + noiseOffset;
                    }

                    if (shellDistance < outerCrust) {
                        continue;
                    }

                    if (shouldGenerateCrack && crackDistance >= crackSize && shellDistance < innerAir) {
                        if (setGeodeBlock(world, pos, Blocks.AIR.getDefaultState(), 2)) {
                            notifyAdjacentFluids(world, pos);
                        }
                    } else if (shellDistance >= innerAir) {
                        setGeodeBlock(world, pos, Blocks.AIR.getDefaultState(), 2);
                    } else if (shellDistance >= innermostBlockLayer) {
                        boolean budding = random.nextDouble() < GEODE_BUDDING_AMETHYST_CHANCE;
                        if (setGeodeBlock(world, pos, budding ? ModBlocks.BUDDING_AMETHYST.getDefaultState() : ModBlocks.AMETHYST_BLOCK.getDefaultState(), 2)
                                && budding && random.nextDouble() < GEODE_CRYSTAL_PLACEMENT_CHANCE) {
                            potentialCrystalPlacements.add(pos);
                        }
                    } else if (shellDistance >= innerCrust) {
                        setGeodeBlock(world, pos, ModBlocks.CALCITE.getDefaultState(), 2);
                    } else {
                        setGeodeBlock(world, pos, getGeodeOuterLayerState(world, pos), 2);
                    }
                }
            }
        }

        for (BlockPos crystalPos : potentialCrystalPlacements) {
            placeGeodeCrystal(world, random, crystalPos);
        }

        return true;
    }

    private void addGeodeCrackPoints(Random random, BlockPos origin, int pointCount, List<BlockPos> crackPoints) {
        int crackOffset = pointCount * 2 + 1;
        int side = random.nextInt(4);

        if (side == 0) {
            crackPoints.add(origin.add(crackOffset, 7, 0));
            crackPoints.add(origin.add(crackOffset, 5, 0));
            crackPoints.add(origin.add(crackOffset, 1, 0));
        } else if (side == 1) {
            crackPoints.add(origin.add(0, 7, crackOffset));
            crackPoints.add(origin.add(0, 5, crackOffset));
            crackPoints.add(origin.add(0, 1, crackOffset));
        } else if (side == 2) {
            crackPoints.add(origin.add(crackOffset, 7, crackOffset));
            crackPoints.add(origin.add(crackOffset, 5, crackOffset));
            crackPoints.add(origin.add(crackOffset, 1, crackOffset));
        } else {
            crackPoints.add(origin.add(0, 7, 0));
            crackPoints.add(origin.add(0, 5, 0));
            crackPoints.add(origin.add(0, 1, 0));
        }
    }

    private IBlockState getGeodeOuterLayerState(World world, BlockPos pos) {
        double texture = smoothNoise(world.getSeed() ^ 0x444447454F4445L, pos.getX() * 0.35D, pos.getY() * 0.35D, pos.getZ() * 0.35D);
        return texture > -0.35D ? ModBlocks.TUFF.getDefaultState() : STONE;
    }

    private void placeGeodeCrystal(World world, Random random, BlockPos supportPos) {
        Block crystal = getRandomGeodeCrystal(random);
        int start = random.nextInt(EnumFacing.values().length);

        for (int i = 0; i < EnumFacing.values().length; i++) {
            EnumFacing facing = EnumFacing.values()[(start + i) % EnumFacing.values().length];
            BlockPos placePos = supportPos.offset(facing);
            IBlockState target = world.getBlockState(placePos);

            if (canGeodeClusterGrowAtState(world, placePos, target) && canReplaceGeodeBlock(world, placePos, target)) {
                IBlockState crystalState = crystal.getDefaultState().withProperty(AmethystClusterBlock.FACING, facing);
                FluidState fluidState = getFluidState(world, placePos, target);
                world.setBlockState(placePos, crystalState, 3);
                if (fluidState.getFluid() == FluidRegistry.WATER) {
                    FluidloggedUtils.setFluidState(world, placePos, world.getBlockState(placePos), fluidState, false, 3);
                }
                return;
            }
        }
    }

    private Block getRandomGeodeCrystal(Random random) {
        int choice = random.nextInt(4);
        if (choice == 0) {
            return ModBlocks.SMALL_AMETHYST_BUD;
        }
        if (choice == 1) {
            return ModBlocks.MEDIUM_AMETHYST_BUD;
        }
        if (choice == 2) {
            return ModBlocks.LARGE_AMETHYST_BUD;
        }
        return ModBlocks.AMETHYST_CLUSTER;
    }

    private boolean isInvalidGeodeSample(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block == Blocks.AIR
                || block == Blocks.BEDROCK
                || block == Blocks.ICE
                || block == Blocks.PACKED_ICE
                || material == Material.WATER
                || material == Material.LAVA;
    }

    private boolean setGeodeBlock(World world, BlockPos pos, IBlockState state, int flags) {
        IBlockState current = world.getBlockState(pos);
        if (!canReplaceGeodeBlock(world, pos, current)) {
            return false;
        }

        world.setBlockState(pos, state, flags);
        return true;
    }

    private boolean canReplaceGeodeBlock(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        return block != Blocks.BEDROCK
                && block != Blocks.MOB_SPAWNER
                && block != Blocks.CHEST
                && block != Blocks.TRAPPED_CHEST
                && block != Blocks.END_PORTAL_FRAME
                && block != Blocks.END_PORTAL
                && block != Blocks.PORTAL
                && block != Blocks.COMMAND_BLOCK
                && block != Blocks.CHAIN_COMMAND_BLOCK
                && block != Blocks.REPEATING_COMMAND_BLOCK
                && block != Blocks.STRUCTURE_BLOCK
                && !block.hasTileEntity(state);
    }

    private boolean canGeodeClusterGrowAtState(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == Blocks.AIR
                || state.getMaterial() == Material.WATER
                || FluidloggedUtils.getFluidState(world, pos, state).getFluid() == FluidRegistry.WATER;
    }

    private FluidState getFluidState(World world, BlockPos pos, IBlockState state) {
        return state.getMaterial() == Material.WATER ? FluidState.of(state) : FluidloggedUtils.getFluidState(world, pos, state);
    }

    private void notifyAdjacentFluids(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacent = pos.offset(facing);
            IBlockState state = world.getBlockState(adjacent);
            if (state.getMaterial().isLiquid()) {
                world.scheduleUpdate(adjacent, state.getBlock(), 0);
            }
        }
    }

    private void decorateCavesInWorld(World world, Random positionRandom, Random decorationRandom, int blockX, int blockZ) {
        for (int i = 0; i < WORLD_CAVE_DECORATION_ATTEMPTS; i++) {
            BlockPos pos = findCaveAirInColumn(world,
                    blockX + positionRandom.nextInt(16),
                    8 + positionRandom.nextInt(62),
                    blockZ + positionRandom.nextInt(16),
                    WORLD_CAVE_AIR_SCAN_DISTANCE);
            if (pos == null) {
                continue;
            }

            CaveStyle style = classifyWorldCaveStyle(world, pos);
            if (GENERATE_LUSH_CAVES && style == CaveStyle.LUSH) {
                decorateLushWorld(world, decorationRandom, pos);
            } else if (GENERATE_DRIPSTONE_CAVES && style == CaveStyle.DRIPSTONE) {
                decorateDripstoneWorld(world, decorationRandom, pos);
            } else if (decorationRandom.nextInt(4) == 0) {
                placeGlowLichenWorld(world, decorationRandom, pos);
            }
        }
    }

    public static CaveStyle classifyWorldCaveStyle(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        boolean wet = biome.getRainfall() >= 0.8F
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP);
        double region = smoothNoise(world.getSeed() ^ 0x4C555348L, pos.getX() * 0.014D, 0.0D, pos.getZ() * 0.014D);
        double detail = smoothNoise(world.getSeed() ^ 0x44524950L, pos.getX() * 0.023D, pos.getY() * 0.032D, pos.getZ() * 0.023D);
        double patch = smoothNoise(world.getSeed() ^ 0x4C55534843415645L, pos.getX() * 0.045D, pos.getY() * 0.04D, pos.getZ() * 0.045D);
        double lushScore = region + detail * 0.35D + patch * 0.15D;
        double lushThreshold = wet ? 0.02D : 0.48D;

        if (pos.getY() >= 8 && pos.getY() <= 72 && lushScore > lushThreshold) {
            return CaveStyle.LUSH;
        }
        if (region * 0.35D - detail > 0.18D && pos.getY() < 74) {
            return CaveStyle.DRIPSTONE;
        }
        return CaveStyle.NORMAL;
    }

    private void decorateLushWorld(World world, Random random, BlockPos pos) {
        BlockPos floor = scan(world, pos, EnumFacing.DOWN, LUSH_SURFACE_SCAN_DISTANCE);
        BlockPos ceiling = scan(world, pos, EnumFacing.UP, LUSH_SURFACE_SCAN_DISTANCE);
        if (floor != null) {
            placeMossPatchWorld(world, random, floor.up(), 4 + random.nextInt(4));
            if (random.nextBoolean()) {
                placeClayAndDripleafWorld(world, random, floor.up());
            }
        }
        if (ceiling != null) {
            if (isLushGroundReplaceable(world.getBlockState(ceiling)) && random.nextBoolean()) {
                world.setBlockState(ceiling, ModBlocks.MOSS_BLOCK.getDefaultState(), 2);
            }
            if (random.nextBoolean()) {
                placeCaveVineWorld(world, random, ceiling.down());
            }
            if (random.nextInt(6) == 0 && world.isAirBlock(ceiling.down())) {
                world.setBlockState(ceiling.down(), ModBlocks.SPORE_BLOSSOM.getDefaultState(), 2);
            }
        }
    }

    private void decorateDripstoneWorld(World world, Random random, BlockPos pos) {
        BlockPos floor = scan(world, pos, EnumFacing.DOWN, 12);
        BlockPos ceiling = scan(world, pos, EnumFacing.UP, 12);
        if (random.nextInt(3) == 0 && floor != null) {
            world.setBlockState(floor, ModBlocks.DRIPSTONE_BLOCK.getDefaultState(), 2);
            ((PointedDripstoneBlock)ModBlocks.POINTED_DRIPSTONE).placeColumn(world, floor.up(), EnumFacing.UP, 1 + random.nextInt(3), 2);
        }
        if (ceiling != null) {
            world.setBlockState(ceiling, ModBlocks.DRIPSTONE_BLOCK.getDefaultState(), 2);
            ((PointedDripstoneBlock)ModBlocks.POINTED_DRIPSTONE).placeColumn(world, ceiling.down(), EnumFacing.DOWN, 1 + random.nextInt(4), 2);
        }
    }

    private void placeMossPatchWorld(World world, Random random, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius + random.nextInt(3)) {
                    continue;
                }
                BlockPos top = center.add(x, 0, z);
                BlockPos ground = top.down();
                if (world.isAirBlock(top) && isLushGroundReplaceable(world.getBlockState(ground))) {
                    world.setBlockState(ground, ModBlocks.MOSS_BLOCK.getDefaultState(), 2);
                    if (random.nextInt(4) == 0) {
                        world.setBlockState(top, ModBlocks.MOSS_CARPET.getDefaultState(), 2);
                    } else if (random.nextInt(9) == 0) {
                        world.setBlockState(top, (random.nextBoolean() ? ModBlocks.Azalea : ModBlocks.Flowering_Azalea).getDefaultState(), 2);
                    }
                }
            }
        }
    }

    private void placeClayAndDripleafWorld(World world, Random random, BlockPos center) {
        for (int i = 0; i < 12; i++) {
            BlockPos pos = center.add(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            if (world.isAirBlock(pos) && isLushGroundReplaceable(world.getBlockState(pos.down()))) {
                world.setBlockState(pos.down(), Blocks.CLAY.getDefaultState(), 2);
                if (random.nextBoolean() && world.isAirBlock(pos.up())) {
                    ((SmallDripleaf)ModBlocks.SMALL_DRIPLEAF).placeAt(world, pos, EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)], 2);
                } else if (random.nextBoolean()) {
                    EnumFacing facing = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
                    int height = 1 + random.nextInt(3);
                    for (int y = 0; y < height && world.isAirBlock(pos.up(y)); y++) {
                        world.setBlockState(pos.up(y), ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(DripleafStem.FACING, facing), 2);
                    }
                    if (world.isAirBlock(pos.up(height))) {
                        world.setBlockState(pos.up(height), ModBlocks.BIG_DRIPLEAF.getDefaultState().withProperty(BigDripleaf.FACING, facing), 2);
                    }
                }
            }
        }
    }

    private BlockPos findCaveAirInColumn(World world, int x, int startY, int z, int distance) {
        BlockPos pos = getDecoratableCaveAir(world, x, startY, z);
        if (pos != null) {
            return pos;
        }

        for (int offset = 1; offset <= distance; offset++) {
            pos = getDecoratableCaveAir(world, x, startY + offset, z);
            if (pos != null) {
                return pos;
            }

            pos = getDecoratableCaveAir(world, x, startY - offset, z);
            if (pos != null) {
                return pos;
            }
        }

        return null;
    }

    private BlockPos getDecoratableCaveAir(World world, int x, int y, int z) {
        if (y < 5 || y > 86) {
            return null;
        }

        BlockPos pos = new BlockPos(x, y, z);
        return world.isAirBlock(pos) && !world.canSeeSky(pos) ? pos : null;
    }

    private void placeCaveVineWorld(World world, Random random, BlockPos start) {
        if (!world.isAirBlock(start) || !world.getBlockState(start.up()).isSideSolid(world, start.up(), EnumFacing.DOWN)) {
            return;
        }
        int length = 1 + random.nextInt(6);
        for (int i = 0; i < length; i++) {
            BlockPos pos = start.down(i);
            if (!world.isAirBlock(pos)) {
                break;
            }
            boolean berries = random.nextInt(5) == 0;
            if (i == length - 1) {
                world.setBlockState(pos, ModBlocks.CAVE_VINE.getDefaultState().withProperty(CaveVine.AGE, 1).withProperty(CaveVine.BERRIES, berries), 2);
            } else {
                world.setBlockState(pos, ModBlocks.CAVE_VINE_PLANT.getDefaultState().withProperty(CaveVinePlant.BERRIES, berries), 2);
            }
        }
    }

    private void placeGlowLichenWorld(World world, Random random, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing == EnumFacing.DOWN) {
                continue;
            }
            if (random.nextBoolean()) {
                BlockPos support = pos.offset(facing);
                if (world.isAirBlock(pos) && world.getBlockState(support).isSideSolid(world, support, facing.getOpposite())) {
                    world.setBlockState(pos, ((GlowLichenBlock)ModBlocks.GLOW_LICHEN).getStateForFace(facing), 2);
                    return;
                }
            }
        }
    }

    private BlockPos scan(World world, BlockPos start, EnumFacing direction, int distance) {
        BlockPos pos = start;
        for (int i = 0; i < distance; i++) {
            pos = pos.offset(direction);
            if (world.getBlockState(pos).getMaterial().isSolid()) {
                return pos;
            }
        }
        return null;
    }

    private void spawnAquaticCaveMobs(World world, Random random, int blockX, int blockZ) {
        if (random.nextInt(8) != 0) {
            return;
        }

        BlockPos pos = new BlockPos(blockX + random.nextInt(16), 12 + random.nextInt(38), blockZ + random.nextInt(16));
        if (EntityGlowSquid.canSpawnAt(world, pos, random)) {
            spawnMob(world, new EntityGlowSquid(world), pos);
        } else if (random.nextInt(3) == 0 && world.getBlockState(pos).getMaterial() == Material.WATER && world.getBlockState(pos.down()).getBlock() == Blocks.CLAY) {
            EntityAxolotl axolotl = new EntityAxolotl(world);
            axolotl.setRandomVariant();
            spawnMob(world, axolotl, pos);
        }
    }

    private void spawnMob(World world, EntityLiving entity, BlockPos pos) {
        if (world.countEntities(EnumCreatureType.WATER_CREATURE, false) > 40) {
            return;
        }
        entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        if (entity.getCanSpawnHere()) {
            world.spawnEntity(entity);
        }
    }

    private void decorateLushPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int x, int y, int z, double strength) {
        int floor = scanPrimer(context, x, y, z, EnumFacing.DOWN, LUSH_SURFACE_SCAN_DISTANCE);
        int ceiling = scanPrimer(context, x, y, z, EnumFacing.UP, LUSH_SURFACE_SCAN_DISTANCE);
        if (floor >= 0) {
            placeMossPatchPrimer(context, random, x, floor + 1, z, 3 + random.nextInt(4), strength);
            if (random.nextDouble() < 0.42D * strength) {
                placeClayAndDripleafPrimer(context, random, x, floor + 1, z, strength);
            }
        }
        if (ceiling >= 0) {
            if (random.nextDouble() < 0.72D * strength) {
                placeCeilingMossPrimer(context, random, x, ceiling, z, 2 + random.nextInt(3), strength);
            }
            if (random.nextDouble() < 0.72D * strength) {
                placeCaveVinePrimer(context, random, x, ceiling - 1, z, 2 + random.nextInt(6));
            }
            if (random.nextDouble() < 0.12D * strength && isAir(context.getBlockState(x, ceiling - 1, z))) {
                context.setBlockState(x, ceiling - 1, z, ModBlocks.SPORE_BLOSSOM.getDefaultState());
            } else if (random.nextDouble() < 0.18D * strength && isAir(context.getBlockState(x, ceiling - 1, z))) {
                context.setBlockState(x, ceiling - 1, z, ModBlocks.HANGING_ROOTS.getDefaultState());
            }
        }
    }

    private void placeMossPatchPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int centerX, int centerY, int centerZ, int radius, double strength) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(3)) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;
                if (!isInsideChunk(x, centerY, z)) {
                    continue;
                }

                if (!isAir(context.getBlockState(x, centerY, z)) || !isLushGroundReplaceable(context.getBlockState(x, centerY - 1, z))) {
                    continue;
                }

                context.setBlockState(x, centerY - 1, z, ModBlocks.MOSS_BLOCK.getDefaultState());
                if (random.nextDouble() < 0.22D * strength) {
                    context.setBlockState(x, centerY, z, ModBlocks.MOSS_CARPET.getDefaultState());
                } else if (random.nextDouble() < 0.06D * strength) {
                    context.setBlockState(x, centerY, z, (random.nextBoolean() ? ModBlocks.Azalea : ModBlocks.Flowering_Azalea).getDefaultState());
                } else if (random.nextDouble() < 0.05D * strength) {
                    context.setBlockState(x, centerY, z, Blocks.TALLGRASS.getDefaultState());
                }
            }
        }
    }

    private void placeCeilingMossPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int centerX, int ceilingY, int centerZ, int radius, double strength) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(3)) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;
                if (!isInsideChunk(x, ceilingY - 1, z)) {
                    continue;
                }

                if (isAir(context.getBlockState(x, ceilingY - 1, z)) && isLushGroundReplaceable(context.getBlockState(x, ceilingY, z))
                        && random.nextDouble() < 0.64D * strength) {
                    context.setBlockState(x, ceilingY, z, ModBlocks.MOSS_BLOCK.getDefaultState());
                }
            }
        }
    }

    private void placeClayAndDripleafPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int centerX, int centerY, int centerZ, double strength) {
        int count = 6 + (int)(10.0D * strength);
        for (int i = 0; i < count; i++) {
            int x = centerX + random.nextInt(9) - 4;
            int z = centerZ + random.nextInt(9) - 4;
            if (!isInsideChunk(x, centerY + 3, z)) {
                continue;
            }

            if (!isAir(context.getBlockState(x, centerY, z)) || !isLushGroundReplaceable(context.getBlockState(x, centerY - 1, z))) {
                continue;
            }

            context.setBlockState(x, centerY - 1, z, Blocks.CLAY.getDefaultState());
            EnumFacing facing = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
            if (random.nextDouble() < 0.55D && isAir(context.getBlockState(x, centerY + 1, z))) {
                context.setBlockState(x, centerY, z, ModBlocks.SMALL_DRIPLEAF.getDefaultState()
                        .withProperty(SmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
                        .withProperty(SmallDripleaf.FACING, facing));
                context.setBlockState(x, centerY + 1, z, ModBlocks.SMALL_DRIPLEAF.getDefaultState()
                        .withProperty(SmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.UPPER)
                        .withProperty(SmallDripleaf.FACING, facing));
            } else if (random.nextDouble() < 0.55D) {
                int height = 1 + random.nextInt(3);
                int placedStems = 0;
                for (int y = 0; y < height && isAir(context.getBlockState(x, centerY + y, z)); y++) {
                    context.setBlockState(x, centerY + y, z, ModBlocks.DRIPLEAF_STEM.getDefaultState().withProperty(DripleafStem.FACING, facing));
                    placedStems++;
                }
                if (placedStems == height && isAir(context.getBlockState(x, centerY + height, z))) {
                    context.setBlockState(x, centerY + height, z, ModBlocks.BIG_DRIPLEAF.getDefaultState().withProperty(BigDripleaf.FACING, facing));
                }
            }
        }
    }

    private void placeCaveVinePrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int x, int y, int z, int length) {
        if (!isInsideChunk(x, y, z) || !isAir(context.getBlockState(x, y, z)) || !isSolid(context.getBlockState(x, y + 1, z))) {
            return;
        }

        for (int i = 0; i < length && y - i > 1; i++) {
            if (!isAir(context.getBlockState(x, y - i, z))) {
                break;
            }

            boolean berries = random.nextInt(5) == 0;
            if (i == length - 1) {
                context.setBlockState(x, y - i, z, ModBlocks.CAVE_VINE.getDefaultState().withProperty(CaveVine.AGE, 1).withProperty(CaveVine.BERRIES, berries));
            } else {
                context.setBlockState(x, y - i, z, ModBlocks.CAVE_VINE_PLANT.getDefaultState().withProperty(CaveVinePlant.BERRIES, berries));
            }
        }
    }

    private void decorateDripstonePrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int x, int y, int z, double strength) {
        int floor = scanPrimer(context, x, y, z, EnumFacing.DOWN, 12);
        int ceiling = scanPrimer(context, x, y, z, EnumFacing.UP, 12);
        if (floor >= 0 && ceiling >= 0 && random.nextDouble() < 0.18D * strength) {
            placeDripstoneClusterPrimer(context, random, x, floor, z, 2 + random.nextInt(6), strength);
            return;
        }

        if (floor >= 0 && random.nextDouble() < 0.55D * strength) {
            placePointedDripstonePrimer(context, random, x, floor, z, EnumFacing.UP, 1 + random.nextInt(3 + (int)(strength * 3.0D)));
        }
        if (ceiling >= 0 && random.nextDouble() < 0.75D * strength) {
            placePointedDripstonePrimer(context, random, x, ceiling, z, EnumFacing.DOWN, 1 + random.nextInt(4 + (int)(strength * 4.0D)));
        }
    }

    private void placeDripstoneClusterPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int centerX, int floorY, int centerZ, int radius, double strength) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius || random.nextDouble() > (1.0D - distance / (radius + 1.0D)) * strength + 0.08D) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;
                if (!isInsideChunk(x, floorY + 1, z)) {
                    continue;
                }

                int airY = floorY + 1;
                if (isAir(context.getBlockState(x, airY, z)) && isDripstoneReplaceable(context.getBlockState(x, floorY, z))) {
                    int height = Math.max(1, (int)Math.round((radius - distance + 1.0D) * (0.45D + strength * 0.45D))) + random.nextInt(2);
                    placePointedDripstonePrimer(context, random, x, floorY, z, EnumFacing.UP, height);
                }

                int ceilingY = scanPrimer(context, x, airY, z, EnumFacing.UP, 12);
                if (ceilingY >= 0 && random.nextDouble() < 0.7D * strength) {
                    int height = Math.max(1, (int)Math.round((radius - distance + 1.0D) * (0.5D + strength * 0.5D))) + random.nextInt(3);
                    placePointedDripstonePrimer(context, random, x, ceilingY, z, EnumFacing.DOWN, height);
                }
            }
        }
    }

    private void placePointedDripstonePrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int x, int supportY, int z, EnumFacing direction, int length) {
        if (!isInsideChunk(x, supportY, z) || !isDripstoneReplaceable(context.getBlockState(x, supportY, z))) {
            return;
        }

        context.setBlockState(x, supportY, z, ModBlocks.DRIPSTONE_BLOCK.getDefaultState());
        int y = supportY + direction.getYOffset();
        for (int i = 0; i < length && y > 0 && y < 255; i++, y += direction.getYOffset()) {
            if (!isAirOrWater(context.getBlockState(x, y, z))) {
                break;
            }
            context.setBlockState(x, y, z, ModBlocks.POINTED_DRIPSTONE.getDefaultState()
                    .withProperty(PointedDripstoneBlock.VERTICAL_DIRECTION, direction)
                    .withProperty(PointedDripstoneBlock.THICKNESS, getGeneratedDripstoneThickness(i, length)));
        }
    }

    private PointedDripstoneBlock.Thickness getGeneratedDripstoneThickness(int index, int length) {
        if (length <= 1 || index == length - 1) {
            return PointedDripstoneBlock.Thickness.TIP;
        }
        if (index == 0) {
            return length >= 4 ? PointedDripstoneBlock.Thickness.BASE : PointedDripstoneBlock.Thickness.FRUSTUM;
        }
        if (index == length - 2) {
            return PointedDripstoneBlock.Thickness.FRUSTUM;
        }
        return PointedDripstoneBlock.Thickness.MIDDLE;
    }

    private void placeGlowLichenPrimer(ModernCaveTerrainCaveDecorationContext context, Random random, int x, int y, int z) {
        if (!isAir(context.getBlockState(x, y, z))) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing == EnumFacing.DOWN) {
                continue;
            }
            int sx = x + facing.getXOffset();
            int sy = y + facing.getYOffset();
            int sz = z + facing.getZOffset();
            if (sx < 0 || sx > 15 || sy < 0 || sy > 255 || sz < 0 || sz > 15) {
                continue;
            }
            if (isSolid(context.getBlockState(sx, sy, sz)) && random.nextBoolean()) {
                context.setBlockState(x, y, z, ((GlowLichenBlock)ModBlocks.GLOW_LICHEN).getStateForFace(facing));
                return;
            }
        }
    }

    private int scanPrimer(ModernCaveTerrainCaveDecorationContext context, int x, int y, int z, EnumFacing direction, int distance) {
        int checkY = y;
        for (int i = 0; i < distance; i++) {
            checkY += direction.getYOffset();
            if (checkY < 0 || checkY > 255) {
                return -1;
            }
            if (isSolid(context.getBlockState(x, checkY, z))) {
                return checkY;
            }
        }
        return -1;
    }

    private int findPrimerCaveAir(ModernCaveTerrainCaveDecorationContext context, int x, int startY, int z, int distance) {
        if (isInsideChunk(x, startY, z) && isAir(context.getBlockState(x, startY, z))) {
            return startY;
        }

        for (int offset = 1; offset <= distance; offset++) {
            int up = startY + offset;
            if (isInsideChunk(x, up, z) && isAir(context.getBlockState(x, up, z))) {
                return up;
            }

            int down = startY - offset;
            if (isInsideChunk(x, down, z) && isAir(context.getBlockState(x, down, z))) {
                return down;
            }
        }

        return -1;
    }

    private boolean isInsideChunk(int x, int y, int z) {
        return x >= 0 && x <= 15 && y >= 0 && y <= 255 && z >= 0 && z <= 15;
    }

    private boolean isNaturalStone(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == ModBlocks.TUFF || block == ModBlocks.DRIPSTONE_BLOCK;
    }

    private boolean isLushGroundReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return isNaturalStone(state)
                || block == Blocks.DIRT
                || block == Blocks.GRASS
                || block == Blocks.GRAVEL
                || block == Blocks.CLAY
                || block == ModBlocks.ROOTED_DIRT;
    }

    private boolean isAir(IBlockState state) {
        return state.getBlock() == Blocks.AIR;
    }

    private boolean isAirOrWater(IBlockState state) {
        return state.getBlock() == Blocks.AIR || state.getMaterial() == Material.WATER;
    }

    private boolean isSolid(IBlockState state) {
        return state.getMaterial().isSolid();
    }

    private boolean isDripstoneReplaceable(IBlockState state) {
        Block block = state.getBlock();
        return isNaturalStone(state)
                || block == Blocks.DIRT
                || block == Blocks.GRAVEL
                || block == Blocks.CLAY;
    }

    private void setFluidloggableWater(World world, BlockPos pos, IBlockState state, int flags) {
        FluidState fluidState = FluidState.of(Blocks.WATER.getDefaultState());
        world.setBlockState(pos, state, flags);
        FluidloggedUtils.setFluidState(world, pos, world.getBlockState(pos), fluidState, false, flags);
        world.scheduleUpdate(pos, Blocks.WATER, Blocks.WATER.tickRate(world));
    }

    private static int randomRange(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static double inverseSqrt(double value) {
        return 1.0D / Math.sqrt(value);
    }

    private static double distanceSq(BlockPos first, BlockPos second) {
        double x = first.getX() - second.getX();
        double y = first.getY() - second.getY();
        double z = first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }

    private static double clampedMap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        if (fromMin < fromMax) {
            if (value <= fromMin) {
                return toMin;
            }
            if (value >= fromMax) {
                return toMax;
            }
        } else {
            if (value >= fromMin) {
                return toMin;
            }
            if (value <= fromMax) {
                return toMax;
            }
        }

        return lerp((value - fromMin) / (fromMax - fromMin), toMin, toMax);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double smoothNoise(long seed, double x, double y, double z) {
        int x0 = floor(x);
        int y0 = floor(y);
        int z0 = floor(z);
        double tx = smoothstep(x - x0);
        double ty = smoothstep(y - y0);
        double tz = smoothstep(z - z0);
        return lerp(tz,
                lerp(ty, lerp(tx, valueNoise(seed, x0, y0, z0), valueNoise(seed, x0 + 1, y0, z0)), lerp(tx, valueNoise(seed, x0, y0 + 1, z0), valueNoise(seed, x0 + 1, y0 + 1, z0))),
                lerp(ty, lerp(tx, valueNoise(seed, x0, y0, z0 + 1), valueNoise(seed, x0 + 1, y0, z0 + 1)), lerp(tx, valueNoise(seed, x0, y0 + 1, z0 + 1), valueNoise(seed, x0 + 1, y0 + 1, z0 + 1))));
    }

    private static double valueNoise(long seed, int x, int y, int z) {
        long hash = seed;
        hash ^= x * 341873128712L;
        hash ^= y * 132897987541L;
        hash ^= z * 42317861L;
        hash ^= hash >> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >> 33;
        return ((hash >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }

    private static int floor(double value) {
        int integer = (int)value;
        return value < integer ? integer - 1 : integer;
    }

    private static double smoothstep(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double factor, double from, double to) {
        return from + factor * (to - from);
    }

    public enum CaveStyle {
        NORMAL,
        LUSH,
        DRIPSTONE
    }

    private static class GeodePoint {
        private final BlockPos pos;
        private final int offset;

        private GeodePoint(BlockPos pos, int offset) {
            this.pos = pos;
            this.offset = offset;
        }
    }
}
