package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import java.util.Random;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMangroveWallSign extends BlockWallSign {

    public BlockMangroveWallSign() {
        super();
        this.setRegistryName(RTWU.ID, "mangrove_wall_sign");
        this.setTranslationKey(RTWU.ID + ".mangrove_sign");
        this.setCreativeTab(null);
        this.setHardness(1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMangroveSign();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return com.canoestudio.retrofuturethewildupdate.item.ModItems.MANGROVE_SIGN;
    }

    @Override
    public ItemStack getItem(World worldIn, net.minecraft.util.math.BlockPos pos, IBlockState state) {
        return new ItemStack(com.canoestudio.retrofuturethewildupdate.item.ModItems.MANGROVE_SIGN);
    }
}
