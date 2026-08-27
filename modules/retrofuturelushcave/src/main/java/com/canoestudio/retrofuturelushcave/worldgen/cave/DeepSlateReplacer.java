package com.canoestudio.retrofuturelushcave.worldgen.cave;

import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Random;

public final class DeepSlateReplacer {
    /* 1.12 适配：Y=64 及以下完全深板岩，Y=65..71 为七层递减过渡。 */
    private static final int FULL_DEEPSLATE_MAX_Y = 64;
    private static final int TRANSITION_TOP_Y = 71;

    private static final long COPPER_SALT = 0x434F505045525F31L;
    private static final int COPPER_MIN_Y = 48;   // router -16 -> primer 48
    private static final int COPPER_MAX_Y = 176;  // router 112 -> primer 176

    /**
     * LOWEST 使本处理器在多数 Populate 监听器之后执行。目标 chunk 此时已生成且已加载；
     * 通过 Chunk#setBlockState 直写而不向 World 分派邻居/流体通知。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPopulatePost(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = originX + localX;
                int z = originZ + localZ;
                for (int y = 0; y <= TRANSITION_TOP_Y; y++) {
                    if (y > FULL_DEEPSLATE_MAX_Y && !shouldBecomeDeepslate(world.getSeed(), x, y, z)) {
                        continue;
                    }
                    pos.setPos(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    IBlockState replacement = getReplacement(block);

                    if (replacement != null) {
                        chunk.setBlockState(pos, replacement);
                    }
                }
            }
        }

        Random random = new Random(mixSeed(world.getSeed(), chunkX, chunkZ));

        /* 原版：small size=10与large size=20均为count 16、triangle -16..112。 */
        for (int i = 0; i < 16; i++) {
            placeVein(chunk, chunkX << 4, chunkZ << 4, random, 10);
        }
        for (int i = 0; i < 16; i++) {
            placeVein(chunk, chunkX << 4, chunkZ << 4, random, 20);
        }
    }


    @Nullable
    private static IBlockState getReplacement(Block block) {
        IBlockState replacement = null;
        if (block == Blocks.STONE) {
            replacement = ModBlocks.DeepSlate.getDefaultState();
        } else if (block == Blocks.IRON_ORE) {
            replacement = ModBlocks.DEEPSLATE_IRON_ORE.getDefaultState();
        } else if (block == Blocks.GOLD_ORE) {
            replacement = ModBlocks.DEEPSLATE_GOLD_ORE.getDefaultState();
        } else if (block == Blocks.DIAMOND_ORE) {
            replacement = ModBlocks.DEEPSLATE_DIAMOND_ORE.getDefaultState();
        } else if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) {
            replacement = ModBlocks.DEEPSLATE_REDSTONE_ORE.getDefaultState();
        } else if (block == Blocks.LAPIS_ORE) {
            replacement = ModBlocks.DEEPSLATE_LAPIS_ORE.getDefaultState();
        } else if (block == Blocks.EMERALD_ORE) {
            replacement = ModBlocks.DEEPSLATE_EMERALD_ORE.getDefaultState();
        } else if (block == Blocks.COAL_ORE) {
            replacement = ModBlocks.DEEPSLATE_COAL_ORE.getDefaultState();
        }
        return replacement;
    }

    private static void placeVein(Chunk chunk, int baseX, int baseZ, Random random, int size) {
        int centerX = baseX + random.nextInt(16);
        int centerZ = baseZ + random.nextInt(16);
        int centerY = triangularY(random, COPPER_MIN_Y, COPPER_MAX_Y);
        double angle = random.nextDouble() * Math.PI;
        double halfLength = size / 8.0D;
        double x0 = centerX + Math.sin(angle) * halfLength;
        double x1 = centerX - Math.sin(angle) * halfLength;
        double z0 = centerZ + Math.cos(angle) * halfLength;
        double z1 = centerZ - Math.cos(angle) * halfLength;
        double y0 = centerY + random.nextInt(3) - 2;
        double y1 = centerY + random.nextInt(3) - 2;

        for (int step = 0; step < size; step++) {
            double t = (double) step / (double) size;
            double x = lerp(t, x0, x1);
            double y = lerp(t, y0, y1);
            double z = lerp(t, z0, z1);
            double radius = (Math.sin(Math.PI * t) + 1.0D) * random.nextDouble() * 0.55D + 0.65D;
            int minX = (int) Math.floor(x - radius);
            int maxX = (int) Math.floor(x + radius);
            int minY = Math.max(0, (int) Math.floor(y - radius));
            int maxY = Math.min(255, (int) Math.floor(y + radius));
            int minZ = (int) Math.floor(z - radius);
            int maxZ = (int) Math.floor(z + radius);
            for (int blockX = minX; blockX <= maxX; blockX++) {
                for (int blockY = minY; blockY <= maxY; blockY++) {
                    for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                        if (blockX < baseX || blockX >= baseX + 16 || blockZ < baseZ || blockZ >= baseZ + 16) {
                            continue;
                        }
                        double dx = ((double) blockX + 0.5D - x) / radius;
                        double dy = ((double) blockY + 0.5D - y) / radius;
                        double dz = ((double) blockZ + 0.5D - z) / radius;
                        if (dx * dx + dy * dy + dz * dz >= 1.0D) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                        IBlockState existing = chunk.getBlockState(pos);
                                                if (existing.getBlock() == Blocks.STONE) {
                            chunk.setBlockState(pos, blockY <= FULL_DEEPSLATE_MAX_Y
                                    ? ModBlocks.DEEPSLATE_COPPER_ORE.getDefaultState()
                                    : ModBlocks.COPPER_ORE.getDefaultState());

                        } else if (existing.getBlock() == ModBlocks.DeepSlate) {
                            chunk.setBlockState(pos, ModBlocks.DEEPSLATE_COPPER_ORE.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    private static int triangularY(Random random, int min, int max) {
        return min + (random.nextInt(max - min + 1) + random.nextInt(max - min + 1)) / 2;
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static long mixSeed(long seed, int chunkX, int chunkZ) {
        long value = seed ^ COPPER_SALT;
        value ^= (long) chunkX * 341873128712L;
        value ^= (long) chunkZ * 132897987541L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return value;
    }

    /**
          * Y=64及以下总是转换；Y=65的转换概率为7/8，逐层下降，Y=71为1/8。

     * 使用坐标哈希而不是 World#rand，以确保区块生成顺序不会改变过渡图案。
     */
    private static boolean shouldBecomeDeepslate(long worldSeed, int x, int y, int z) {
        long value = worldSeed;
        value ^= (long) x * 341873128712L;
        value ^= (long) z * 132897987541L;
        value ^= (long) y * 42317861L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        int bucket = (int) (value & 7L);
        return bucket < TRANSITION_TOP_Y + 1 - y;
    }
}