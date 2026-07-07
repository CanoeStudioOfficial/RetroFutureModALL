package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.block.BlockSeaPickle;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeaPickleBlock extends ItemBlock {

    public ItemSeaPickleBlock(Block block) {
        super(block);
        this.setRegistryName(block.getRegistryName());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() == this.block && state.getValue(BlockSeaPickle.PICKLES) < 4) {
            IBlockState placed = state.withProperty(BlockSeaPickle.PICKLES, state.getValue(BlockSeaPickle.PICKLES) + 1);
            AxisAlignedBB box = placed.getCollisionBoundingBox(worldIn, pos);
            if (box != Block.NULL_AABB && !worldIn.checkNoEntityCollision(box.offset(pos))) {
                return EnumActionResult.FAIL;
            }
            worldIn.setBlockState(pos, placed, 10);
            SoundType soundType = this.block.getSoundType(placed, worldIn, pos, player);
            worldIn.playSound(player, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
