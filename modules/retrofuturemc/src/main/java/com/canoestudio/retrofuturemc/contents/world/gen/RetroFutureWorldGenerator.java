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
import com.yungnickyoung.minecraft.bettercaves.api.BetterCavesAPI;
import com.yungnickyoung.minecraft.bettercaves.api.BetterCavesCaveBiomeType;
import com.yungnickyoung.minecraft.bettercaves.api.BetterCavesCaveDecorationContext;
import com.yungnickyoung.minecraft.bettercaves.api.BetterCavesCaveDecorator;
import com.yungnickyoung.minecraft.bettercaves.api.BetterCavesCaveSample;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
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

import java.util.Random;

public class RetroFutureWorldGenerator implements IWorldGenerator, BetterCavesCaveDecorator {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
    private static final IBlockState DEEPSLATE = ModBlocks.DeepSlate.getDefaultState();
    private static boolean betterCavesDecoratorRegistered;

    public static void registerBetterCavesDecorator() {
        if (!betterCavesDecoratorRegistered) {
            BetterCavesAPI.registerCaveDecorator(new RetroFutureWorldGenerator());
            betterCavesDecoratorRegistered = true;
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        generateOres(world, random, blockX, blockZ);

        if (random.nextInt(24) == 0) {
            generateAmethystGeode(world, random, new BlockPos(blockX + random.nextInt(16), 6 + random.nextInt(25), blockZ + random.nextInt(16)));
        }

        decorateCavesInWorld(world, random, blockX, blockZ);
        spawnAquaticCaveMobs(world, random, blockX, blockZ);
    }

    @Override
    public void decorate(BetterCavesCaveDecorationContext context) {
        World world = context.getWorld();
        Random random = new Random(world.getSeed() ^ (long)context.getChunkX() * 341873128712L ^ (long)context.getChunkZ() * 132897987541L);

        for (int i = 0; i < 22; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = 8 + random.nextInt(56);
            BetterCavesCaveSample sample = context.sampleLocal(x, y, z);
            if (!sample.isOpen()) {
                continue;
            }

            if (sample.getCaveBiomeType() == BetterCavesCaveBiomeType.LUSH) {
                decorateLushPrimer(context, random, x, y, z);
            } else if (sample.getCaveBiomeType() == BetterCavesCaveBiomeType.DRIPSTONE) {
                decorateDripstonePrimer(context, random, x, y, z);
            } else if (random.nextInt(5) == 0) {
                placeGlowLichenPrimer(context, random, x, y, z);
            }
        }
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

    private boolean generateAmethystGeode(World world, Random random, BlockPos center) {
        if (!isNaturalStone(world.getBlockState(center))) {
            return false;
        }

        int radius = 4 + random.nextInt(2);
        boolean crack = random.nextFloat() < 0.35F;
        EnumFacing crackFace = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];

        for (int x = -radius - 2; x <= radius + 2; x++) {
            for (int y = -radius - 2; y <= radius + 2; y++) {
                for (int z = -radius - 2; z <= radius + 2; z++) {
                    BlockPos pos = center.add(x, y, z);
                    double distance = Math.sqrt(x * x + y * y + z * z) + random.nextDouble() * 0.55D;
                    boolean inCrack = crack && isInCrack(x, y, z, crackFace, radius);

                    if (distance <= radius - 1.7D || inCrack && distance <= radius + 1.1D) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    } else if (distance <= radius - 0.7D) {
                        world.setBlockState(pos, random.nextFloat() < 0.18F ? ModBlocks.BUDDING_AMETHYST.getDefaultState() : ModBlocks.AMETHYST_BLOCK.getDefaultState(), 2);
                    } else if (distance <= radius + 0.25D) {
                        world.setBlockState(pos, ModBlocks.CALCITE.getDefaultState(), 2);
                    } else if (distance <= radius + 1.2D) {
                        world.setBlockState(pos, ModBlocks.SMOOTH_BASALT.getDefaultState(), 2);
                    }
                }
            }
        }

        placeAmethystBuds(world, random, center, radius);
        return true;
    }

    private boolean isInCrack(int x, int y, int z, EnumFacing face, int radius) {
        int along = face.getAxis() == EnumFacing.Axis.X ? x * face.getXOffset() : z * face.getZOffset();
        int across = face.getAxis() == EnumFacing.Axis.X ? Math.abs(z) : Math.abs(x);
        return along > radius - 2 && across <= 1 && y >= -2 && y <= 2;
    }

