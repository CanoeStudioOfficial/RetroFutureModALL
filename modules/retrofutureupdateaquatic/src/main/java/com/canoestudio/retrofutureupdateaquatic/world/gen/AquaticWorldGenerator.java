package com.canoestudio.retrofutureupdateaquatic.world.gen;

import com.canoestudio.retrofutureupdateaquatic.block.BlockKelp;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralFan;
import com.canoestudio.retrofutureupdateaquatic.block.BlockCoralPlant;
import com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

public class AquaticWorldGenerator implements IWorldGenerator {

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
        int seagrassAttempts = ocean ? 12 : 5;
        generateSeagrass(world, random, blockX, blockZ, seagrassAttempts, ocean ? 0.35F : 0.12F);
        if (ocean) {
            generateKelp(world, random, blockX, blockZ, 12);
            if (isFrozenOceanLike(biome) && random.nextInt(14) == 0) {
                generateIceberg(world, random, blockX, blockZ,
                    blockX + 4 + random.nextInt(8), blockZ + 4 + random.nextInt(8));
            } else if (isWarmOceanLike(biome)) {
                generateCoral(world, random, blockX, blockZ);
            }
        }
    }

    private void generateSeagrass(World world, Random random, int blockX, int blockZ, int attempts, float tallChance) {
        for (int i = 0; i < attempts; i++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null) {
                continue;
            }
            BlockPos place = floor.up();
            if (ModBlocks.SEAGRASS.canPlaceBlockAt(world, place)) {
                if (random.nextFloat() < tallChance && world.getBlockState(place.up()).getBlock() == Blocks.WATER) {
                    ModBlocks.SEAGRASS.placeTallAt(world, place, 2);
                } else {
                    world.setBlockState(place, ModBlocks.SEAGRASS.getDefaultState(), 2);
                }
            }
        }
    }

    private void generateKelp(World world, Random random, int blockX, int blockZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null || random.nextInt(3) == 0) {
                continue;
            }
            BlockPos place = floor.up();
            if (ModBlocks.KELP.canPlaceBlockAt(world, place)) {
                ((BlockKelp) ModBlocks.KELP).growColumn(world, random, place, 2 + random.nextInt(8));
            }
        }
    }

    private void generateCoral(World world, Random random, int blockX, int blockZ) {
        for (int patch = 0; patch < 3; patch++) {
            BlockPos floor = findSeaFloor(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (floor == null) {
                continue;
            }
            ModBlocks.CoralSet coral = randomCoral(random);
            for (int i = 0; i < 18; i++) {
                BlockPos pos = floor.add(random.nextInt(9) - 4, random.nextInt(3) - 1, random.nextInt(9) - 4);
                if (world.getBlockState(pos).getBlock() == Blocks.WATER && world.getBlockState(pos.down()).isFullBlock()) {
                    world.setBlockState(pos.down(), coral.liveBlock.getDefaultState(), 2);
                    if (random.nextBoolean() && world.getBlockState(pos).getBlock() == Blocks.WATER) {
                        world.setBlockState(pos, coral.livePlant.getDefaultState()
                            .withProperty(BlockCoralPlant.WATERLOGGED, true), 2);
                    }
                    if (random.nextInt(5) == 0 && world.getBlockState(pos).getBlock() == Blocks.WATER) {
                        world.setBlockState(pos, ModBlocks.SEA_PICKLE.getDefaultState()
                            .withProperty(BlockSeaPickle.PICKLES, random.nextInt(4) + 1)
                            .withProperty(BlockSeaPickle.WATERLOGGED, true), 2);
                    }
                }
                tryPlaceFan(world, random, pos, coral);
            }
        }
    }

    private void tryPlaceFan(World world, Random random, BlockPos pos, ModBlocks.CoralSet coral) {
        if (random.nextInt(3) != 0) {
            return;
        }
        for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.HORIZONTALS) {
            BlockPos target = pos.offset(facing);
            if (world.getBlockState(target).getBlock() == Blocks.WATER
                    && coral.liveFan.canPlaceBlockOnSide(world, target, facing)) {
                world.setBlockState(target, coral.liveFan.getDefaultState()
                    .withProperty(BlockCoralFan.FACING, facing)
                    .withProperty(BlockCoralFan.WATERLOGGED, true), 2);
                return;
            }
        }
    }

    private ModBlocks.CoralSet randomCoral(Random random) {
        List<ModBlocks.CoralSet> corals = ModBlocks.corals();
        return corals.get(random.nextInt(corals.size()));
    }

    private void generateIceberg(World world, Random random, int blockX, int blockZ, int x, int z) {
        BlockPos surface = new BlockPos(x, world.getSeaLevel(), z);
        if (world.getBlockState(surface).getMaterial() != Material.WATER
                && world.getBlockState(surface.down()).getMaterial() != Material.WATER) {
            return;
        }

        int height = random.nextInt(6) + 6;
        if (random.nextInt(9) == 0) {
            height += 8 + random.nextInt(10);
        }
        int below = Math.min(12 + random.nextInt(7), height + 8);
        int width = 5 + random.nextInt(5);
        boolean ellipse = random.nextBoolean();
        boolean blue = random.nextInt(5) == 0;

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
                    double xScale = ellipse ? 1.45D : 1.0D;
                    double zScale = ellipse ? 0.8D : 1.0D;
                    double distance = (dx * dx) / (radius * radius * xScale)
                        + (dz * dz) / (radius * radius * zScale);
                    if (distance > 1.0D || random.nextDouble() < distance * 0.11D) {
                        continue;
                    }

                    BlockPos pos = surface.add(dx, y, dz);
                    if (isInsideChunk(pos, blockX, blockZ) && canReplaceForIceberg(world, pos)) {
                        if (blue && y <= 0 && random.nextInt(18) == 0) {
                            world.setBlockState(pos, ModBlocks.BLUE_ICE.getDefaultState(), 2);
                        } else {
                            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        addIcebergSnow(world, random, blockX, blockZ, surface, width + 2, height);
        growBlueIce(world, random, blockX, blockZ, surface, width + 2, below);
    }

    private void addIcebergSnow(World world, Random random, int blockX, int blockZ, BlockPos surface, int radius,
            int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = height + 1; y >= 0; y--) {
                    BlockPos pos = surface.add(dx, y, dz);
                    if (isInsideChunk(pos, blockX, blockZ)
                            && world.getBlockState(pos).getBlock() == Blocks.PACKED_ICE && world.isAirBlock(pos.up())
                            && random.nextInt(3) != 0) {
                        world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState(), 2);
                        break;
                    }
                }
            }
        }
    }

    private void growBlueIce(World world, Random random, int blockX, int blockZ, BlockPos surface, int radius,
            int below) {
        for (int i = 0; i < 28; i++) {
            BlockPos pos = surface.add(random.nextInt(radius * 2 + 1) - radius,
                -random.nextInt(Math.max(2, below)), random.nextInt(radius * 2 + 1) - radius);
            if (isInsideChunk(pos, blockX, blockZ) && world.getBlockState(pos).getBlock() == Blocks.PACKED_ICE
                    && touchesBlueIce(world, pos)) {
                world.setBlockState(pos, ModBlocks.BLUE_ICE.getDefaultState(), 2);
            }
        }
    }

    private boolean touchesBlueIce(World world, BlockPos pos) {
        for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.values()) {
            if (world.getBlockState(pos.offset(facing)).getBlock() == ModBlocks.BLUE_ICE) {
                return true;
            }
        }
        return false;
    }

    private boolean canReplaceForIceberg(World world, BlockPos pos) {
        return world.isAirBlock(pos)
            || world.getBlockState(pos).getMaterial() == Material.WATER
            || world.getBlockState(pos).getBlock() == Blocks.ICE
            || world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER;
    }

    private boolean isInsideChunk(BlockPos pos, int blockX, int blockZ) {
        return pos.getX() >= blockX && pos.getX() < blockX + 16 && pos.getZ() >= blockZ && pos.getZ() < blockZ + 16;
    }

    private BlockPos findSeaFloor(World world, int x, int z) {
        BlockPos pos = new BlockPos(x, Math.max(1, world.getSeaLevel() - 1), z);
        while (pos.getY() > 2 && world.getBlockState(pos).getBlock() == Blocks.WATER) {
            pos = pos.down();
        }
        if (world.getBlockState(pos.up()).getBlock() != Blocks.WATER) {
            return null;
        }
        return pos;
    }

    private boolean isWarmOceanLike(Biome biome) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return name.contains("warm") || name.contains("lukewarm") || name.contains("tropical")
            || name.contains("coral")
            || (!isFrozenOceanLike(biome) && name.contains("ocean") && biome.getDefaultTemperature() >= 0.8F);
    }

    private boolean isFrozenOceanLike(Biome biome) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        return name.contains("frozen") || name.contains("ice") || name.contains("glacier")
            || (name.contains("ocean") && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)
            || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY) || biome.getDefaultTemperature() <= 0.15F));
    }
}
