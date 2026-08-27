package com.canoestudio.retrofuturelushcave.worldgen.cave;

import net.minecraft.util.math.BlockPos;

public final class OverworldHeightCompat {
    private static final int VANILLA_112_SEA_LEVEL = 63;

    private OverworldHeightCompat() {
    }

    /** 当前无压缩映射相对原版1.12海平面的垂直抬升量，现为64。 */
    public static int getVerticalOffset() {
        return 127 - VANILLA_112_SEA_LEVEL;
    }

    /**
     * 将抬高后世界中的气候采样位置还原为原版1.12温度曲线应读取的Y。
     * 最低处钳制到Y=0，避免构造用于温度计算的负Y坐标。
     */
    public static BlockPos toVanillaClimatePosition(BlockPos raisedWorldPos) {
        int y = Math.max(0, raisedWorldPos.getY() - getVerticalOffset());
        return new BlockPos(raisedWorldPos.getX(), y, raisedWorldPos.getZ());
    }
}
