package com.canoestudio.retrofuturelushcave.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * 在原版 {@link ChunkGeneratorOverworld} 写完基础地形和表层材料后执行的高度迁移器。
 *
 * <p>普通1.12世界没有Y&lt;0空间。为保留等价于1.18 -64..0的64格深层，原始
 * Y=0..128精确平移到Y=64..192；原始Y=129..255压缩到Y=193..255。原版海平面
 * Y=63因此迁移到Y=127。压缩区保留每列最高的源状态，使高山顶端、表层材料、地表水
 * 和露天空气不会因256高度上限而被直接截断。</p>
 *
 * <p>本类只处理Primer，绝不调用World#setBlockState，不会触发流体或邻居更新。</p>
 */
public final class VanillaTerrainMigration118 {
    public static final int SOURCE_SEA_LEVEL = 63;
    public static final int TARGET_SEA_LEVEL = 127;
    public static final int DEEP_SPACE_HEIGHT = 64;
    public static final int LINEAR_SOURCE_MAX_Y = 128;
    public static final int LINEAR_TARGET_MAX_Y = LINEAR_SOURCE_MAX_Y + DEEP_SPACE_HEIGHT;
    public static final int PRIMER_MAX_Y = 255;

    private VanillaTerrainMigration118() {
    }

    /**
     * 将已经由原版生成器写入的Primer迁移到+64布局，并返回每列迁移后最高非空气/水底的地表高度。
     */
    public static int[] migrate(ChunkPrimer primer) {
        IBlockState[][][] source = new IBlockState[16][256][16];
        for (int localX = 0; localX < 16; localX++) {
            for (int y = 0; y <= PRIMER_MAX_Y; y++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    source[localX][y][localZ] = primer.getBlockState(localX, y, localZ);
                }
            }
        }

        int[] surfaceHeights = new int[256];
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int column = (localZ << 4) | localX;

                /* 先填满新增的-64..-1等价深层；后续仅在天然岩石中雕刻洞穴。 */
                for (int y = 0; y < DEEP_SPACE_HEIGHT; y++) {
                    /* 以1.18 -64附近的基岩底床代替原版被迁移走的Y=0基岩。 */
                    primer.setBlockState(localX, y, localZ,
                            y < 5 ? Blocks.BEDROCK.getDefaultState() : Blocks.STONE.getDefaultState());
                }

                /* 低/中高度严格+64，保留原版海洋、河流、沙滩和表层材料。 */
                for (int sourceY = 0; sourceY <= LINEAR_SOURCE_MAX_Y; sourceY++) {
                    IBlockState shifted = source[localX][sourceY][localZ];
                    /* 原版底床不能被搬到Y=64附近，否则会把深层和上层地形切成两段。 */
                    if (sourceY <= 10 && shifted.getBlock() == Blocks.BEDROCK) {
                        shifted = Blocks.STONE.getDefaultState();
                    }
                    primer.setBlockState(localX, sourceY + DEEP_SPACE_HEIGHT, localZ, shifted);
                }

                /*
                 * 顶端压缩：目的高度取其覆盖的原始高度区间中的最高状态。
                 * 空气会自然保留山体上方天空；固体/水体则保留最高地表和山顶材料。
                 */
                for (int targetY = LINEAR_TARGET_MAX_Y + 1; targetY <= PRIMER_MAX_Y; targetY++) {
                    /* 源区间129..255共127格，目标压缩区193..255共63格。 */
                    int sourceStart = LINEAR_SOURCE_MAX_Y + 1
                            + (targetY - (LINEAR_TARGET_MAX_Y + 1)) * (PRIMER_MAX_Y - LINEAR_SOURCE_MAX_Y)
                            / (PRIMER_MAX_Y - LINEAR_TARGET_MAX_Y);
                    int sourceEndExclusive = LINEAR_SOURCE_MAX_Y + 1
                            + (targetY - LINEAR_TARGET_MAX_Y) * (PRIMER_MAX_Y - LINEAR_SOURCE_MAX_Y)
                            / (PRIMER_MAX_Y - LINEAR_TARGET_MAX_Y);
                    IBlockState chosen = Blocks.AIR.getDefaultState();
                    for (int sourceY = Math.min(PRIMER_MAX_Y, sourceEndExclusive - 1);
                         sourceY >= Math.max(LINEAR_SOURCE_MAX_Y + 1, sourceStart); sourceY--) {
                        IBlockState candidate = source[localX][sourceY][localZ];
                        if (!isAir(candidate)) {
                            chosen = candidate;
                            break;
                        }
                    }
                    primer.setBlockState(localX, targetY, localZ, chosen);
                }

                surfaceHeights[column] = findSurfaceHeight(primer, localX, localZ);
            }
        }
        return surfaceHeights;
    }

    /** 返回顶部非空气方块上方的Y坐标；水被视为地表水的一部分。 */
    private static int findSurfaceHeight(ChunkPrimer primer, int localX, int localZ) {
        for (int y = PRIMER_MAX_Y; y >= 0; y--) {
            if (!isAir(primer.getBlockState(localX, y, localZ))) {
                return Math.min(PRIMER_MAX_Y, y + 1);
            }
        }
        return 0;
    }

    public static boolean isNaturalCarvable(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.GRAVEL || block == Blocks.SAND || block == Blocks.SANDSTONE
                || block == Blocks.RED_SANDSTONE || block == Blocks.HARDENED_CLAY
                || block == Blocks.STAINED_HARDENED_CLAY || block == Blocks.MYCELIUM;
    }

    private static boolean isAir(IBlockState state) {
        return state.getBlock() == Blocks.AIR;
    }
}
