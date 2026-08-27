package com.canoestudio.retrofuturelushcave.worldgen.noise;

import java.util.Random;

/** 保留1.18.2 PerlinNoise的八度与采样数学，初始化改用Java Random。 */
final class PerlinNoise118 extends AbstractPerlinNoise118 {

    PerlinNoise118(Random random, int firstOctave, double[] amplitudes) {
        super(createLevels(random, firstOctave, amplitudes), firstOctave, amplitudes);
    }

    private static NoiseSampler118[] createLevels(Random random, int firstOctave, double[] amplitudes) {
        NoiseSampler118[] levels = new NoiseSampler118[amplitudes.length];
        for (int index = 0; index < amplitudes.length; index++) {
            if (amplitudes[index] != 0.0D) {
                levels[index] = new ImprovedNoise118(new Random(random.nextLong()));
            }
        }
        return levels;
    }
}
