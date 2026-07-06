package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractRetroBlockInteractionHandler implements RetroBlockInteractionHandler {

    @Override
    public RetroEventResult onRightClickBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, ItemStack stack, EnumFacing face, Vec3d hitVec) {
        return RetroEventResult.PASS;
    }
}
