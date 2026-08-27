package com.canoestudio.retrofuturelushcave.worldgen.noise;

/**
 * ImprovedNoise 的纯排列与采样部分。
 * 子类只负责使用各自原版随机流填充坐标偏移与置换表，不能改变随机消费顺序。
 */
abstract class AbstractImprovedNoise118 implements NoiseSampler118 {
    private static final int[][] GRADIENTS = {
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1},
            {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}
    };

    protected final byte[] permutation = new byte[256];
    protected final double xo;
    protected final double yo;
    protected final double zo;

    protected AbstractImprovedNoise118(double xo, double yo, double zo) {
        this.xo = xo;
        this.yo = yo;
        this.zo = zo;
    }

    @Override
    public final double getValue(double x, double y, double z) {
        return noise(x, y, z);
    }

    final double noise(double x, double y, double z) {
        double nx = x + xo;
        double ny = y + yo;
        double nz = z + zo;
        int ix = floor(nx);
        int iy = floor(ny);
        int iz = floor(nz);
        return sampleAndLerp(ix, iy, iz, nx - ix, ny - iy, nz - iz, ny - iy);
    }

    /** BlendedNoise仍需要的带Y偏移采样路径。 */
    final double legacyNoise(double x, double y, double z, double yScale, double yMax) {
        double nx = x + xo;
        double ny = y + yo;
        double nz = z + zo;
        int ix = floor(nx);
        int iy = floor(ny);
        int iz = floor(nz);
        double dx = nx - ix;
        double dy = ny - iy;
        double dz = nz - iz;
        double shift = 0.0D;
        if (yScale != 0.0D) {
            double limited = yMax >= 0.0D && yMax < dy ? yMax : dy;
            shift = Math.floor(limited / yScale + 1.0E-7D) * yScale;
        }
        return sampleAndLerp(ix, iy, iz, dx, dy - shift, dz, dy);
    }

    private double sampleAndLerp(int x, int y, int z, double dx, double dy, double dz, double fadeY) {
        int a = p(x);
        int b = p(x + 1);
        int aa = p(a + y);
        int ab = p(a + y + 1);
        int ba = p(b + y);
        int bb = p(b + y + 1);
        return lerp3(fade(dx), fade(fadeY), fade(dz),
                grad(p(aa + z), dx, dy, dz), grad(p(ba + z), dx - 1.0D, dy, dz),
                grad(p(ab + z), dx, dy - 1.0D, dz), grad(p(bb + z), dx - 1.0D, dy - 1.0D, dz),
                grad(p(aa + z + 1), dx, dy, dz - 1.0D), grad(p(ba + z + 1), dx - 1.0D, dy, dz - 1.0D),
                grad(p(ab + z + 1), dx, dy - 1.0D, dz - 1.0D), grad(p(bb + z + 1), dx - 1.0D, dy - 1.0D, dz - 1.0D));
    }

    private int p(int value) {
        return permutation[value & 255] & 255;
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static double grad(int hash, double x, double y, double z) {
        int[] gradient = GRADIENTS[hash & 15];
        return gradient[0] * x + gradient[1] * y + gradient[2] * z;
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp3(double tx, double ty, double tz,
                                double d000, double d100, double d010, double d110,
                                double d001, double d101, double d011, double d111) {
        double z0 = NoiseMath118.lerp(ty, NoiseMath118.lerp(tx, d000, d100), NoiseMath118.lerp(tx, d010, d110));
        double z1 = NoiseMath118.lerp(ty, NoiseMath118.lerp(tx, d001, d101), NoiseMath118.lerp(tx, d011, d111));
        return NoiseMath118.lerp(tz, z0, z1);
    }
}
