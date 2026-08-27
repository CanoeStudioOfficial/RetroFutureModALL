package com.canoestudio.retrofuturelushcave.worldgen;

/** 集中管理1.18 Overworld生成器的关键常量，避免魔法数字分散。 */
public final class NoiseGeneratorSettings118 {
    // 1.18.2 Overworld 完整路由高度范围
    public static final int ROUTER_MIN_Y = -64;
    public static final int ROUTER_HEIGHT = 384;
    public static final int ROUTER_SEA_LEVEL = 63;

    // 1.12.2 可容纳的地形高度范围（对应router Y=-64..191）
    public static final int PRIMER_MIN_Y = 0;
    public static final int PRIMER_HEIGHT = 256;
    public static final int PRIMER_MAX_Y_EXCLUSIVE = PRIMER_MIN_Y + PRIMER_HEIGHT;

    // NoiseChunk 的采样粒度
    public static final int CELL_WIDTH = 4;
    public static final int CELL_HEIGHT = 8;

    // 1.18 Overworld NoiseSettings 的 slide 参数
    public static final double TOP_SLIDE_TARGET = -0.078125D;
    public static final int TOP_SLIDE_SIZE = 2;
    public static final int TOP_SLIDE_OFFSET = 8;
    public static final double BOTTOM_SLIDE_TARGET = 0.1171875D;
    public static final int BOTTOM_SLIDE_SIZE = 3;
    public static final int BOTTOM_SLIDE_OFFSET = 0;

    // 地下熔岩级别的1.12映射（对应router Y=-54）
    public static final int LAVA_LEVEL_112 = 10;

    // NoiseRouterData#underground：rangeChoice(pillars, -∞, 0.03, -∞, pillars)。
    // 0.28会错误抑制绝大多数噪声柱，令Cheese洞室上下连成异常巨型空腔。
    public static final double PILLAR_ACTIVATION_THRESHOLD = 0.03D;
    public static final double NOODLE_TOGGLE_BIAS = 0.09D;
    public static final double NOODLE_THICKNESS_BIAS = -0.02D;
    public static final double NOODLE_RIDGE_MULTIPLIER = 1.35D;

    private NoiseGeneratorSettings118() {
    }
}