    private void placeAmethystBuds(World world, Random random, BlockPos center, int radius) {
        for (int i = 0; i < 28; i++) {
            BlockPos pos = center.add(random.nextInt(radius * 2 + 1) - radius, random.nextInt(radius * 2 + 1) - radius, random.nextInt(radius * 2 + 1) - radius);
            if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
                continue;
            }
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos support = pos.offset(facing.getOpposite());
                if (world.getBlockState(support).getBlock() == ModBlocks.BUDDING_AMETHYST && random.nextInt(3) == 0) {
                    Block block = random.nextInt(4) == 0 ? ModBlocks.AMETHYST_CLUSTER : random.nextBoolean() ? ModBlocks.LARGE_AMETHYST_BUD : ModBlocks.MEDIUM_AMETHYST_BUD;
                    world.setBlockState(pos, block.getDefaultState().withProperty(AmethystClusterBlock.FACING, facing), 2);
                    break;
                }
            }
        }
    }

    private void decorateCavesInWorld(World world, Random random, int blockX, int blockZ) {
        for (int i = 0; i < 28; i++) {
            BlockPos pos = new BlockPos(blockX + random.nextInt(16), 8 + random.nextInt(58), blockZ + random.nextInt(16));
            if (!world.isAirBlock(pos)) {
                continue;
            }

            CaveStyle style = classifyCaveStyle(world, pos);
            if (style == CaveStyle.LUSH) {
                decorateLushWorld(world, random, pos);
            } else if (style == CaveStyle.DRIPSTONE) {
                decorateDripstoneWorld(world, random, pos);
            } else if (random.nextInt(4) == 0) {
                placeGlowLichenWorld(world, random, pos);
            }
        }
    }

    private CaveStyle classifyCaveStyle(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        boolean wet = biome.getRainfall() >= 0.8F
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP);
        double region = smoothNoise(world.getSeed() ^ 0x4C555348L, pos.getX() * 0.018D, 0.0D, pos.getZ() * 0.018D);
        double detail = smoothNoise(world.getSeed() ^ 0x44524950L, pos.getX() * 0.025D, pos.getY() * 0.03D, pos.getZ() * 0.025D);

        if (wet && region + detail * 0.25D > 0.12D && pos.getY() > 10 && pos.getY() < 70) {
            return CaveStyle.LUSH;
        }
        if (region * 0.35D - detail > 0.18D && pos.getY() < 74) {
            return CaveStyle.DRIPSTONE;
        }
        return CaveStyle.NORMAL;
    }

    private void decorateLushWorld(World world, Random random, BlockPos pos) {
        BlockPos floor = scan(world, pos, EnumFacing.DOWN, 10);
        BlockPos ceiling = scan(world, pos, EnumFacing.UP, 10);
        if (floor != null) {
            placeMossPatchWorld(world, random, floor.up(), 3 + random.nextInt(4));
            if (random.nextInt(3) == 0) {
                placeClayAndDripleafWorld(world, random, floor.up());
            }
        }
        if (ceiling != null) {
            if (random.nextInt(3) == 0) {
                placeCaveVineWorld(world, random, ceiling.down());
            }
            if (random.nextInt(8) == 0 && world.isAirBlock(ceiling.down())) {
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
                if (world.isAirBlock(top) && isNaturalStone(world.getBlockState(ground))) {
                    world.setBlockState(ground, ModBlocks.MOSS_BLOCK.getDefaultState(), 2);
                    if (random.nextInt(5) == 0) {
                        world.setBlockState(top, ModBlocks.MOSS_CARPET.getDefaultState(), 2);
                    } else if (random.nextInt(8) == 0) {
                        world.setBlockState(top, (random.nextBoolean() ? ModBlocks.Azalea : ModBlocks.Flowering_Azalea).getDefaultState(), 2);
                    }
                }
            }
        }
    }

    private void placeClayAndDripleafWorld(World world, Random random, BlockPos center) {
        for (int i = 0; i < 12; i++) {
            BlockPos pos = center.add(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            if (world.isAirBlock(pos) && isNaturalStone(world.getBlockState(pos.down()))) {
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
            if (random.nextBoolean()) {
                BlockPos support = pos.offset(facing.getOpposite());
                if (world.isAirBlock(pos) && world.getBlockState(support).isSideSolid(world, support, facing)) {
                    world.setBlockState(pos, ModBlocks.GLOW_LICHEN.getDefaultState()
                            .withProperty(GlowLichenBlock.UP, facing == EnumFacing.UP)
                            .withProperty(GlowLichenBlock.DOWN, facing == EnumFacing.DOWN)
                            .withProperty(GlowLichenBlock.NORTH, facing == EnumFacing.NORTH)
                            .withProperty(GlowLichenBlock.EAST, facing == EnumFacing.EAST)
                            .withProperty(GlowLichenBlock.SOUTH, facing == EnumFacing.SOUTH)
                            .withProperty(GlowLichenBlock.WEST, facing == EnumFacing.WEST), 2);
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

    private void decorateLushPrimer(BetterCavesCaveDecorationContext context, Random random, int x, int y, int z) {
        int floor = scanPrimer(context, x, y, z, EnumFacing.DOWN, 10);
        int ceiling = scanPrimer(context, x, y, z, EnumFacing.UP, 10);
        if (floor >= 0) {
            for (int i = 0; i < 12; i++) {
                int px = Math.max(0, Math.min(15, x + random.nextInt(7) - 3));
                int pz = Math.max(0, Math.min(15, z + random.nextInt(7) - 3));
                if (isAir(context.getBlockState(px, floor + 1, pz)) && isNaturalStone(context.getBlockState(px, floor, pz))) {
                    context.setBlockState(px, floor, pz, ModBlocks.MOSS_BLOCK.getDefaultState());
                    if (random.nextInt(4) == 0 && floor + 1 < 255) {
                        context.setBlockState(px, floor + 1, pz, ModBlocks.MOSS_CARPET.getDefaultState());
                    }
                }
            }
        }
        if (ceiling >= 0 && random.nextBoolean()) {
            int length = 1 + random.nextInt(5);
            for (int i = 1; i <= length && ceiling - i > 1; i++) {
                if (!isAir(context.getBlockState(x, ceiling - i, z))) {
                    break;
                }
                boolean berries = random.nextInt(5) == 0;
                context.setBlockState(x, ceiling - i, z, (i == length ? ModBlocks.CAVE_VINE : ModBlocks.CAVE_VINE_PLANT).getDefaultState().withProperty(CaveVinePlant.BERRIES, berries));
            }
        }
    }

    private void decorateDripstonePrimer(BetterCavesCaveDecorationContext context, Random random, int x, int y, int z) {
        int floor = scanPrimer(context, x, y, z, EnumFacing.DOWN, 12);
        int ceiling = scanPrimer(context, x, y, z, EnumFacing.UP, 12);
        if (floor >= 0) {
            context.setBlockState(x, floor, z, ModBlocks.DRIPSTONE_BLOCK.getDefaultState());
            if (floor + 1 < 255) {
                context.setBlockState(x, floor + 1, z, ModBlocks.POINTED_DRIPSTONE.getDefaultState().withProperty(PointedDripstoneBlock.VERTICAL_DIRECTION, EnumFacing.UP));
            }
        }
        if (ceiling >= 0) {
            context.setBlockState(x, ceiling, z, ModBlocks.DRIPSTONE_BLOCK.getDefaultState());
            if (ceiling - 1 > 0) {
                context.setBlockState(x, ceiling - 1, z, ModBlocks.POINTED_DRIPSTONE.getDefaultState().withProperty(PointedDripstoneBlock.VERTICAL_DIRECTION, EnumFacing.DOWN));
            }
        }
    }

    private void placeGlowLichenPrimer(BetterCavesCaveDecorationContext context, Random random, int x, int y, int z) {
        if (!isAir(context.getBlockState(x, y, z))) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            int sx = x - facing.getXOffset();
            int sy = y - facing.getYOffset();
            int sz = z - facing.getZOffset();
            if (sx < 0 || sx > 15 || sy < 0 || sy > 255 || sz < 0 || sz > 15) {
                continue;
            }
            if (isSolid(context.getBlockState(sx, sy, sz)) && random.nextBoolean()) {
                context.setBlockState(x, y, z, ModBlocks.GLOW_LICHEN.getDefaultState()
                        .withProperty(GlowLichenBlock.UP, facing == EnumFacing.UP)
                        .withProperty(GlowLichenBlock.DOWN, facing == EnumFacing.DOWN)
                        .withProperty(GlowLichenBlock.NORTH, facing == EnumFacing.NORTH)
                        .withProperty(GlowLichenBlock.EAST, facing == EnumFacing.EAST)
                        .withProperty(GlowLichenBlock.SOUTH, facing == EnumFacing.SOUTH)
                        .withProperty(GlowLichenBlock.WEST, facing == EnumFacing.WEST));
                return;
            }
        }
    }

    private int scanPrimer(BetterCavesCaveDecorationContext context, int x, int y, int z, EnumFacing direction, int distance) {
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

    private boolean isNaturalStone(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == ModBlocks.DeepSlate || block == ModBlocks.TUFF || block == ModBlocks.DRIPSTONE_BLOCK;
    }

    private boolean isAir(IBlockState state) {
        return state.getBlock() == Blocks.AIR;
    }

    private boolean isSolid(IBlockState state) {
        return state.getMaterial().isSolid();
    }

    private void setFluidloggableWater(World world, BlockPos pos, IBlockState state, int flags) {
        FluidState fluidState = FluidState.of(Blocks.WATER.getDefaultState());
        world.setBlockState(pos, state, flags);
        FluidloggedUtils.setFluidState(world, pos, world.getBlockState(pos), fluidState, false, flags);
        world.scheduleUpdate(pos, Blocks.WATER, Blocks.WATER.tickRate(world));
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

    private enum CaveStyle {
        NORMAL,
        LUSH,
        DRIPSTONE
    }
}
