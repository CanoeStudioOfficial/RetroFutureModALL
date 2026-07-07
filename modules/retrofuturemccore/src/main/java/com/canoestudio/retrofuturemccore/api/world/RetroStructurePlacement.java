package com.canoestudio.retrofuturemccore.api.world;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class RetroStructurePlacement {
    private static final long REGION_X_SEED = 341873128712L;
    private static final long REGION_Z_SEED = 132897987541L;

    private final ResourceLocation id;
    private final int spacing;
    private final int separation;
    private final long salt;
    private final Integer dimension;
    private final int minY;
    private final int maxY;
    private final Predicate<Biome> biomePredicate;

    private RetroStructurePlacement(Builder builder) {
        if (builder.spacing <= 0 || builder.separation < 0 || builder.separation >= builder.spacing) {
            throw new IllegalArgumentException("Structure placement requires spacing > separation >= 0");
        }
        this.id = builder.id;
        this.spacing = builder.spacing;
        this.separation = builder.separation;
        this.salt = builder.salt;
        this.dimension = builder.dimension;
        this.minY = builder.minY;
        this.maxY = builder.maxY;
        this.biomePredicate = builder.biomePredicate;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public long getSalt() {
        return this.salt;
    }

    public Integer getDimension() {
        return this.dimension;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public boolean shouldStartAt(World world, int chunkX, int chunkZ) {
        if (world == null) {
            return false;
        }
        if (this.dimension != null && world.provider.getDimension() != this.dimension.intValue()) {
            return false;
        }
        if (!this.isPlacementChunk(world.getSeed(), chunkX, chunkZ)) {
            return false;
        }
        Biome biome = world.getBiome(getChunkCenter(chunkX, chunkZ));
        return this.biomePredicate == null || this.biomePredicate.test(biome);
    }

    public boolean isPlacementChunk(long worldSeed, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, this.spacing);
        int regionZ = Math.floorDiv(chunkZ, this.spacing);
        Random random = new Random(getRegionSeed(worldSeed, regionX, regionZ));
        int maxOffset = this.spacing - this.separation;
        int candidateX = regionX * this.spacing + random.nextInt(maxOffset);
        int candidateZ = regionZ * this.spacing + random.nextInt(maxOffset);
        return chunkX == candidateX && chunkZ == candidateZ;
    }

    public Random createChunkRandom(World world, int chunkX, int chunkZ) {
        long seed = world.getSeed() + this.salt + (long) chunkX * REGION_X_SEED + (long) chunkZ * REGION_Z_SEED;
        return new Random(seed);
    }

    public boolean isInsideYRange(int y) {
        return y >= this.minY && y <= this.maxY;
    }

    public BlockPos getStartBlockPos(int chunkX, int chunkZ) {
        return new BlockPos((chunkX << 4) + 8, this.minY, (chunkZ << 4) + 8);
    }

    public static BlockPos getChunkCenter(int chunkX, int chunkZ) {
        return new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
    }

    private long getRegionSeed(long worldSeed, int regionX, int regionZ) {
        return worldSeed + this.salt + (long) regionX * REGION_X_SEED + (long) regionZ * REGION_Z_SEED;
    }

    public static final class Builder {
        private final ResourceLocation id;
        private int spacing = 32;
        private int separation = 8;
        private long salt;
        private Integer dimension = Integer.valueOf(0);
        private int minY;
        private int maxY = 255;
        private Predicate<Biome> biomePredicate;

        private Builder(ResourceLocation id) {
            if (id == null) {
                throw new IllegalArgumentException("Structure placement id cannot be null");
            }
            this.id = id;
            this.salt = id.toString().hashCode();
        }

        public Builder spacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder separation(int separation) {
            this.separation = separation;
            return this;
        }

        public Builder salt(long salt) {
            this.salt = salt;
            return this;
        }

        public Builder dimension(int dimension) {
            this.dimension = Integer.valueOf(dimension);
            return this;
        }

        public Builder anyDimension() {
            this.dimension = null;
            return this;
        }

        public Builder yRange(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
            return this;
        }

        public Builder biome(Predicate<Biome> biomePredicate) {
            this.biomePredicate = biomePredicate;
            return this;
        }

        public RetroStructurePlacement build() {
            return new RetroStructurePlacement(this);
        }
    }
}
