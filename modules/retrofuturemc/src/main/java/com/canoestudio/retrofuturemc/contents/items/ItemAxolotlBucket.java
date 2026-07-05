package com.canoestudio.retrofuturemc.contents.items;

import com.canoestudio.retrofuturemc.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import com.canoestudio.retrofuturemc.sounds.ModSoundHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import static com.canoestudio.retrofuturemc.contents.tab.CreativeTab.CREATIVE_TABS;

public class ItemAxolotlBucket extends Item {
    private static final String VARIANT_KEY = "Variant";

    public ItemAxolotlBucket() {
        setTranslationKey(Tags.MOD_ID + ".axolotl_bucket");
        setRegistryName("axolotl_bucket");
        setCreativeTab(CREATIVE_TABS);
        setMaxStackSize(1);
        setContainerItem(Items.BUCKET);
        ModItems.ITEMS.add(this);
    }

    public static ItemStack create(EntityAxolotl axolotl) {
        ItemStack stack = new ItemStack(ModItems.AXOLOTL_BUCKET);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(VARIANT_KEY, axolotl.getVariant());
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        RayTraceResult ray = rayTrace(worldIn, playerIn, false);
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        BlockPos hitPos = ray.getBlockPos();
        BlockPos placePos = worldIn.getBlockState(hitPos).getBlock().isReplaceable(worldIn, hitPos) && ray.sideHit == EnumFacing.UP ? hitPos : hitPos.offset(ray.sideHit);
        if (!worldIn.isBlockModifiable(playerIn, hitPos) || !playerIn.canPlayerEdit(placePos, ray.sideHit, stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!tryPlaceAxolotl(playerIn, worldIn, placePos, stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.capabilities.isCreativeMode ? stack : new ItemStack(Items.BUCKET));
    }

    private boolean tryPlaceAxolotl(EntityPlayer player, World world, BlockPos pos, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        boolean replaceable = state.getBlock().isReplaceable(world, pos);
        if (!world.isAirBlock(pos) && material != Material.WATER && material.isSolid() && !replaceable) {
            return false;
        }

        if (!world.isRemote) {
            if (!world.provider.doesWaterVaporize() && material != Material.WATER) {
                if (replaceable && !material.isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                world.setBlockState(pos, Blocks.WATER.getDefaultState(), 11);
            }

            EntityAxolotl axolotl = new EntityAxolotl(world);
            axolotl.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, player.rotationYaw, 0.0F);
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(VARIANT_KEY)) {
                axolotl.setVariant(tag.getInteger(VARIANT_KEY));
            }
            axolotl.enablePersistence();
            world.spawnEntity(axolotl);
        }

        world.playSound(player, pos, ModSoundHandler.ITEM_BUCKET_EMPTY_AXOLOTL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        return true;
    }
}
