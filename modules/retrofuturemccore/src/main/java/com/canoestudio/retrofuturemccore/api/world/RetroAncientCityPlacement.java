package com.canoestudio.retrofuturemccore.api.world;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class RetroAncientCityPlacement {
    public static final int DEFAULT_SPACING = 24;
    public static final int DEFAULT_SEPARATION = 8;
    public static final int DEFAULT_MIN_Y = 0;
    public static final int DEFAULT_MAX_Y = 36;
    public static final long DEFAULT_SALT = 0x414E4349454E54L;

    private RetroAncientCityPlacement() {
    }

    public static RetroStructurePlacement.Builder builder(ResourceLocation id) {
        return RetroStructurePlacement.builder(id)
                .dimension(0)
                .spacing(DEFAULT_SPACING)
                .separation(DEFAULT_SEPARATION)
                .salt(DEFAULT_SALT ^ id.toString().hashCode())
                .yRange(DEFAULT_MIN_Y, DEFAULT_MAX_Y);
    }

    public static RetroStructurePlacement create(ResourceLocation id, Predicate<Biome> biomePredicate) {
        return builder(id).biome(biomePredicate).build();
    }

    public static BlockPos findDeepStart(World world, Random random, int blockX, int blockZ, int attempts,
            Predicate<IBlockState> foundationPredicate) {
        return findDeepStart(world, random, blockX, blockZ, attempts, DEFAULT_MIN_Y, DEFAULT_MAX_Y,
                foundationPredicate);
    }

    public static BlockPos findDeepStart(World world, Random random, int blockX, int blockZ, int attempts, int minY,
            int maxY, Predicate<IBlockState> foundationPredicate) {
        if (world == null || random == null || foundationPredicate == null || maxY < minY) {
            return null;
        }
        int range = maxY - minY + 1;
        for (int i = 0; i < attempts; i++) {
            int x = blockX + 3 + random.nextInt(10);
            int y = minY + random.nextInt(range);
            int z = blockZ + 3 + random.nextInt(10);
            BlockPos pos = new BlockPos(x, y, z);
            if (foundationPredicate.test(world.getBlockState(pos))) {
                return pos;
            }
        }
        return null;
    }
}
