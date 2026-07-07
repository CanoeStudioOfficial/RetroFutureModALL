package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMangroveWallHangingSign extends BlockWildHangingSignBase {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockMangroveWallHangingSign() {
        super("mangrove_wall_hanging_sign");
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getAxis() == EnumFacing.Axis.X ? WALL_EW_AABB : WALL_NS_AABB;
    }

    @Override
    protected boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return this.canAttachToEitherSide(world, pos, state);
    }

    public boolean canAttachToEitherSide(World world, BlockPos pos, IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        EnumFacing clockwise = facing.rotateY();
        EnumFacing counterClockwise = facing.rotateYCCW();
        return this.canAttachTo(world, pos.offset(clockwise), counterClockwise, facing)
            || this.canAttachTo(world, pos.offset(counterClockwise), clockwise, facing);
    }

    private boolean canAttachTo(World world, BlockPos attachPos, EnumFacing attachFace, EnumFacing signFacing) {
        IBlockState attachState = world.getBlockState(attachPos);
        if (attachState.getBlock() == ModBlocks.MANGROVE_WALL_HANGING_SIGN) {
            EnumFacing otherFacing = attachState.getValue(FACING);
            return otherFacing.getAxis() == signFacing.getAxis();
        }
        return attachState.isSideSolid(world, attachPos, attachFace);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileEntityHangingSign sign = new TileEntityHangingSign();
        sign.setWallFacingSilently(this.getStateFromMeta(meta).getValue(FACING));
        return sign;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }
}
