package com.canoestudio.retrofuturelushcave.worldgen.noise;

/** NormalNoise 的共享双Perlin混合采样；不涉及随机源或octave构造。 */
abstract class AbstractNormalNoise118 implements NoiseSampler118 {
    private static final double INPUT_FACTOR = 1.0181268882175227D;
    private final NoiseSampler118 first;
    private final NoiseSampler118 second;
    private final double valueFactor;

    protected AbstractNormalNoise118(NoiseSampler118 first, NoiseSampler118 second, double[] amplitudes) {
        this.first = first;
        this.second = second;
        int firstNonZero = Integer.MAX_VALUE;
        int lastNonZero = Integer.MIN_VALUE;
        for (int index = 0; index < amplitudes.length; index++) {
            if (amplitudes[index] != 0.0D) {
                firstNonZero = Math.min(firstNonZero, index);
                lastNonZero = Math.max(lastNonZero, index);
            }
        }
        int octaveSpan = lastNonZero - firstNonZero;
        double expectedDeviation = 0.1D * (1.0D + 1.0D / (octaveSpan + 1.0D));
        valueFactor = (1.0D / 6.0D) / expectedDeviation;
    }

    @Override
    public final double getValue(double x, double y, double z) {
        return (first.getValue(x, y, z)
                + second.getValue(x * INPUT_FACTOR, y * INPUT_FACTOR, z * INPUT_FACTOR)) * valueFactor;
    }
}
