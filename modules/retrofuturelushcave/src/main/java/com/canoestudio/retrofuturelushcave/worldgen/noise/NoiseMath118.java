package com.canoestudio.retrofuturelushcave.worldgen.noise;

/** 1.18 噪声实现共享的纯数学工具。 */
final class NoiseMath118 {
    private static final double WRAP_PERIOD = 33554432.0D;

    private NoiseMath118() {
    }

    static double wrap(double value) {
        return value - Math.floor(value / WRAP_PERIOD + 0.5D) * WRAP_PERIOD;
    }

    static double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }

    static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    static double clampedLerp(double start, double end, double t) {
        return lerp(clamp(t, 0.0D, 1.0D), start, end);
    }
}
