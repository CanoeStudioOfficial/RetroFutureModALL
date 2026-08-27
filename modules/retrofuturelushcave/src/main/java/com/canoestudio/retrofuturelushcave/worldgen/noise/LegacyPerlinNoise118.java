package com.canoestudio.retrofuturelushcave.worldgen.noise;

import java.util.Random;

/** 保留Legacy PerlinNoise的八度采样结构，初始化改用Java Random。 */
final class LegacyPerlinNoise118 extends AbstractPerlinNoise118 {

    LegacyPerlinNoise118(Random random, int firstOctave, double[] amplitudes) {
        super(createLevels(random, firstOctave, amplitudes), firstOctave, amplitudes);
    }

    private static NoiseSampler118[] createLevels(Random random, int firstOctave, double[] amplitudes) {
        NoiseSampler118[] levels = new NoiseSampler118[amplitudes.length];
        for (int index = 0; index < amplitudes.length; index++) {
            if (amplitudes[index] != 0.0D) {
                levels[index] = new LegacyImprovedNoise118(new Random(random.nextLong()));
            }
        }
        return levels;
    }
}
