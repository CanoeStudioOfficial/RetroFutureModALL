package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangroveHangingSign;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangroveWallHangingSign;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.block.TileEntityHangingSign;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemMangroveHangingSign extends Item {

    public ItemMangroveHangingSign() {
        this.maxStackSize = 16;
        this.setRegistryName(RTWU.ID, "mangrove_hanging_sign");
        this.setTranslationKey(RTWU.ID + ".mangrove_hanging_sign");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState clickedState = worldIn.getBlockState(pos);
        boolean replaceClicked = clickedState.getBlock().isReplaceable(worldIn, pos);
        BlockPos placePos = replaceClicked ? pos : pos.offset(facing);
        ItemStack itemStack = player.getHeldItem(hand);

        if (facing == EnumFacing.UP || !player.canPlayerEdit(placePos, facing, itemStack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState placedState = this.getPlacementState(player, worldIn, placePos, facing);
        if (placedState == null || !worldIn.mayPlace(placedState.getBlock(), placePos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (!worldIn.setBlockState(placePos, placedState, 11)) {
            return EnumActionResult.FAIL;
        }

        TileEntity tileEntity = worldIn.getTileEntity(placePos);
        if (tileEntity instanceof TileEntityHangingSign) {
            TileEntityHangingSign sign = (TileEntityHangingSign) tileEntity;
            if (placedState.getBlock() == ModBlocks.MANGROVE_HANGING_SIGN) {
                sign.setAttached(shouldAttachToMiddle(player, worldIn, placePos, placedState));
            } else if (placedState.getBlock() == ModBlocks.MANGROVE_WALL_HANGING_SIGN) {
                sign.setWallFacing(placedState.getValue(BlockMangroveWallHangingSign.FACING));
            }
            if (!ItemBlock.setTileEntityNBT(worldIn, player, placePos, itemStack)) {
                player.openEditSign(sign);
            }
            worldIn.notifyBlockUpdate(placePos, placedState, placedState, 3);
        }

        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, placePos, itemStack);
        }

        itemStack.shrink(1);
        return EnumActionResult.SUCCESS;
    }

    private IBlockState getPlacementState(EntityPlayer player, World world, BlockPos pos, EnumFacing clickedFace) {
        if (clickedFace == EnumFacing.DOWN) {
            IBlockState state = ModBlocks.MANGROVE_HANGING_SIGN.getDefaultState()
                .withProperty(BlockMangroveHangingSign.ROTATION, yawToRotation(player.rotationYaw));
            return canCeilingSignStay(world, pos) ? state : null;
        }
        if (clickedFace.getAxis().isHorizontal()) {
            IBlockState wallState = this.findWallState(player, world, pos, clickedFace);
            if (wallState != null) {
                return wallState;
            }
            IBlockState ceilingState = ModBlocks.MANGROVE_HANGING_SIGN.getDefaultState()
                .withProperty(BlockMangroveHangingSign.ROTATION, yawToRotation(player.rotationYaw));
            return canCeilingSignStay(world, pos) ? ceilingState : null;
        }
        return null;
    }

    private IBlockState findWallState(EntityPlayer player, World world, BlockPos pos, EnumFacing clickedFace) {
        EnumFacing look = player.getHorizontalFacing().getOpposite();
        EnumFacing first = look.getAxis() == clickedFace.getAxis() ? clickedFace.rotateY() : look;
        EnumFacing[] candidates = {
            first,
            first.getOpposite(),
            clickedFace.rotateY(),
            clickedFace.rotateYCCW()
        };

        for (EnumFacing candidate : candidates) {
            if (candidate.getAxis() == clickedFace.getAxis()) {
                continue;
            }
            IBlockState state = ModBlocks.MANGROVE_WALL_HANGING_SIGN.getDefaultState()
                .withProperty(BlockMangroveWallHangingSign.FACING, candidate);
            if (((BlockMangroveWallHangingSign) ModBlocks.MANGROVE_WALL_HANGING_SIGN).canAttachToEitherSide(world, pos, state)) {
                return state;
            }
        }
        return null;
    }

    private static boolean canCeilingSignStay(World world, BlockPos pos) {
        IBlockState above = world.getBlockState(pos.up());
        Block block = above.getBlock();
        return block == ModBlocks.MANGROVE_HANGING_SIGN
            || block == ModBlocks.MANGROVE_WALL_HANGING_SIGN
            || above.isSideSolid(world, pos.up(), EnumFacing.DOWN);
    }

    private static boolean shouldAttachToMiddle(EntityPlayer player, World world, BlockPos pos, IBlockState placedState) {
        IBlockState above = world.getBlockState(pos.up());
        if (above.getBlock() == ModBlocks.MANGROVE_HANGING_SIGN) {
            int aboveRotation = above.getValue(BlockMangroveHangingSign.ROTATION);
            int placedRotation = placedState.getValue(BlockMangroveHangingSign.ROTATION);
            return (aboveRotation & 3) != (placedRotation & 3);
        }
        if (above.getBlock() == ModBlocks.MANGROVE_WALL_HANGING_SIGN) {
            EnumFacing aboveFacing = above.getValue(BlockMangroveWallHangingSign.FACING);
            EnumFacing playerFacing = player.getHorizontalFacing();
            return aboveFacing.getAxis() != playerFacing.getAxis();
        }
        return !above.isFullCube();
    }

    private static int yawToRotation(float yaw) {
        return MathHelper.floor((double) ((yaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
    }
}
