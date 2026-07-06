package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractRetroEntityInteractionHandler implements RetroEntityInteractionHandler {

    @Override
    public RetroEventResult onRightClickEntity(World world, Entity target, EntityPlayer player, EnumHand hand,
            ItemStack stack) {
        return RetroEventResult.PASS;
    }

    @Override
    public RetroEventResult onRightClickEntityAt(World world, Entity target, EntityPlayer player, EnumHand hand,
            ItemStack stack, Vec3d localPos) {
        return RetroEventResult.PASS;
    }
}
