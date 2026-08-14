package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityTadpole;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;

public class ItemTadpoleBucket extends Item {

    public ItemTadpoleBucket() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setRegistryName(RTWU.ID, "tadpole_bucket");
        this.setTranslationKey(RTWU.ID + ".tadpole_bucket");
    }

    public static ItemStack create(int age) {
        ItemStack stack = new ItemStack(ModItems.TADPOLE_BUCKET);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Age", age);
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos placePos = FluidloggedSupport.isWater(world, pos) ? pos : pos.offset(facing);

        if (!world.isRemote) {
            if (!FluidloggedSupport.isWater(world, placePos) && !world.isAirBlock(placePos)) {
                return EnumActionResult.FAIL;
            }
            if (world.isAirBlock(placePos)) {
                world.setBlockState(placePos, net.minecraft.init.Blocks.WATER.getDefaultState(), 3);
            }
            EntityTadpole tadpole = new EntityTadpole(world);
            tadpole.setGrowingAgeTicks(stack.hasTagCompound() ? stack.getTagCompound().getInteger("Age") : 0);
            tadpole.setLocationAndAngles(placePos.getX() + 0.5D, placePos.getY() + 0.15D, placePos.getZ() + 0.5D,
                player.rotationYaw, 0.0F);
            world.spawnEntity(tadpole);
            world.playSound(null, placePos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, new ItemStack(Items.BUCKET));
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
