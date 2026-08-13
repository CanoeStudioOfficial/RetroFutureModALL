package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.AquaticFishType;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityAquaticFish;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFishBucket extends Item {

    private final AquaticFishType fishType;

    public ItemFishBucket(AquaticFishType fishType) {
        this.fishType = fishType;
        this.setRegistryName(RetroFutureUpdateAquatic.ID, fishType.getId() + "_bucket");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + "." + fishType.getId() + "_bucket");
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC);
        this.setMaxStackSize(1);
        this.setContainerItem(Items.BUCKET);
    }

    public static ItemStack create(EntityAquaticFish fish) {
        ItemStack bucket = new ItemStack(ModItems.getBucket(fish.getFishType()));
        if (fish.getFishType() == AquaticFishType.TROPICAL_FISH) {
            NBTTagCompound entityTag = new NBTTagCompound();
            entityTag.setInteger("Variant", fish.getTropicalFishVariant());
            setEntityTag(bucket, entityTag);
        }
        return bucket;
    }

    private static void setEntityTag(ItemStack stack, NBTTagCompound entityTag) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setTag("EntityTag", entityTag);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        RayTraceResult ray = this.rayTrace(worldIn, playerIn, false);
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        BlockPos hitPos = ray.getBlockPos();
        BlockPos placePos = worldIn.getBlockState(hitPos).getBlock().isReplaceable(worldIn, hitPos)
            && ray.sideHit == EnumFacing.UP ? hitPos : hitPos.offset(ray.sideHit);
        if (!worldIn.isBlockModifiable(playerIn, hitPos) || !playerIn.canPlayerEdit(placePos, ray.sideHit, stack)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (!tryPlaceFish(playerIn, worldIn, placePos, stack)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS,
            playerIn.capabilities.isCreativeMode ? stack : new ItemStack(Items.BUCKET));
    }

    private boolean tryPlaceFish(EntityPlayer player, World world, BlockPos pos, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        boolean water = FluidloggedSupport.isWater(world, pos);
        boolean replaceable = state.getBlock().isReplaceable(world, pos);
        if (!world.isAirBlock(pos) && !water && material.isSolid() && !replaceable) {
            return false;
        }

        if (!world.isRemote) {
            if (!world.provider.doesWaterVaporize() && !water) {
                if (replaceable && !material.isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                FluidloggedSupport.setFluidState(world, pos, world.getBlockState(pos),
                    FluidState.of(FluidRegistry.WATER), 11);
            }

            EntityAquaticFish fish = this.fishType.create(world);
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("EntityTag", 10)) {
                NBTTagCompound entityTag = tag.getCompoundTag("EntityTag");
                if (this.fishType == AquaticFishType.TROPICAL_FISH && entityTag.hasKey("Variant")) {
                    fish.setTropicalFishVariant(entityTag.getInteger("Variant"));
                }
            }
            fish.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D,
                player.rotationYaw, 0.0F);
            fish.enablePersistence();
            world.spawnEntity(fish);
        }

        world.playSound(player, pos, net.minecraft.init.SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.NEUTRAL,
            1.0F, 1.0F);
        return true;
    }
}
