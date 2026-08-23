package com.canoestudio.retrofutureupdateaquatic.world.gen;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralBlock;
import com.canoestudio.retrofutureupdateaquatic.block.BlockKelp;
import com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * Oceanic Expanse-style decoration.
 *
 * OE does not register warm/cold/deep replacement biomes or alter GenLayer.
 * It decorates the OCEAN and BEACH biomes that the world provider already
 * selected. The noise seeds and placement thresholds below are copied from
 * OE; only the block writes are adapted to this module's Fluidlogged API.
 */
public class AquaticWorldGenerator implements IWorldGenerator {

    private static final double OCEAN_NOISE_SCALE = 0.00764D;
    private static final double OCEAN_NOISE_MIN = 0.6D;
    private static final double REEF_NOISE_MIN = 0.96D;
    private static final double ICE_SHEET_SPREAD = 0.3D;
    private static final double KELP_CONNECTIVE = 0.2D;
    private static final double KELP_SPREAD = 0.1D;
    private static final double KELP_DENSITY = 0.2D;

    private final Biome[] oceanAndBeachBiomes;
    private final Biome[] oceanBiomes;
    private final NoiseGeneratorOctaves warmNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(2560), 4);
    private final NoiseGeneratorOctaves frozenNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(5120), 4);
    private final NoiseGeneratorOctaves iceSheetNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(1280), 4);
    private final NoiseGeneratorOctaves kelpNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(1244), 4);
    private final NoiseGeneratorOctaves icebergNoiseGenerator =
        new NoiseGeneratorOctaves(new Random(3840), 6);

    private double[] warmNoise = new double[256];
    private double[] sandNoise = new double[256];
    private double[] frozenNoise = new double[256];
    private double[] iceSheetNoise = new double[256];
    private double[] kelpNoise = new double[256];
    private double[] icebergIceNoise = new double[256];
    private double[] icebergCircleNoise = new double[256];
    private double[] icebergSnowNoise = new double[256];

    public AquaticWorldGenerator() {
        oceanAndBeachBiomes = collectOceanAndBeachBiomes();
        oceanBiomes = collectBiomes(BiomeDictionary.Type.OCEAN);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        generateWarmOcean(random, chunkX, chunkZ, world);
        generateFrozenOcean(random, chunkX, chunkZ, world);
        generateKelpForest(random, chunkX, chunkZ, world);

        // Same four WorldGenOceanPatch registrations as OE.
        generatePatch(random, chunkX, chunkZ, world, ModBlocks.SEAGRASS.getDefaultState(),
            2, 2, 48, 8, 4, 0.4D, -1, -1, -1, PatchTarget.RIVER);
        generatePatch(random, chunkX, chunkZ, world, ModBlocks.SEAGRASS.getDefaultState(),
            6, 2, 48, 8, 4, 0.3D, -1, -1, -1, PatchTarget.OCEAN);
        generatePatch(random, chunkX, chunkZ, world, ModBlocks.SEAGRASS.getDefaultState(),
            6, 2, 64, 8, 4, 0.8D, -1, -1, -1, PatchTarget.DEEP_OCEAN);
        generatePatch(random, chunkX, chunkZ, world, ModBlocks.SEAGRASS.getDefaultState(),
            2, 2, 48, 8, 4, 0.6D, -1, -1, -1, PatchTarget.SWAMP);
    }

    private void generateWarmOcean(Random random, int chunkX, int chunkZ, World world) {
        warmNoise = warmNoiseGenerator.generateNoiseOctaves(warmNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);

        boolean hasWarmFloor = false;
        boolean hasReef = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int posX = chunkX * 16 + x;
                int posZ = chunkZ * 16 + z;
                mutable.setPos(posX, 0, posZ);
                BlockPos waterFloor = world.getTopSolidOrLiquidBlock(mutable);
                Biome biome = world.getBiomeForCoordsBody(waterFloor);
                boolean valid = contains(oceanAndBeachBiomes, biome);
                double noise = warmNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;

                if (valid && noise > OCEAN_NOISE_MIN) {
                    hasWarmFloor = true;
                    mutable.setPos(waterFloor.getX(), waterFloor.getY() - 1, waterFloor.getZ());
                    if (world.getBlockState(mutable).getBlock() == Blocks.GRAVEL) {
                        world.setBlockState(mutable, Blocks.SAND.getDefaultState(), 16 | 2);
                    }
                }
                if (valid && noise > REEF_NOISE_MIN) {
                    hasReef = true;
                }
            }
        }

        if (hasWarmFloor) {
            generatePatch(random, chunkX, chunkZ, world, ModBlocks.SEA_PICKLE.getDefaultState(),
                1, 6, 16, 8, 4, 0.0D, 1, 1, 3, PatchTarget.OCEAN_AND_BEACH);
        }
        if (hasReef) {
            generateCoralReef(random, chunkX, chunkZ, world);
        }
    }

    private void generateCoralReef(Random random, int chunkX, int chunkZ, World world) {
        List<ModBlocks.CoralSet> corals = ModBlocks.corals();
        int chunkPosX = chunkX * 16;
        int chunkPosZ = chunkZ * 16;

        for (int i = 0; i <= 7; i++) {
            ModBlocks.CoralSet coral = corals.get(random.nextInt(corals.size()));
            BlockPos coralPos = world.getTopSolidOrLiquidBlock(new BlockPos(
                chunkPosX + 8 + random.nextInt(16), 0,
                chunkPosZ + 8 + random.nextInt(16)));
            int shape = random.nextInt(11);
            if (coralPos.getY() <= world.getSeaLevel() - 5
                    && !(world.getBlockState(coralPos.down()).getBlock() instanceof BlockCoralBlock)) {
                if (shape >= 8) {
                    generateCoralBulb(world, random, coralPos, coral.liveBlock.getDefaultState());
                } else if (shape >= 4) {
                    generateCoralBranch(world, random, coralPos, coral.liveBlock.getDefaultState());
                } else {
                    generateCoralStalk(world, random, coralPos, coral.liveBlock.getDefaultState());
                }
            }
        }

        for (ModBlocks.CoralSet coral : corals) {
            generatePatch(random, chunkX, chunkZ, world, coral.liveFan.getDefaultState(),
                8, 2, 48, 8, 16, 0.0D, 10, -1, -1, PatchTarget.OCEAN_AND_BEACH);
            generatePatch(random, chunkX, chunkZ, world, coral.livePlant.getDefaultState(),
                8, 2, 48, 8, 16, 0.0D, 10, -1, -1, PatchTarget.OCEAN_AND_BEACH);
        }
    }

    private void generateFrozenOcean(Random random, int chunkX, int chunkZ, World world) {
        sandNoise = warmNoiseGenerator.generateNoiseOctaves(sandNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        frozenNoise = frozenNoiseGenerator.generateNoiseOctaves(frozenNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        iceSheetNoise = iceSheetNoiseGenerator.generateNoiseOctaves(iceSheetNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, 0.225D, 1.0D, 0.225D);
        icebergIceNoise = icebergNoiseGenerator.generateNoiseOctaves(icebergIceNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, 1.0D, 1.0D, 1.0D);
        icebergCircleNoise = icebergNoiseGenerator.generateNoiseOctaves(icebergCircleNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, 1.0D, 1.0D, 1.0D);
        icebergSnowNoise = icebergNoiseGenerator.generateNoiseOctaves(icebergSnowNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, 0.825D, 1.0D, 0.825D);

        int chunkPosX = chunkX * 16 + 8;
        int chunkPosZ = chunkZ * 16 + 8;
        int groundReplaceLowest = world.getSeaLevel() - 3;
        boolean frozenAtChunkEnd = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int posX = chunkPosX + x;
                int posZ = chunkPosZ + z;
                BlockPos waterFloor = world.getTopSolidOrLiquidBlock(new BlockPos(posX, 0, posZ));
                Biome biome = world.getBiomeForCoordsBody(waterFloor);
                boolean valid = contains(oceanAndBeachBiomes, biome);
                boolean beach = BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
                double frozenValue = frozenNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                double sandValue = sandNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                if (!valid || frozenValue <= OCEAN_NOISE_MIN || sandValue > OCEAN_NOISE_MIN) {
                    continue;
                }
                if (x == 15 && z == 15) {
                    frozenAtChunkEnd = true;
                }

                BlockPos seaLevel = new BlockPos(posX, world.getSeaLevel(), posZ);
                if (world.getBlockState(waterFloor.down()).getBlock() == Blocks.SAND
                        && waterFloor.getY() < groundReplaceLowest) {
                    world.setBlockState(waterFloor.down(), Blocks.GRAVEL.getDefaultState(), 16 | 2);
                }
                if (!beach) {
                    spawnIceberg(world, random, chunkX, chunkZ, x, z, false, 0.3D, 0.01D, 3.0D, 1.0D);
                    spawnIceberg(world, random, chunkX, chunkZ, x, z, true, 0.3D, 0.01D, 10.0D, 0.5D);
                    icebergToppings(world, random, chunkX, chunkZ, x, z);
                }
                if (iceSheetNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.225D > ICE_SHEET_SPREAD
                        && world.getBlockState(seaLevel.down()).getBlock().isReplaceable(world, seaLevel.down())) {
                    world.setBlockState(seaLevel.down(), Blocks.ICE.getDefaultState(), 16 | 2);
                }
            }
        }
        if (frozenAtChunkEnd) {
            generateFrozenBlueIce(random, chunkX, chunkZ, world);
        }
    }

    private void spawnIceberg(World world, Random random, int chunkX, int chunkZ, int x, int z,
            boolean stack, double scale, double verticalScale, double smoothing, double slope) {
        int seaLevel = world.getSeaLevel();
        double noise = icebergIceNoise[x * 16 + z] / 2.0D;
        double circleNoise = icebergCircleNoise[x * 16 + z];
        for (int y = Math.min(seaLevel + 40, 256); y >= seaLevel - 18; y--) {
            BlockPos icePos = new BlockPos(chunkX * 16 + 8 + x, y, chunkZ * 16 + 8 + z);
            int difference = y - seaLevel;
            double topRatio = Math.pow((double) difference / smoothing, slope);
            double bottomRatio = Math.pow((double) -difference * 2 / smoothing / 4.0D, slope);
            double bottomDropoff = scale - difference * verticalScale / 1.5D + bottomRatio;
            if (difference <= 0) {
                difference = 1;
                topRatio = Math.pow((double) difference / smoothing, slope);
            }
            double topDropoff = scale + difference * verticalScale + topRatio;
            if (y > seaLevel - 2) {
                if (noise / 6.0D > topDropoff && (!stack || circleNoise > topDropoff)
                        && canReplaceIcebergBlock(world, icePos)) {
                    placeIcebergBlock(world, icePos);
                }
            } else if (noise / 6.0D - random.nextDouble() * 0.2D > bottomDropoff
                    && (!stack || circleNoise > bottomDropoff)
                    && canReplaceIcebergBlock(world, icePos)) {
                placeIcebergBlock(world, icePos);
            }
        }
    }

    private void icebergToppings(World world, Random random, int chunkX, int chunkZ, int x, int z) {
        for (int y = Math.min(world.getSeaLevel() + 42, 256); y >= world.getSeaLevel() - 18; y--) {
            BlockPos icePos = new BlockPos(chunkX * 16 + 8 + x, y, chunkZ * 16 + 8 + z);
            int difference = y - world.getSeaLevel();
            BlockPos above = icePos.up(difference / 3);
            if (y > world.getSeaLevel() - 2
                    && icebergSnowNoise[x * 16 + z] / 8.0D - random.nextDouble() * 0.8D
                        < difference * 0.2D - 3.0D
                    && world.getBlockState(icePos).getBlock() == Blocks.PACKED_ICE
                    && world.getBlockState(above).getMaterial() == Material.AIR) {
                world.setBlockState(icePos, Blocks.SNOW.getDefaultState(), 16 | 2);
            }
        }
    }

    private boolean canReplaceIcebergBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.WATER || FluidloggedSupport.isWater(world, pos)
            || state.getBlock() == Blocks.ICE || state.getMaterial() == Material.AIR;
    }

    private void placeIcebergBlock(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState(), 16 | 2);
    }

    private void generateFrozenBlueIce(Random random, int chunkX, int chunkZ, World world) {
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
                    random.nextInt(3) - random.nextInt(3), random.nextInt(3) - random.nextInt(3));
                int maxY = Math.max(world.getSeaLevel() + 2, 1);
                if (target.getY() > maxY) {
                    target = new BlockPos(target.getX(), maxY, target.getZ());
                }
                IBlockState state = world.getBlockState(target);
                if (state.getBlock() == Blocks.ICE || state.getBlock() == Blocks.PACKED_ICE
                        || state.getMaterial() == Material.WATER || FluidloggedSupport.isWater(world, target)) {
                    world.setBlockState(target, ModBlocks.BLUE_ICE.getDefaultState(), 16 | 2);
                } else if (random.nextInt(2) == 0) {
                    i--;
                }
            }
        }
    }

    private void generateKelpForest(Random random, int chunkX, int chunkZ, World world) {
        if (!contains(oceanBiomes, biomeAtChunkOrigin(world, chunkX, chunkZ))) {
            return;
        }
        double[] localFrozen = frozenNoiseGenerator.generateNoiseOctaves(new double[256], chunkX * 16, 0,
            chunkZ * 16, 16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        double[] localSand = warmNoiseGenerator.generateNoiseOctaves(new double[256], chunkX * 16, 0,
            chunkZ * 16, 16, 1, 16, OCEAN_NOISE_SCALE, 1.0D, OCEAN_NOISE_SCALE);
        kelpNoise = kelpNoiseGenerator.generateNoiseOctaves(kelpNoise, chunkX * 16, 0, chunkZ * 16,
            16, 1, 16, KELP_CONNECTIVE, 1.0D, KELP_CONNECTIVE);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double kelp = kelpNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.07D;
                double frozen = localFrozen[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                double sand = localSand[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                if (kelp <= KELP_SPREAD || frozen > OCEAN_NOISE_MIN || sand > 0.95D) {
                    continue;
                }
                BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(
                    chunkX * 16 + 8 + x, 0, chunkZ * 16 + 8 + z));
                if (random.nextDouble() < KELP_DENSITY
                        && ModBlocks.KELP.canPlaceBlockAt(world, pos)
                        && world.getBlockState(pos.down()).getBlock() != ModBlocks.KELP) {
                    growKelpStalk(world, random, pos);
                }
            }
        }
    }

    private void growKelpStalk(World world, Random random, BlockPos pos) {
        int growthLimit = random.nextInt(15);
        for (int i = 0; i < growthLimit && ModBlocks.KELP.canPlaceBlockAt(world, pos); i++) {
            world.setBlockState(pos, ModBlocks.KELP.getDefaultState()
                .withProperty(BlockKelp.AGE, i == growthLimit - 1 ? random.nextInt(15) : 0)
                .withProperty(BlockKelp.TOP, i == growthLimit - 1), 2 | 64);
            pos = pos.up();
        }
    }

    private void generatePatch(Random random, int chunkX, int chunkZ, World world, IBlockState state,
            int attempts, int chance, int amount, int spreadXZ, int spreadY, double tallChance,
            int seaLevelMin, int pickleMin, int pickleMax, PatchTarget target) {
        if (!target.matches(world, chunkX, chunkZ)) {
            return;
        }
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (random.nextInt(chance) != 0) {
                continue;
            }
            BlockPos pos = chunkOrigin.add(random.nextInt(16) + 8,
                Math.max(world.getSeaLevel() - 1, 1), random.nextInt(16) + 8);
            while ((world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                    || FluidloggedSupport.isWater(world, pos)) && pos.getY() > 0) {
                pos = pos.down();
            }
            if (!canPlace(state, world, pos.up())) {
                continue;
            }
            for (int i = 0; i < amount; i++) {
                BlockPos targetPos = pos.up().add(
                    random.nextInt(spreadXZ) - random.nextInt(spreadXZ),
                    random.nextInt(spreadY) - random.nextInt(spreadY),
                    random.nextInt(spreadXZ) - random.nextInt(spreadXZ));
                if (!FluidloggedSupport.isWater(world, targetPos)
                        || !canPlace(state, world, targetPos)
                        || (seaLevelMin > -1 && targetPos.getY() >= world.getSeaLevel() - seaLevelMin)) {
                    continue;
                }
                IBlockState placeState = state;
                if (state.getBlock() == ModBlocks.SEA_PICKLE) {
                    placeState = state.withProperty(BlockSeaPickle.PICKLES,
                        pickleMin + random.nextInt(pickleMax - pickleMin + 1));
                }
                if (state.getBlock() == ModBlocks.SEAGRASS && random.nextDouble() < tallChance
                        && FluidloggedSupport.isWater(world, targetPos.up())) {
                    ModBlocks.SEAGRASS.placeTallAt(world, targetPos, 2 | 64);
                } else {
                    FluidloggedSupport.setFluidloggableBlock(world, targetPos, placeState, 2 | 64);
                }
            }
        }
    }

    private static boolean canPlace(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().canPlaceBlockAt(world, pos);
    }

    private void generateCoralBulb(World world, Random random, BlockPos pos, IBlockState state) {
        int down = random.nextInt(2) + 1;
        int length = random.nextInt(3) + 3;
        int height = random.nextInt(3) + 3;
        int width = random.nextInt(3) + 3;
        for (int x = 0; x <= length; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = 0; z <= width; z++) {
                    boolean edge = (x != 0 || (y != 0 && z != 0 && y != height && z != width))
                        && (x != length || (y != 0 && z != 0 && y != height && z != width))
                        && (y != 0 || (z != 0 && z != width))
                        && (y != height || (z != 0 && z != width));
                    boolean inside = x != 0 && y != 0 && z != 0
                        && x != length && y != height && z != width;
                    if (edge && !inside && random.nextFloat() > 0.1F) {
                        placeCoralBlock(world, pos.add(x, y - down, z), state);
                    }
                }
            }
        }
    }

    private void generateCoralBranch(World world, Random random, BlockPos pos, IBlockState state) {
        int branchCount = 2 + random.nextInt(2);
        EnumFacing first = EnumFacing.Plane.HORIZONTAL.random(random);
        List<EnumFacing> directions = new ArrayList<EnumFacing>();
        directions.add(first);
        directions.add(first.rotateY());
        directions.add(first.rotateYCCW());
        java.util.Collections.shuffle(directions, random);
        placeCoralBlock(world, pos, state);
        for (int branch = 0; branch < branchCount; branch++) {
            EnumFacing facing = directions.get(branch);
            BlockPos cursor = pos;
            if (facing != first) {
                cursor = pos.up();
                for (int i = 1; i <= Math.max(random.nextInt(6) - 3, 1); i++) {
                    cursor = pos.offset(facing, i).up();
                    placeCoralBlock(world, cursor, state);
                }
            }
            int length = random.nextInt(4) + 1;
            for (int i = 0; i <= length; i++) {
                placeCoralBlock(world, cursor, state);
                cursor = cursor.offset(first);
                if (random.nextFloat() < 0.25F) {
                    cursor = cursor.up();
                }
            }
        }
    }

    private void generateCoralStalk(World world, Random random, BlockPos pos, IBlockState state) {
        int baseHeight = random.nextInt(3) + 1;
        List<EnumFacing> directions = new ArrayList<EnumFacing>();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            directions.add(facing);
        }
        java.util.Collections.shuffle(directions, random);
        int branch = 0;
        BlockPos cursor = pos;
        for (int i = 0; i <= baseHeight; i++) {
            placeCoralBlock(world, cursor, state);
            if (branch != 4 && random.nextFloat() < 0.75F) {
                generateCoralBranchArm(world, random, cursor, directions.get(branch++), state);
            }
            cursor = cursor.up();
        }
    }

    private void generateCoralBranchArm(World world, Random random, BlockPos pos,
            EnumFacing facing, IBlockState state) {
        BlockPos cursor = pos;
        int height = random.nextInt(4) + 1;
        for (int i = 0; i <= height; i++) {
            if (!FluidloggedSupport.isWater(world, cursor)) {
                cursor = cursor.up();
                if (i <= 1) {
                    cursor = cursor.offset(facing);
                } else if (random.nextFloat() < 0.25F) {
                    cursor = cursor.offset(EnumFacing.Plane.HORIZONTAL.random(random));
                }
                placeCoralBlock(world, cursor, state);
            }
        }
    }

    private void placeCoralBlock(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() < world.getSeaLevel() - 1) {
            FluidloggedSupport.setFluidloggableBlock(world, pos, state, 16 | 2);
        }
    }

    private Biome biomeAtChunkOrigin(World world, int chunkX, int chunkZ) {
        return world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));
    }

    private static boolean contains(Biome[] biomes, Biome biome) {
        for (Biome candidate : biomes) {
            if (candidate == biome) {
                return true;
            }
        }
        return false;
    }

    private static Biome[] collectOceanAndBeachBiomes() {
        List<Biome> biomes = new ArrayList<Biome>();
        addUnique(biomes, BiomeDictionary.getBiomes(BiomeDictionary.Type.OCEAN));
        addUnique(biomes, BiomeDictionary.getBiomes(BiomeDictionary.Type.BEACH));
        return biomes.toArray(new Biome[biomes.size()]);
    }

    private static Biome[] collectBiomes(BiomeDictionary.Type type) {
        List<Biome> biomes = new ArrayList<Biome>();
        addUnique(biomes, BiomeDictionary.getBiomes(type));
        return biomes.toArray(new Biome[biomes.size()]);
    }

    private static void addUnique(List<Biome> target, Set<Biome> source) {
        for (Biome biome : source) {
            if (!target.contains(biome)) {
                target.add(biome);
            }
        }
    }

    private enum PatchTarget {
        RIVER {
            @Override
            boolean matches(World world, int chunkX, int chunkZ) {
                return BiomeDictionary.hasType(biome(world, chunkX, chunkZ), BiomeDictionary.Type.RIVER);
            }
        },
        OCEAN {
            @Override
            boolean matches(World world, int chunkX, int chunkZ) {
                return biome(world, chunkX, chunkZ) == Biomes.OCEAN;
            }
        },
        DEEP_OCEAN {
            @Override
            boolean matches(World world, int chunkX, int chunkZ) {
                return biome(world, chunkX, chunkZ) == Biomes.DEEP_OCEAN;
            }
        },
        SWAMP {
            @Override
            boolean matches(World world, int chunkX, int chunkZ) {
                return BiomeDictionary.hasType(biome(world, chunkX, chunkZ), BiomeDictionary.Type.SWAMP);
            }
        },
        OCEAN_AND_BEACH {
            @Override
            boolean matches(World world, int chunkX, int chunkZ) {
                Biome biome = biome(world, chunkX, chunkZ);
                return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
            }
        };

        abstract boolean matches(World world, int chunkX, int chunkZ);

        private static Biome biome(World world, int chunkX, int chunkZ) {
            return world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));
        }
    }
}
