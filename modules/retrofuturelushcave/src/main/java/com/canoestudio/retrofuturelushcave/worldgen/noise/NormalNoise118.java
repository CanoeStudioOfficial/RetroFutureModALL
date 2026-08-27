package com.canoestudio.retrofuturelushcave.worldgen.noise;

import java.util.Random;

/** 保留1.18.2 NormalNoise的采样组合，初始化改用Java Random。 */
public final class NormalNoise118 extends AbstractNormalNoise118 {

    public NormalNoise118(Random random, int firstOctave, double... amplitudes) {
        super(new PerlinNoise118(random, firstOctave, amplitudes),
                new PerlinNoise118(random, firstOctave, amplitudes), amplitudes);
    }
}
