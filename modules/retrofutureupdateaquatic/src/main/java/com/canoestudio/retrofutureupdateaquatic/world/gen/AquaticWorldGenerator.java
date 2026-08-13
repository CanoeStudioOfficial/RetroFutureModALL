package com.canoestudio.retrofutureupdateaquatic.world.gen;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralFan;
import com.canoestudio.retrofutureupdateaquatic.block.BlockKelp;
import com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle;
import com.canoestudio.retrofutureupdateaquatic.block.BlockSeagrass;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * 1.13 aquatic terrain and seafloor features, adapted from Oceanic Expanse.
 * OE-only blocks such as coquina, sea oats and tube sponge are intentionally
 * not referenced here.
 */
public class AquaticWorldGenerator implements IWorldGenerator {

    private final NoiseGeneratorOctaves warmOceanNoise = new NoiseGeneratorOctaves(new Random(2560), 4);
    private final NoiseGeneratorOctaves frozenOceanNoise = new NoiseGeneratorOctaves(new Random(5120), 4);
    private final NoiseGeneratorOctaves iceSheetNoise = new NoiseGeneratorOctaves(new Random(1280), 4);
    private final NoiseGeneratorOctaves kelpForestNoise = new NoiseGeneratorOctaves(new Random(1244), 4);
    private final NoiseGeneratorOctaves icebergNoise = new NoiseGeneratorOctaves(new Random(3840), 6);

