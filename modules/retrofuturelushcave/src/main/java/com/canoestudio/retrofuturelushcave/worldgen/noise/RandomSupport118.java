package com.canoestudio.retrofuturelushcave.worldgen.noise;

/** Minecraft 1.18.2 RandomSupport 中密度噪声实际使用的种子升级函数。 */
public final class RandomSupport118 {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;

    private RandomSupport118() {
    }

    public static long mixStafford13(long value) {
        value = (value ^ (value >>> 30)) * -4658895280553007687L;
        value = (value ^ (value >>> 27)) * -7723592293110705685L;
        return value ^ (value >>> 31);
    }

    public static Seed128bit upgradeSeedTo128bit(long worldSeed) {
        long seedLoInput = worldSeed ^ SILVER_RATIO_64;
        long seedHiInput = seedLoInput + GOLDEN_RATIO_64;
        return new Seed128bit(mixStafford13(seedLoInput), mixStafford13(seedHiInput));
    }

    public static final class Seed128bit {
        public final long seedLo;
        public final long seedHi;

        public Seed128bit(long seedLo, long seedHi) {
            this.seedLo = seedLo;
            this.seedHi = seedHi;
        }
    }
}
