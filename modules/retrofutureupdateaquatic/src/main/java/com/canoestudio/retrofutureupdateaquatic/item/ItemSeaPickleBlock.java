package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofuturemccore.api.fluid.RetroWaterlogging;
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

        BlockPos placePos = getPlacementPos(worldIn, pos, facing);
        if (!player.canPlayerEdit(placePos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState state = worldIn.getBlockState(placePos);
        if (state.getBlock() == this.block && state.getValue(BlockSeaPickle.PICKLES) < 4) {
            IBlockState placed = state.withProperty(BlockSeaPickle.PICKLES, state.getValue(BlockSeaPickle.PICKLES) + 1);
            AxisAlignedBB box = placed.getCollisionBoundingBox(worldIn, placePos);
            if (box != Block.NULL_AABB && !worldIn.checkNoEntityCollision(box.offset(placePos))) {
                return EnumActionResult.FAIL;
            }
            worldIn.setBlockState(placePos, placed, 10);
            SoundType soundType = this.block.getSoundType(placed, worldIn, placePos, player);
            worldIn.playSound(player, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }

        if (canReplaceForPlacement(worldIn, placePos) && this.block.canPlaceBlockAt(worldIn, placePos)) {
            IBlockState placed = this.block.getStateForPlacement(worldIn, placePos, facing, hitX, hitY, hitZ,
                this.getMetadata(stack.getMetadata()), player);
            AxisAlignedBB box = placed.getCollisionBoundingBox(worldIn, placePos);
            if (box != Block.NULL_AABB && !worldIn.checkNoEntityCollision(box.offset(placePos))) {
                return EnumActionResult.FAIL;
            }
            if (worldIn.setBlockState(placePos, placed, 11)) {
                SoundType soundType = this.block.getSoundType(placed, worldIn, placePos, player);
                worldIn.playSound(player, placePos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                this.block.onBlockPlacedBy(worldIn, placePos, placed, player, stack);
                stack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side,
            net.minecraft.entity.player.EntityPlayer player, ItemStack stack) {
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() == this.block && state.getValue(BlockSeaPickle.PICKLES) < 4) {
            return true;
        }
        BlockPos placePos = getPlacementPos(worldIn, pos, side);
        return canReplaceForPlacement(worldIn, placePos) && this.block.canPlaceBlockAt(worldIn, placePos);
    }

    private BlockPos getPlacementPos(World world, BlockPos pos, EnumFacing facing) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block || canReplaceForPlacement(world, pos)) {
            return pos;
        }
        return pos.offset(facing);
    }

    private boolean canReplaceForPlacement(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceable(world, pos) || RetroWaterlogging.isWater(world, pos);
    }
}
