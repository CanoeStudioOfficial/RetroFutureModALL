package com.canoestudio.retrofuturelushcave.worldgen.noise;

import java.util.Random;

/** 保留1.18.2 ImprovedNoise采样数学，初始化改用Java Random。 */
final class ImprovedNoise118 extends AbstractImprovedNoise118 {

    ImprovedNoise118(Random random) {
        super(random.nextDouble() * 256.0D, random.nextDouble() * 256.0D, random.nextDouble() * 256.0D);
        for (int index = 0; index < 256; index++) {
            permutation[index] = (byte) index;
        }
        for (int index = 0; index < 256; index++) {
            int offset = random.nextInt(256 - index);
            byte current = permutation[index];
            permutation[index] = permutation[index + offset];
            permutation[index + offset] = current;
        }
    }
}
