package com.canoestudio.retrofuturelushcave.contents.blocks;

import com.canoestudio.retrofuturelushcave.utils.FluidloggedCompat;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api")
public abstract class FluidloggableDirectionalBlock extends BlockDirectional implements IFluidloggable {

    @Override
    public boolean isFluidValid(IBlockState state, World world, BlockPos pos, Fluid fluid) {
        return FluidloggedSupport.isWater(fluid);
    }
    protected FluidloggableDirectionalBlock(Material material) {
        super(material);
        setSoundType(SoundType.STONE);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
        if (Loader.isModLoaded("fluidlogged_api")) {
            FluidloggedCompat.scheduleFluidTick(worldIn, pos, state);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
}