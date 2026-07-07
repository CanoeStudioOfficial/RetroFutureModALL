package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemMangroveSign extends Item {

    public ItemMangroveSign() {
        this.maxStackSize = 16;
        this.setRegistryName(RTWU.ID, "mangrove_sign");
        this.setTranslationKey(RTWU.ID + ".mangrove_sign");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState clickedState = worldIn.getBlockState(pos);
        boolean replaceClicked = clickedState.getBlock().isReplaceable(worldIn, pos);

        if (facing == EnumFacing.DOWN
            || (!clickedState.getMaterial().isSolid() && !replaceClicked)
            || (replaceClicked && facing != EnumFacing.UP)) {
            return EnumActionResult.FAIL;
        }

        BlockPos placePos = replaceClicked ? pos : pos.offset(facing);
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(placePos, facing, stack) || !ModBlocks.MANGROVE_SIGN.canPlaceBlockAt(worldIn, placePos)) {
            return EnumActionResult.FAIL;
        }

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (facing == EnumFacing.UP) {
            int rotation = MathHelper.floor((double) ((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
            worldIn.setBlockState(placePos,
                ModBlocks.MANGROVE_SIGN.getDefaultState().withProperty(BlockStandingSign.ROTATION, rotation), 11);
        } else {
            worldIn.setBlockState(placePos,
                ModBlocks.MANGROVE_WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, facing), 11);
        }

        TileEntity tileEntity = worldIn.getTileEntity(placePos);
        if (tileEntity instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(worldIn, player, placePos, stack)) {
            player.openEditSign((TileEntitySign) tileEntity);
        }

        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, placePos, stack);
        }

        stack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
