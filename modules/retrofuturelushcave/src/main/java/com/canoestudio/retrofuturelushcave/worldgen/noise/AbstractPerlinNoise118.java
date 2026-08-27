package com.canoestudio.retrofuturelushcave.worldgen.noise;

/** PerlinNoise 的纯多octave采样部分；子类仅构造不同随机流产生的levels。 */
abstract class AbstractPerlinNoise118 implements NoiseSampler118 {
    private final NoiseSampler118[] levels;
    private final double[] amplitudes;
    private final double lowestFrequencyInputFactor;
    private final double lowestFrequencyValueFactor;

    protected AbstractPerlinNoise118(NoiseSampler118[] levels, int firstOctave, double[] amplitudes) {
        this.levels = levels;
        this.amplitudes = amplitudes.clone();
        int zeroOctaveIndex = -firstOctave;
        lowestFrequencyInputFactor = Math.pow(2.0D, -zeroOctaveIndex);
        lowestFrequencyValueFactor = Math.pow(2.0D, amplitudes.length - 1)
                / (Math.pow(2.0D, amplitudes.length) - 1.0D);
    }

    @Override
    public final double getValue(double x, double y, double z) {
        double result = 0.0D;
        double inputFactor = lowestFrequencyInputFactor;
        double valueFactor = lowestFrequencyValueFactor;
        for (int index = 0; index < levels.length; index++) {
            NoiseSampler118 level = levels[index];
            if (level != null) {
                result += amplitudes[index] * level.getValue(
                        NoiseMath118.wrap(x * inputFactor),
                        NoiseMath118.wrap(y * inputFactor),
                        NoiseMath118.wrap(z * inputFactor)) * valueFactor;
            }
            inputFactor *= 2.0D;
            valueFactor *= 0.5D;
        }
        return result;
    }
}
