package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMangroveHangingSign extends BlockWildHangingSignBase {

    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);

    public BlockMangroveHangingSign() {
        super("mangrove_hanging_sign");
        this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, 0));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return CEILING_AABB;
    }

    @Override
    protected boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState above = world.getBlockState(pos.up());
        if (this.isHangingSign(above)) {
            return true;
        }
        return above.isSideSolid(world, pos.up(), net.minecraft.util.EnumFacing.DOWN);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ROTATION, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ROTATION) & 15;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(ROTATION, rot.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withProperty(ROTATION, mirrorIn.mirrorRotation(state.getValue(ROTATION), 16));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {ROTATION});
    }
}
