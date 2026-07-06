package com.canoestudio.retrofuturemc.contents.world.gen;

import com.canoestudio.retrofuturemc.contents.blocks.AmethystClusterBlock;
import com.canoestudio.retrofuturemc.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturemc.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturemc.contents.mobs.glowsquid.EntityGlowSquid;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
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

public class RetroFutureWorldGenerator implements IWorldGenerator {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
    private static final IBlockState DEEPSLATE = ModBlocks.DeepSlate.getDefaultState();
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

        spawnAquaticCaveMobs(world, random, blockX, blockZ);
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

    private boolean isNaturalStone(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == ModBlocks.TUFF;
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

    private static class GeodePoint {
        private final BlockPos pos;
        private final int offset;

        private GeodePoint(BlockPos pos, int offset) {
            this.pos = pos;
            this.offset = offset;
        }
    }
}
