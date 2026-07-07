package com.canoestudio.retrofuturethewildupdate.world.gen;

import com.canoestudio.retrofuturelushcavecore.api.world.RetroAncientCityPlacement;
import com.canoestudio.retrofuturelushcavecore.api.world.RetroStructurePlacement;
import com.canoestudio.retrofuturelushcavecore.api.world.RetroWorldgenRegistry;
import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.BlockSculkVein;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.entity.EntityFrog;
import com.canoestudio.retrofuturethewildupdate.entity.EntityTadpole;
import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import com.canoestudio.retrofuturethewildupdate.world.biome.ModBiomes;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WildUpdateWorldGenerator implements IWorldGenerator {

    private static final int SEA_LEVEL_BIAS = 63;
    private static final long MANGROVE_SALT = 0x525457554D414E47L;
    private static final long ANCIENT_CITY_SALT = 0x5254575543495459L;
    private static final RetroStructurePlacement ANCIENT_CITY_PLACEMENT = RetroAncientCityPlacement
        .builder(new ResourceLocation(RTWU.ID, "ancient_city"))
        .salt(ANCIENT_CITY_SALT)
        .biome(WildUpdateWorldGenerator::isDeepDarkCandidate)
        .build();

    private final WorldGenMangroveTree mangroveTree = new WorldGenMangroveTree(true);

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;
        Random chunkRandom = new Random(chunkSeed(world, chunkX, chunkZ, MANGROVE_SALT));
        decorateMangrove(world, chunkRandom, blockX, blockZ);

        Random cityRandom = ANCIENT_CITY_PLACEMENT.createChunkRandom(world, chunkX, chunkZ);
        if (ANCIENT_CITY_PLACEMENT.shouldStartAt(world, chunkX, chunkZ)) {
            generateAncientCityRuin(world, cityRandom, blockX, blockZ);
        } else if (cityRandom.nextInt(24) == 0) {
            generateDeepDarkPatch(world, cityRandom, blockX, blockZ);
        }
    }

    private void decorateMangrove(World world, Random random, int blockX, int blockZ) {
        Biome biome = world.getBiome(new BlockPos(blockX + 8, 0, blockZ + 8));
        if (!isMangroveCandidate(biome)) {
            return;
        }

        boolean dedicatedBiome = biome == ModBiomes.MANGROVE_SWAMP;
        int mudPatches = dedicatedBiome ? 10 : 4;
        int treeAttempts = dedicatedBiome ? 5 : 2;
        int waterLifeAttempts = dedicatedBiome ? 5 : 2;

        for (int i = 0; i < mudPatches; i++) {
            BlockPos surface = findSurface(world, blockX + random.nextInt(16), blockZ + random.nextInt(16));
            if (surface != null && surface.getY() <= SEA_LEVEL_BIAS + 4 && hasNearbyWater(world, surface, 4)) {
                replaceMudPatch(world, random, surface, blockX, blockZ, dedicatedBiome ? 4 : 3);
            }
        }

        for (int i = 0; i < treeAttempts; i++) {
            BlockPos surface = findSurface(world, blockX + 3 + random.nextInt(10), blockZ + 3 + random.nextInt(10));
            if (surface != null && surface.getY() <= SEA_LEVEL_BIAS + 5 && hasNearbyWater(world, surface, 5)) {
                if (isSoftGround(world.getBlockState(surface))) {
                    world.setBlockState(surface, ModBlocks.MUD.getDefaultState(), 2);
                }
                this.mangroveTree.generate(world, random, surface.up());
            }
        }

        for (int i = 0; i < waterLifeAttempts; i++) {
            BlockPos water = findShallowWater(world, random, blockX, blockZ);
            if (water != null) {
                if (random.nextBoolean()) {
                    spawnTadpoles(world, random, water);
                } else {
                    placeFrogspawn(world, random, water);
                }
                if (dedicatedBiome || random.nextInt(3) == 0) {
                    spawnFrog(world, random, findNearbyLand(world, water));
                }
            }
        }
    }

    private boolean isMangroveCandidate(Biome biome) {
        if (biome == ModBiomes.MANGROVE_SWAMP || biome == Biomes.SWAMPLAND || biome == Biomes.MUTATED_SWAMPLAND) {
            return true;
        }
        return RetroWorldgenRegistry.hasAllTypes(biome, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WET)
            && biome.getDefaultTemperature() >= 0.65F;
    }

    private static boolean isDeepDarkCandidate(Biome biome) {
        return biome != null && !RetroWorldgenRegistry.hasAnyType(biome,
            BiomeDictionary.Type.OCEAN,
            BiomeDictionary.Type.RIVER,
            BiomeDictionary.Type.BEACH,
            BiomeDictionary.Type.NETHER,
            BiomeDictionary.Type.END);
    }

    private void replaceMudPatch(World world, Random random, BlockPos center, int blockX, int blockZ, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius + random.nextInt(4)) {
                    continue;
                }
                BlockPos pos = center.add(x, 0, z);
                if (!isInsideChunk(pos, blockX, blockZ)) {
                    continue;
                }
                BlockPos surface = findSurface(world, pos.getX(), pos.getZ());
                if (surface != null && Math.abs(surface.getY() - center.getY()) <= 2
                    && isSoftGround(world.getBlockState(surface)) && hasNearbyWater(world, surface, 3)) {
                    world.setBlockState(surface, ModBlocks.MUD.getDefaultState(), 2);
                }
            }
        }
    }

    private BlockPos findShallowWater(World world, Random random, int blockX, int blockZ) {
        for (int i = 0; i < 16; i++) {
            int x = blockX + random.nextInt(16);
            int z = blockZ + random.nextInt(16);
            for (int y = SEA_LEVEL_BIAS + 3; y >= SEA_LEVEL_BIAS - 5; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).getMaterial() == Material.WATER
                    && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private void spawnTadpoles(World world, Random random, BlockPos water) {
        if (world.countEntities(net.minecraft.entity.EnumCreatureType.WATER_CREATURE, false) > 55) {
            return;
        }
        int count = 2 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            EntityTadpole tadpole = new EntityTadpole(world);
            tadpole.setGrowingAgeTicks(random.nextInt(4000));
            tadpole.setLocationAndAngles(water.getX() + 0.2D + random.nextDouble() * 0.6D,
                water.getY() + 0.1D,
                water.getZ() + 0.2D + random.nextDouble() * 0.6D,
                random.nextFloat() * 360.0F,
                0.0F);
            if (tadpole.getCanSpawnHere()) {
                world.spawnEntity(tadpole);
            }
        }
    }

    private void placeFrogspawn(World world, Random random, BlockPos water) {
        BlockPos spawnPos = water.up();
        if (world.isAirBlock(spawnPos) && random.nextInt(3) != 0 && ModBlocks.FROGSPAWN.canPlaceBlockAt(world, spawnPos)) {
            world.setBlockState(spawnPos, ModBlocks.FROGSPAWN.getDefaultState(), 3);
            world.scheduleUpdate(spawnPos, ModBlocks.FROGSPAWN, 3600 + random.nextInt(8401));
        }
    }

    private void spawnFrog(World world, Random random, BlockPos land) {
        if (land == null || world.countEntities(net.minecraft.entity.EnumCreatureType.CREATURE, false) > 70) {
            return;
        }
        EntityFrog frog = new EntityFrog(world);
        frog.setVariant(EntityFrog.variantForBiome(world.getBiome(land)));
        frog.setLocationAndAngles(land.getX() + 0.5D, land.getY(), land.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (frog.getCanSpawnHere()) {
            world.spawnEntity(frog);
        }
    }

    private BlockPos findNearbyLand(World world, BlockPos water) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos land = water.offset(facing);
            if (world.getBlockState(land).getMaterial() != Material.WATER
                && world.getBlockState(land).isSideSolid(world, land, EnumFacing.UP)
                && world.isAirBlock(land.up())) {
                return land;
            }
        }
        return null;
    }

    private void generateDeepDarkPatch(World world, Random random, int blockX, int blockZ) {
        BlockPos center = findDeepStone(world, random, blockX, blockZ, 8);
        if (center == null) {
            return;
        }

        for (int i = 0; i < 42; i++) {
            BlockPos pos = center.add(random.nextInt(13) - 6, random.nextInt(5) - 2, random.nextInt(13) - 6);
            if (!isInsideChunk(pos, blockX, blockZ) || !isNaturalDeepBlock(world.getBlockState(pos))) {
                continue;
            }
            world.setBlockState(pos, ModBlocks.SCULK.getDefaultState(), 2);
            if (random.nextInt(7) == 0 && canPlaceSculkGrowth(world, pos.up())) {
                placeSculkGrowth(world, random, pos.up());
            }
        }
    }

    private void generateAncientCityRuin(World world, Random random, int blockX, int blockZ) {
        BlockPos center = findDeepStone(world, random, blockX, blockZ, 18);
        if (center == null || center.getY() > 36) {
            return;
        }

        int floorY = center.getY() - 2;
        carveRoom(world, random, center, blockX, blockZ);
        buildAncientCityFrame(world, center, blockX, blockZ);
        scatterSculk(world, random, new BlockPos(center.getX(), floorY + 1, center.getZ()), blockX, blockZ);
        placeAncientLoot(world, random, new BlockPos(center.getX() + 4, floorY + 1, center.getZ() + 4), blockX, blockZ);
    }

    private void carveRoom(World world, Random random, BlockPos center, int blockX, int blockZ) {
        int floorY = center.getY() - 2;
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                for (int y = -2; y <= 5; y++) {
                    BlockPos pos = center.add(x, y, z);
                    if (!isInsideChunk(pos, blockX, blockZ)) {
                        continue;
                    }

                    boolean edge = Math.abs(x) == 7 || Math.abs(z) == 7 || y == 5;
                    boolean floor = pos.getY() == floorY;
                    if (floor) {
                        world.setBlockState(pos, random.nextInt(6) == 0
                            ? com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
                            : com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DEEPSLATE_BRICKS.getDefaultState(), 2);
                    } else if (edge && random.nextInt(5) != 0) {
                        world.setBlockState(pos, random.nextBoolean()
                            ? com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DEEPSLATE_TILES.getDefaultState()
                            : com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DEEPSLATE_BRICKS.getDefaultState(), 2);
                    } else if (pos.getY() > floorY) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            for (int i = 1; i <= 6; i++) {
                BlockPos pos = center.offset(facing, i).down(2);
                if (isInsideChunk(pos, blockX, blockZ)) {
                    world.setBlockState(pos, com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                }
            }
        }
    }

    private void buildAncientCityFrame(World world, BlockPos center, int blockX, int blockZ) {
        int floorY = center.getY() - 1;
        for (int y = 0; y <= 4; y++) {
            for (int x = -2; x <= 2; x++) {
                if (Math.abs(x) == 2 || y == 4) {
                    BlockPos pos = new BlockPos(center.getX() + x, floorY + y, center.getZ() - 5);
                    if (isInsideChunk(pos, blockX, blockZ)) {
                        world.setBlockState(pos, ModBlocks.REINFORCED_DEEPSLATE.getDefaultState(), 2);
                    }
                }
            }
        }

        BlockPos chiseled = new BlockPos(center.getX(), floorY, center.getZ() - 5);
        if (isInsideChunk(chiseled, blockX, blockZ)) {
            world.setBlockState(chiseled, com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.CHISELED_DEEPSLATE.getDefaultState(), 2);
        }
    }

    private void scatterSculk(World world, Random random, BlockPos center, int blockX, int blockZ) {
        for (int i = 0; i < 64; i++) {
            BlockPos pos = center.add(random.nextInt(13) - 6, random.nextInt(3) - 1, random.nextInt(13) - 6);
            if (!isInsideChunk(pos, blockX, blockZ)) {
                continue;
            }
            BlockPos ground = pos.down();
            if (canPlaceSculkGrowth(world, pos)) {
                placeSculkGrowth(world, random, pos);
                if (random.nextInt(3) == 0) {
                    world.setBlockState(ground, ModBlocks.SCULK.getDefaultState(), 2);
                }
            } else if (isNaturalDeepBlock(world.getBlockState(pos)) && random.nextInt(4) == 0) {
                world.setBlockState(pos, ModBlocks.SCULK.getDefaultState(), 2);
            }
        }
    }

    private void placeSculkGrowth(World world, Random random, BlockPos pos) {
        if (!canPlaceSculkGrowth(world, pos)) {
            return;
        }
        int roll = random.nextInt(18);
        if (roll == 0) {
            world.setBlockState(pos, ModBlocks.SCULK_SHRIEKER.getDefaultState(), 3);
        } else if (roll <= 2) {
            world.setBlockState(pos, ModBlocks.SCULK_SENSOR.getDefaultState(), 3);
        } else if (roll == 3) {
            world.setBlockState(pos, ModBlocks.SCULK_CATALYST.getDefaultState(), 3);
        } else {
            world.setBlockState(pos, ModBlocks.SCULK_VEIN.getDefaultState().withProperty(BlockSculkVein.DOWN, true), 2);
        }
    }

    private boolean canPlaceSculkGrowth(World world, BlockPos pos) {
        return world.isAirBlock(pos) && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    private void placeAncientLoot(World world, Random random, BlockPos pos, int blockX, int blockZ) {
        if (!isInsideChunk(pos, blockX, blockZ) || !world.isAirBlock(pos) || !world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
            return;
        }

        world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 3);
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityChest)) {
            return;
        }

        TileEntityChest chest = (TileEntityChest) tile;
        chest.setInventorySlotContents(4, new ItemStack(ModItems.ECHO_SHARD, 1 + random.nextInt(3)));
        if (random.nextBoolean()) {
            chest.setInventorySlotContents(10, new ItemStack(ModItems.DISC_FRAGMENT_5, 1 + random.nextInt(2)));
        }
        if (random.nextInt(4) == 0) {
            chest.setInventorySlotContents(13, new ItemStack(ModItems.RECOVERY_COMPASS));
        }
        chest.setInventorySlotContents(16, new ItemStack(Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(5)));
        chest.setInventorySlotContents(22, new ItemStack(Items.BONE, 4 + random.nextInt(8)));
    }

    private BlockPos findDeepStone(World world, Random random, int blockX, int blockZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            BlockPos pos = new BlockPos(blockX + 4 + random.nextInt(8), 8 + random.nextInt(28), blockZ + 4 + random.nextInt(8));
            if (isNaturalDeepBlock(world.getBlockState(pos)) && isNaturalDeepBlock(world.getBlockState(pos.down()))) {
                return pos;
            }
        }
        return null;
    }

    private BlockPos findSurface(World world, int x, int z) {
        for (int y = Math.min(world.getActualHeight() - 2, 96); y > 36; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.WATER && state.getMaterial() != Material.LAVA) {
                return pos;
            }
        }
        return null;
    }

    private boolean hasNearbyWater(World world, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (world.getBlockState(center.add(x, y, z)).getMaterial() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSoftGround(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS
            || block == Blocks.DIRT
            || block == Blocks.CLAY
            || block == Blocks.SAND
            || block == ModBlocks.MUD
            || state.getMaterial() == Material.GROUND;
    }

    private static boolean isNaturalDeepBlock(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE
            || block == com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DeepSlate
            || block == com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.TUFF
            || block == com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DEEPSLATE_BRICKS
            || block == com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks.DEEPSLATE_TILES;
    }

    private static boolean isInsideChunk(BlockPos pos, int blockX, int blockZ) {
        return pos.getX() >= blockX
            && pos.getX() < blockX + 16
            && pos.getZ() >= blockZ
            && pos.getZ() < blockZ + 16
            && pos.getY() > 1
            && pos.getY() < 255;
    }

    private static long chunkSeed(World world, int chunkX, int chunkZ, long salt) {
        return world.getSeed() ^ (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ salt;
    }
}
