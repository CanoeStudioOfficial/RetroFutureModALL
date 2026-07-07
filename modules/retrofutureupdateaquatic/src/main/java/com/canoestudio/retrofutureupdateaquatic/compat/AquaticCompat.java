package com.canoestudio.retrofutureupdateaquatic.compat;

import java.lang.reflect.Method;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;

public final class AquaticCompat {

    private static Boolean aquaAcrobaticsLoaded;
    private static Class<?> resizeablePlayerClass;
    private static Method isActuallySwimmingMethod;

    private AquaticCompat() {
    }

    public static boolean isAquaAcrobaticsLoaded() {
        if (aquaAcrobaticsLoaded == null) {
            aquaAcrobaticsLoaded = Loader.isModLoaded("aquaacrobatics");
        }
        return aquaAcrobaticsLoaded.booleanValue();
    }

    public static boolean isActuallySwimming(EntityPlayer player) {
        if (!isAquaAcrobaticsLoaded()) {
            return false;
        }
        try {
            if (resizeablePlayerClass == null) {
                resizeablePlayerClass = Class.forName("com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable");
                isActuallySwimmingMethod = resizeablePlayerClass.getMethod("isActuallySwimming");
            }
            return resizeablePlayerClass.isInstance(player)
                && Boolean.TRUE.equals(isActuallySwimmingMethod.invoke(player));
        } catch (ReflectiveOperationException e) {
            aquaAcrobaticsLoaded = Boolean.FALSE;
            return false;
        }
    }
}
