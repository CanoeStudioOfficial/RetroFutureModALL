package com.canoestudio.retrofutureupdateaquatic.world.gen;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.Locale;

public class AquaticFrozenOceanGenerator implements IWorldGenerator {

    private static final double OCEAN_NOISE_SCALE = 0.00764D;
    private static final double OCEAN_NOISE_MIN = 0.6D;
    private static final double ICE_SHEET_SPREAD = 0.3D;

    private double[] sandNoise = new double[256];
    private final NoiseGeneratorOctaves warmOceanNoise =
        new NoiseGeneratorOctaves(new Random(2560), 4);
    private double[] frozenOceanNoise = new double[256];
    private final NoiseGeneratorOctaves frozenOceanNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(5120), 4);
    private double[] iceSheetNoise = new double[256];
    private final NoiseGeneratorOctaves iceSheetNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(1280), 4);
    private double[] icebergIceNoise = new double[256];
    private double[] icebergCircleNoise = new double[256];
    private double[] icebergSnowNoise = new double[256];
    private final NoiseGeneratorOctaves icebergNoise =
        new NoiseGeneratorOctaves(new Random(3840), 6);

    private final Biome[] biomes;

    public AquaticFrozenOceanGenerator() {
        this.biomes = collectOceanAndBeachBiomes();
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        spawnFrozenOcean(world, random, chunkX, chunkZ);
    }

    private void spawnFrozenOcean(World world, Random random, int chunkX, int chunkZ) {
        int chunkPosX = chunkX * 16 + 8;
        int chunkPosZ = chunkZ * 16 + 8;
        int groundReplaceLowest = world.getSeaLevel() - 3;

        sandNoise = warmOceanNoise.generateNoiseOctaves(sandNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        frozenOceanNoise = frozenOceanNoiseGenerator.generateNoiseOctaves(frozenOceanNoise,
            chunkX * 16, 0, chunkZ * 16, 16, 1, 16,
            OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        iceSheetNoise = iceSheetNoiseGenerator.generateNoiseOctaves(iceSheetNoise,
            chunkX * 16, 0, chunkZ * 16, 16, 1, 16, 0.225D, 1.0D, 0.225D);
        icebergIceNoise = icebergNoise.generateNoiseOctaves(icebergIceNoise,
            chunkX * 16, 0, chunkZ * 16, 16, 1, 16, 1.0D, 1.0D, 1.0D);
        icebergCircleNoise = icebergNoise.generateNoiseOctaves(icebergCircleNoise,
            chunkX * 16, 0, chunkZ * 16, 16, 1, 16, 1.0D, 1.0D, 1.0D);
        icebergSnowNoise = icebergNoise.generateNoiseOctaves(icebergSnowNoise,
            chunkX * 16, 0, chunkZ * 16, 16, 1, 16, 0.825D, 1.0D, 0.825D);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int posX = chunkPosX + x;
                int posZ = chunkPosZ + z;
                BlockPos waterFloor = world.getTopSolidOrLiquidBlock(new BlockPos(posX, 0, posZ));
                Biome biome = world.getBiomeForCoordsBody(waterFloor);
                boolean validBiome = contains(biomes, biome) && isFrozenOceanBiome(biome);
                boolean beachBiome = BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
                double frozenValue = frozenOceanNoise[x * 16 + z] / 4.0D
                    - random.nextDouble() * 0.01D;
                double sandValue = sandNoise[x * 16 + z] / 4.0D
                    - random.nextDouble() * 0.01D;

                if (!validBiome || frozenValue <= OCEAN_NOISE_MIN || sandValue > OCEAN_NOISE_MIN) {
                    continue;
                }

                BlockPos seaLevel = new BlockPos(posX, world.getSeaLevel(), posZ);
                if (world.getBlockState(waterFloor.down()).getBlock() == Blocks.SAND
                        && waterFloor.getY() < groundReplaceLowest) {
                    world.setBlockState(waterFloor.down(), Blocks.GRAVEL.getDefaultState(), 16 | 2);
                }

                if (!beachBiome) {
                    spawnIceBerg(world, random, chunkX, chunkZ, x, z,
                        false, 0.3D, 0.01D, 3.0D, 1.0D);
                    spawnIceBerg(world, random, chunkX, chunkZ, x, z,
                        true, 0.3D, 0.01D, 10.0D, 0.5D);
                    icebergToppings(world, random, chunkX, chunkZ, x, z);
                }

                if (iceSheetNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.225D
                        > ICE_SHEET_SPREAD
                        && world.getBlockState(seaLevel.down()).getBlock()
                            .isReplaceable(world, seaLevel.down())) {
                    world.setBlockState(seaLevel.down(), Blocks.ICE.getDefaultState(), 16 | 2);
                }

                if (x == 15 && z == 15) {
                    generateBlueIce(random, chunkX, chunkZ, world);
                }
            }
        }
    }

    private void spawnIceBerg(World world, Random random, int chunkX, int chunkZ,
            int x, int z, boolean stack, double scale, double verticalScale,
            double smoothing, double slope) {
        int seaLevel = world.getSeaLevel();
        int maxDepth = seaLevel - 18;
        int maxHeight = Math.min(seaLevel + 40, 256);
        double noise = icebergIceNoise[x * 16 + z] / 2.0D;
        double circleNoise = icebergCircleNoise[x * 16 + z];

        for (int y = maxHeight; y >= maxDepth; y--) {
            BlockPos icePos = new BlockPos(chunkX * 16 + 8 + x, y, chunkZ * 16 + 8 + z);
            int seaLevelDifference = y - seaLevel;
            double topHeightRatio = Math.pow(
                (double) seaLevelDifference / smoothing, slope);
            double bottomHeightRatio = Math.pow(
                ((double) -seaLevelDifference * 2 / smoothing) / 4, slope);
            double bottomHeightDropoff = scale
                - seaLevelDifference * verticalScale / 1.5D + bottomHeightRatio;

            if (seaLevelDifference <= 0) {
                seaLevelDifference = 1;
                topHeightRatio = Math.pow((double) seaLevelDifference / smoothing, slope);
            }

            double topHeightDropoff = scale + seaLevelDifference * verticalScale + topHeightRatio;
            if (y > seaLevel - 2) {
                if (noise / 6.0D > topHeightDropoff
                        && (!stack || circleNoise > topHeightDropoff)
                        && canReplaceIcebergBlock(world, icePos)) {
                    placeIcebergBlock(world, icePos);
                }
            } else if (noise / 6.0D - random.nextDouble() * 0.2D > bottomHeightDropoff
                    && (!stack || circleNoise > bottomHeightDropoff)
                    && canReplaceIcebergBlock(world, icePos)) {
                placeIcebergBlock(world, icePos);
            }
        }
    }

    private void icebergToppings(World world, Random random, int chunkX, int chunkZ, int x, int z) {
        int seaLevel = world.getSeaLevel();
        for (int y = Math.min(seaLevel + 42, 256); y >= seaLevel - 18; y--) {
            BlockPos icePos = new BlockPos(chunkX * 16 + 8 + x, y, chunkZ * 16 + 8 + z);
            int seaLevelDifference = y - seaLevel;
            double snowNoise = icebergSnowNoise[x * 16 + z];
            double heightScaling = seaLevelDifference * 0.2D - 3.0D;

            if (y > seaLevel - 2
                    && snowNoise / 8.0D - random.nextDouble() * 0.8D < heightScaling
                    && world.getBlockState(icePos).getBlock() == Blocks.PACKED_ICE
                    && world.getBlockState(icePos.up(seaLevelDifference / 3)).getMaterial()
                        == Material.AIR) {
                world.setBlockState(icePos, Blocks.SNOW.getDefaultState(), 16 | 2);
            }
        }
    }

    private boolean canReplaceIcebergBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.WATER
            || FluidloggedSupport.isWater(world, pos)
            || state.getBlock() == Blocks.ICE
            || state.getMaterial() == Material.AIR;
    }

    private void placeIcebergBlock(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState(), 16 | 2);
    }

    private void generateBlueIce(Random random, int chunkX, int chunkZ, World world) {
        ChunkPos chunk = world.getChunk(chunkX, chunkZ).getPos();
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = random.nextInt(16) + 8;
            int z = random.nextInt(16) + 8;
            int y = Math.max(world.getSeaLevel() - 1 - random.nextInt(20), 1);
            if (random.nextInt(2) != 0) {
                continue;
            }

            BlockPos pos = chunk.getBlock(0, 0, 0).add(x, y, z);
            while ((world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                    || FluidloggedSupport.isWater(world, pos)) && pos.getY() > 0) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).getBlock() != Blocks.PACKED_ICE) {
                continue;
            }

            for (int i = 0; i < 50; i++) {
                BlockPos target = pos.add(random.nextInt(3) - random.nextInt(3),
                    random.nextInt(3) - random.nextInt(3),
                    random.nextInt(3) - random.nextInt(3));
                int maxY = Math.max(world.getSeaLevel() + 2, 1);
                if (target.getY() > maxY) {
                    target = new BlockPos(target.getX(), maxY, target.getZ());
                }

                IBlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.ICE
                        || state.getBlock() == Blocks.PACKED_ICE
                        || state.getMaterial() == Material.WATER
                        || FluidloggedSupport.isWater(world, target)) {
                    world.setBlockState(target, ModBlocks.BLUE_ICE.getDefaultState(), 16 | 2);
                } else if (random.nextInt(2) == 0) {
                    i--;
                }
            }
        }
    }

    private static boolean contains(Biome[] biomes, Biome biome) {
        for (Biome candidate : biomes) {
            if (candidate == biome) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFrozenOceanBiome(Biome biome) {
        if (biome == Biomes.FROZEN_OCEAN) {
            return true;
        }
        if (biome == null || !BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
            return false;
        }
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
            || name.contains("frozen")
            || name.contains("ice")
            || name.contains("glacier")
            || biome.getDefaultTemperature() <= 0.15F;
    }

    private static Biome[] collectOceanAndBeachBiomes() {
        List<Biome> result = new ArrayList<Biome>();
        addUnique(result, BiomeDictionary.getBiomes(BiomeDictionary.Type.OCEAN));
        addUnique(result, BiomeDictionary.getBiomes(BiomeDictionary.Type.BEACH));
        return result.toArray(new Biome[result.size()]);
    }

    private static void addUnique(List<Biome> target, java.util.Set<Biome> source) {
        for (Biome biome : source) {
            if (!target.contains(biome)) {
                target.add(biome);
            }
        }
    }
}
