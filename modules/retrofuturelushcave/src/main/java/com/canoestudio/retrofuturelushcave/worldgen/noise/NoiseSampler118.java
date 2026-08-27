package com.canoestudio.retrofuturelushcave.worldgen.noise;

/** 纯三维噪声采样契约；不包含任何随机构造或种子派生行为。 */
interface NoiseSampler118 {
    double getValue(double x, double y, double z);
}