    private double[] warmNoise = new double[256];
    private double[] frozenNoise = new double[256];
    private double[] iceNoise = new double[256];
    private double[] kelpNoise = new double[256];
    private double[] icebergIceNoise = new double[256];
    private double[] icebergCircleNoise = new double[256];

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        Biome biome = world.getBiome(new BlockPos(blockX + 8, 0, blockZ + 8));
        boolean ocean = BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
        boolean river = BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
        boolean swamp = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP);
        if (!ocean && !river && !swamp) {
            return;
        }

        warmNoise = warmOceanNoise.generateNoiseOctaves(warmNoise, blockX, 0, blockZ,
            16, 1, 16, 0.00764D, 1.0D, 0.00764D);
        frozenNoise = frozenOceanNoise.generateNoiseOctaves(frozenNoise, blockX, 0, blockZ,
            16, 1, 16, 0.00764D, 1.0D, 0.00764D);
        iceNoise = iceSheetNoise.generateNoiseOctaves(iceNoise, blockX, 0, blockZ,
            16, 1, 16, 0.225D, 1.0D, 0.225D);
        kelpNoise = kelpForestNoise.generateNoiseOctaves(kelpNoise, blockX, 0, blockZ,
            16, 1, 16, 0.035D, 1.0D, 0.035D);
        icebergIceNoise = icebergNoise.generateNoiseOctaves(icebergIceNoise, blockX, 0, blockZ,
            16, 1, 16, 1.0D, 1.0D, 1.0D);
        icebergCircleNoise = icebergNoise.generateNoiseOctaves(icebergCircleNoise, blockX, 0, blockZ,
            16, 1, 16, 1.0D, 1.0D, 1.0D);

        generatePlants(world, random, blockX, blockZ, ocean, river, swamp);
        if (ocean) {
            generateWarmOceanFeatures(world, random, blockX, blockZ, chunkX, chunkZ);
            generateFrozenOceanFeatures(world, random, blockX, blockZ, chunkX, chunkZ, biome);
        }
    }

    private void generatePlants(World world, Random random, int blockX, int blockZ,
            boolean ocean, boolean river, boolean swamp) {
        int seagrassAttempts = ocean ? 18 : 8;
        for (int i = 0; i < seagrassAttempts; i++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null) {
                continue;
            }
            BlockPos place = floor.up();
            if (!FluidloggedSupport.isWater(world, place)
                    || !ModBlocks.SEAGRASS.canPlaceBlockAt(world, place)) {
                continue;
            }
            if (random.nextFloat() < (ocean ? 0.35F : swamp ? 0.55F : 0.18F)
                    && FluidloggedSupport.isWater(world, place.up())) {
                placeTallSeagrass(world, place);
            } else {
                setWaterlogged(world, place, ModBlocks.SEAGRASS.getDefaultState());
            }
        }

        if (!ocean) {
            return;
        }
        for (int i = 0; i < 18; i++) {
            int x = blockX + random.nextInt(16);
            int z = blockZ + random.nextInt(16);
            int index = random.nextInt(16) * 16 + random.nextInt(16);
            if (kelpNoise[index] / 4.0D - random.nextDouble() * 0.07D < 0.2D) {
                continue;
            }
            BlockPos floor = findSeaFloor(world, x, z);
            if (floor == null || random.nextInt(3) == 0) {
                continue;
            }
            BlockPos place = floor.up();
            if (ModBlocks.KELP.canPlaceBlockAt(world, place)) {
                growKelpColumn(world, random, place, 2 + random.nextInt(10));
            }
        }
    }

    private void generateWarmOceanFeatures(World world, Random random, int blockX, int blockZ,
            int chunkX, int chunkZ) {
        boolean reefPatch = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double value = warmNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                if (value <= 0.6D) {
                    continue;
                }
                BlockPos floor = findSeaFloor(world, blockX + x, blockZ + z);
                if (floor != null && floor.getY() < world.getSeaLevel() - 3
                        && world.getBlockState(floor).getBlock() == Blocks.GRAVEL) {
                    world.setBlockState(floor, Blocks.SAND.getDefaultState(), 18);
                }
                if (value > 0.72D) {
                    reefPatch = true;
                }
            }
        }
        if (!reefPatch) {
            return;
        }

        for (int i = 0; i < 12; i++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null || floor.getY() >= world.getSeaLevel() - 3) {
                continue;
            }
            BlockPos place = floor.up();
            if (random.nextInt(3) == 0 && ModBlocks.SEA_PICKLE.canPlaceBlockAt(world, place)) {
                setWaterlogged(world, place, ModBlocks.SEA_PICKLE.getDefaultState()
                    .withProperty(com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle.PICKLES,
                        random.nextInt(4) + 1));
            }
        }
        generateCoralReef(world, random, blockX, blockZ);
    }

    private void generateCoralReef(World world, Random random, int blockX, int blockZ) {
        List<ModBlocks.CoralSet> corals = ModBlocks.corals();
        for (int i = 0; i < 8; i++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null || floor.getY() > world.getSeaLevel() - 5
                    || world.getBlockState(floor).getBlock()
                        instanceof com.canoestudio.retrofutureupdateaquatic.block.BlockCoralBlock) {
                continue;
            }
            ModBlocks.CoralSet coral = corals.get(random.nextInt(corals.size()));
            BlockPos origin = floor.up();
            switch (random.nextInt(3)) {
                case 0:
                    generateCoralBulb(world, random, origin, coral.liveBlock.getDefaultState());
                    break;
                case 1:
                    generateCoralBranch(world, random, origin, coral.liveBlock.getDefaultState());
                    break;
                default:
                    generateCoralStalk(world, random, origin, coral.liveBlock.getDefaultState());
                    break;
            }
        }

        for (ModBlocks.CoralSet coral : corals) {
            for (int i = 0; i < 8; i++) {
                BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
                if (floor == null) {
                    continue;
                }
                BlockPos pos = floor.up();
                if (FluidloggedSupport.isWater(world, pos)
                        && coral.livePlant.canPlaceBlockAt(world, pos)) {
                    setWaterlogged(world, pos, coral.livePlant.getDefaultState());
                }
                tryPlaceFan(world, random, pos, coral);
            }
        }
    }

    private void generateCoralBulb(World world, Random random, BlockPos origin, IBlockState state) {
        int length = random.nextInt(3) + 3;
        int height = random.nextInt(3) + 3;
        int width = random.nextInt(3) + 3;
        int down = random.nextInt(2) + 1;
        for (int x = 0; x <= length; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = 0; z <= width; z++) {
                    boolean edge = (x == 0 || x == length || y == 0 || y == height || z == 0 || z == width);
                    if (edge && random.nextFloat() > 0.1F) {
                        placeCoralBlock(world, origin.add(x, y - down, z), state);
                    }
                }
            }
        }
    }

    private void generateCoralBranch(World world, Random random, BlockPos origin, IBlockState state) {
        int branchCount = 2 + random.nextInt(2);
        EnumFacing first = EnumFacing.Plane.HORIZONTAL.random(random);
        List<EnumFacing> directions = new java.util.ArrayList<EnumFacing>();
        directions.add(first);
        directions.add(first.rotateY());
        directions.add(first.rotateYCCW());
        java.util.Collections.shuffle(directions, random);
        placeCoralBlock(world, origin, state);
        for (int branch = 0; branch < branchCount; branch++) {
            EnumFacing facing = directions.get(branch);
            BlockPos cursor = origin;
            if (facing != first) {
                cursor = origin.up();
                int sideLength = Math.max(random.nextInt(6) - 3, 1);
                for (int i = 1; i <= sideLength; i++) {
                    cursor = origin.offset(facing, i).up();
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

    private void generateCoralStalk(World world, Random random, BlockPos origin, IBlockState state) {
        int baseHeight = random.nextInt(3) + 1;
        int branches = 2 + random.nextInt(3);
        List<EnumFacing> directions = new java.util.ArrayList<EnumFacing>();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            directions.add(facing);
        }
        java.util.Collections.shuffle(directions, random);
        int branchIndex = 0;
        BlockPos cursor = origin;
        for (int i = 0; i <= baseHeight; i++) {
            placeCoralBlock(world, cursor, state);
            if (branchIndex < branches && random.nextFloat() < 0.75F) {
                generateCoralBranchArm(world, random, cursor, directions.get(branchIndex++), state);
            }
            cursor = cursor.up();
        }
    }

    private void generateCoralBranchArm(World world, Random random, BlockPos origin, EnumFacing facing,
            IBlockState state) {
        BlockPos cursor = origin;
        int height = 1 + random.nextInt(4);
        for (int i = 0; i <= height; i++) {
            if (!isWaterOrAir(world, cursor)) {
                cursor = cursor.up();
                if (i <= 1) {
                    cursor = cursor.offset(facing);
                } else if (random.nextFloat() < 0.25F) {
                    cursor = cursor.offset(EnumFacing.Plane.HORIZONTAL.random(random));
                }
            }
            placeCoralBlock(world, cursor, state);
        }
    }

    private void placeCoralBlock(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() >= world.getSeaLevel() - 1 || !isWaterOrAir(world, pos)) {
            return;
        }
        world.setBlockState(pos, state, 18);
    }

    private void tryPlaceFan(World world, Random random, BlockPos pos, ModBlocks.CoralSet coral) {
        if (random.nextInt(3) != 0) {
            return;
        }
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos target = pos.offset(facing);
            if (FluidloggedSupport.isWater(world, target)
                    && coral.liveFan.canPlaceBlockOnSide(world, target, facing)) {
                setWaterlogged(world, target, coral.liveFan.getDefaultState()
                    .withProperty(BlockCoralFan.FACING, facing));
                return;
            }
        }
    }

    private void generateFrozenOceanFeatures(World world, Random random, int blockX, int blockZ,
            int chunkX, int chunkZ, Biome biome) {
        if (!isFrozenOceanLike(biome)) {
            return;
        }
        boolean frozenPatch = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double value = frozenNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.01D;
                if (value <= 0.6D) {
                    continue;
                }
                frozenPatch = true;
                BlockPos floor = findSeaFloor(world, blockX + x, blockZ + z);
                if (floor != null && floor.getY() < world.getSeaLevel() - 3
                        && world.getBlockState(floor).getBlock() == Blocks.SAND) {
                    world.setBlockState(floor, Blocks.GRAVEL.getDefaultState(), 18);
                }
                if (iceNoise[x * 16 + z] / 4.0D - random.nextDouble() * 0.225D > 0.55D) {
                    BlockPos ice = new BlockPos(blockX + x, world.getSeaLevel() - 1, blockZ + z);
                    if (isWaterOrAir(world, ice)) {
                        world.setBlockState(ice, Blocks.ICE.getDefaultState(), 18);
                    }
                }
            }
        }
        if (!frozenPatch || random.nextInt(3) != 0) {
            return;
        }
        int x = blockX + 4 + random.nextInt(8);
        int z = blockZ + 4 + random.nextInt(8);
        generateIceberg(world, random, chunkX, chunkZ, x, z);
    }

    private void generateIceberg(World world, Random random, int chunkX, int chunkZ, int x, int z) {
        BlockPos surface = new BlockPos(x, world.getSeaLevel(), z);
        if (!isWaterOrAir(world, surface) && !isWaterOrAir(world, surface.down())) {
            return;
        }
        int height = 7 + random.nextInt(10);
        int below = Math.min(12 + random.nextInt(7), height + 8);
        int width = 5 + random.nextInt(5);
        boolean elongated = random.nextBoolean();
        boolean blue = random.nextInt(5) == 0;
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        for (int y = -below; y <= height; y++) {
            double progress = y >= 0 ? (double)y / Math.max(1, height) : (double)-y / Math.max(1, below);
            double radius = width * (1.0D - progress * (y >= 0 ? 0.72D : 0.55D));
            if (y > height - 3) {
                radius *= 0.65D;
            }
            radius = Math.max(1.5D, radius);
            int range = (int)Math.ceil(radius) + 1;
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    double xScale = elongated ? 1.45D : 1.0D;
                    double zScale = elongated ? 0.8D : 1.0D;
                    double distance = (dx * dx) / (radius * radius * xScale)
                        + (dz * dz) / (radius * radius * zScale);
                    if (distance > 1.0D || random.nextDouble() < distance * 0.11D) {
                        continue;
                    }
                    BlockPos pos = surface.add(dx, y, dz);
                    if (pos.getX() >= blockX && pos.getX() < blockX + 16
                            && pos.getZ() >= blockZ && pos.getZ() < blockZ + 16
                            && canReplaceForIceberg(world, pos)) {
                        world.setBlockState(pos, blue && y <= 0 && random.nextInt(18) == 0
                            ? ModBlocks.BLUE_ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState(), 18);
                    }
                }
            }
        }
        addIcebergSnow(world, random, blockX, blockZ, surface, width + 2, height);
        growBlueIce(world, random, blockX, blockZ, surface, width + 2, below);
    }

    private void addIcebergSnow(World world, Random random, int blockX, int blockZ, BlockPos surface,
            int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = height + 1; y >= 0; y--) {
                    BlockPos pos = surface.add(dx, y, dz);
                    if (isInsideChunk(pos, blockX, blockZ) && world.getBlockState(pos).getBlock() == Blocks.PACKED_ICE
                            && world.isAirBlock(pos.up()) && random.nextInt(3) != 0) {
                        world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState(), 18);
                        break;
                    }
                }
            }
        }
    }

    private void growBlueIce(World world, Random random, int blockX, int blockZ, BlockPos surface,
            int radius, int below) {
        for (int i = 0; i < 40; i++) {
            BlockPos pos = surface.add(random.nextInt(radius * 2 + 1) - radius,
                -random.nextInt(Math.max(2, below)), random.nextInt(radius * 2 + 1) - radius);
            if (isInsideChunk(pos, blockX, blockZ) && world.getBlockState(pos).getBlock() == Blocks.PACKED_ICE
                    && touchesBlueIce(world, pos)) {
                world.setBlockState(pos, ModBlocks.BLUE_ICE.getDefaultState(), 18);
            }
        }
    }

    private boolean touchesBlueIce(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (world.getBlockState(pos.offset(facing)).getBlock() == ModBlocks.BLUE_ICE) {
                return true;
            }
        }
        return false;
    }

    private boolean canReplaceForIceberg(World world, BlockPos pos) {
        return world.isAirBlock(pos) || FluidloggedSupport.isWater(world, pos)
            || world.getBlockState(pos).getBlock() == Blocks.ICE
            || world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER;
    }

    private boolean isWaterOrAir(World world, BlockPos pos) {
        return world.isAirBlock(pos) || FluidloggedSupport.isWater(world, pos);
    }

    private boolean isInsideChunk(BlockPos pos, int blockX, int blockZ) {
        return pos.getX() >= blockX && pos.getX() < blockX + 16
            && pos.getZ() >= blockZ && pos.getZ() < blockZ + 16;
    }

    private void placeTallSeagrass(World world, BlockPos pos) {
        setWaterlogged(world, pos, ModBlocks.SEAGRASS.getDefaultState().withProperty(BlockSeagrass.TYPE, 1));
        setWaterlogged(world, pos.up(), ModBlocks.SEAGRASS.getDefaultState().withProperty(BlockSeagrass.TYPE, 2));
    }

    private void growKelpColumn(World world, Random random, BlockPos pos, int height) {
        BlockPos cursor = pos;
        for (int i = 0; i < height && ModBlocks.KELP.canPlaceBlockAt(world, cursor); i++) {
            setWaterlogged(world, cursor, ModBlocks.KELP.getDefaultState()
                .withProperty(BlockKelp.AGE, i == height - 1 ? random.nextInt(15) : 0)
                .withProperty(BlockKelp.TOP, i == height - 1));
            cursor = cursor.up();
        }
    }

    private void setWaterlogged(World world, BlockPos pos, IBlockState state) {
        FluidloggedSupport.setFluidloggableBlock(world, pos, state, 18);
    }

    private BlockPos findSeaFloor(World world, int x, int z) {
        BlockPos pos = new BlockPos(x, Math.max(1, world.getSeaLevel() - 1), z);
        while (pos.getY() > 1) {
            IBlockState state = world.getBlockState(pos);
            Material material = state.getMaterial();
            if (!FluidloggedSupport.isWater(world, pos) && !state.getBlock().isReplaceable(world, pos)
                    && material != Material.LEAVES && material != Material.ICE) {
                return FluidloggedSupport.isWater(world, pos.up()) ? pos : null;
            }
            pos = pos.down();
        }
        return null;
    }

    private boolean isWarmOceanLike(Biome biome) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return name.contains("warm") || name.contains("lukewarm") || name.contains("tropical")
            || (!isFrozenOceanLike(biome) && biome.getDefaultTemperature() >= 0.8F);
    }

    private boolean isFrozenOceanLike(Biome biome) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return name.contains("frozen") || name.contains("ice") || name.contains("glacier")
            || (name.contains("ocean") && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)
            || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
            || biome.getDefaultTemperature() <= 0.15F));
    }
}
