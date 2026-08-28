package com.canoestudio.retrofuturelushcave.worldgen.lushcave;

import com.canoestudio.retrofuturelushcave.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturelushcave.contents.mobs.glowsquid.EntityGlowSquid;
import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;
import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * 1.12没有垂直Biome SpawnSettings；因此在严格LUSH三维区域的实际地下水体中一次性生成水生生物。
 * 仅对新Populate区块执行，不改写方块，也不会改变其他Biome的原版生成表。
 */
public final class LushCaveAquaticSpawner {
    private static final long AXOLOTL_SALT = 0x41584F4C4F544C31L;
    private static final long GLOW_SQUID_SALT = 0x474C4F5753515544L;
    private static final int MAX_WATER_SEARCHES = 24;

    private static long cachedSeed = Long.MIN_VALUE;
    private static DensityCave118Generator generator;
    private static UndergroundRegionSelector regions;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPopulatePost(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0) {
            return;
        }
        ensureGenerator(world.getSeed());
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        Random axolotlRandom = new Random(mixSeed(world.getSeed(), chunkX, chunkZ, AXOLOTL_SALT));
        /* 原版繁茂洞穴美西螈为成组水生生成；这里降低到每六个LUSH区块平均一次，避免Populate刷怪。 */
        if (axolotlRandom.nextInt(6) == 0) {
            BlockPos water = findLushWater(world, chunk, baseX, baseZ, axolotlRandom, 1, 127, false);
            if (water != null) {
                int group = 4 + axolotlRandom.nextInt(3);
                for (int i = 0; i < group; i++) {
                    spawnAxolotl(world, water.add(axolotlRandom.nextInt(5) - 2, 0, axolotlRandom.nextInt(5) - 2));
                }
            }
        }

        Random glowRandom = new Random(mixSeed(world.getSeed(), chunkX, chunkZ, GLOW_SQUID_SALT));
        /* 发光鱿鱼沿用用户实体的Y=9..40、液体、低亮度约束，只在LUSH水体中补充。 */
        if (glowRandom.nextInt(12) == 0) {
            BlockPos water = findLushWater(world, chunk, baseX, baseZ, glowRandom, 9, 40, true);
            if (water != null) {
                int group = 2 + glowRandom.nextInt(3);
                for (int i = 0; i < group; i++) {
                    spawnGlowSquid(world, water.add(glowRandom.nextInt(5) - 2, 0, glowRandom.nextInt(5) - 2));
                }
            }
        }
    }

    private static void ensureGenerator(long worldSeed) {
        if (generator == null || cachedSeed != worldSeed) {
            cachedSeed = worldSeed;
            generator = new DensityCave118Generator(worldSeed);
            regions = new UndergroundRegionSelector(generator);
        }
    }

    private static BlockPos findLushWater(World world, Chunk chunk, int baseX, int baseZ, Random random,
                                          int minY, int maxY, boolean requireDark) {
        for (int attempt = 0; attempt < MAX_WATER_SEARCHES; attempt++) {
            int x = baseX + random.nextInt(16);
            int y = minY + random.nextInt(maxY - minY + 1);
            int z = baseZ + random.nextInt(16);
            if (!regions.isLushAt(x, y, z) || !isWater(chunk, x, y, z)
                    || !isWater(chunk, x, y + 1, z)) {
                continue;
            }
            if (requireDark && world.getLight(new BlockPos(x, y, z)) > 1) {
                continue;
            }
            return new BlockPos(x, y, z);
        }
        return null;
    }

    private static void spawnAxolotl(World world, BlockPos pos) {
        if (!isWater(world, pos) || !isWater(world, pos.up())) {
            return;
        }
        EntityAxolotl entity = new EntityAxolotl(world);
        entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
        world.spawnEntity(entity);
    }

    private static void spawnGlowSquid(World world, BlockPos pos) {
        if (!EntityGlowSquid.canSpawnAt(world, pos, world.rand) || !isWater(world, pos.up())) {
            return;
        }
        EntityGlowSquid entity = new EntityGlowSquid(world);
        entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        world.spawnEntity(entity);
    }

    private static boolean isWater(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= 256) return false;
        return RetroWaterlogging.isWater(chunk.getWorld(), new BlockPos(x, y, z));
    }

    private static boolean isWater(World world, BlockPos pos) {
        return RetroWaterlogging.isWater(world, pos);
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
