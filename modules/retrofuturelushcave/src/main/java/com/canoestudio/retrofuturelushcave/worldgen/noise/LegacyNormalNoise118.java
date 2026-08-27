package com.canoestudio.retrofuturelushcave.worldgen.noise;

import java.util.Random;

/** GeodeFeature的Legacy NormalNoise；初始化使用Java Random。 */
public final class LegacyNormalNoise118 extends AbstractNormalNoise118 {

    private LegacyNormalNoise118(Random random, int firstOctave, double[] amplitudes) {
        super(new LegacyPerlinNoise118(random, firstOctave, amplitudes),
                new LegacyPerlinNoise118(random, firstOctave, amplitudes), amplitudes);
    }

    public static LegacyNormalNoise118 geodeNoise(long worldSeed) {
        return new LegacyNormalNoise118(new Random(worldSeed), -4, new double[] {1.0D});
    }
}
