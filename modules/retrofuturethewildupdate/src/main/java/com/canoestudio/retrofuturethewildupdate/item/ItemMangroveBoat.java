package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityMangroveBoat;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMangroveBoat extends Item {

    public ItemMangroveBoat() {
        this(RTWU.ID, "mangrove_boat", CreativeTabs.TRANSPORTATION);
    }

    protected ItemMangroveBoat(String modid, String name, CreativeTabs tab) {
        this.maxStackSize = 1;
        this.setRegistryName(modid, name);
        this.setTranslationKey(modid + "." + name);
        this.setCreativeTab(tab);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch);
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw);
        double x = player.prevPosX + (player.posX - player.prevPosX);
        double y = player.prevPosY + (player.posY - player.prevPosY) + player.getEyeHeight();
        double z = player.prevPosZ + (player.posZ - player.prevPosZ);
        Vec3d eye = new Vec3d(x, y, z);
        float xFacing = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float zFacing = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float pitchFacing = -MathHelper.cos(-pitch * 0.017453292F);
        float yFacing = MathHelper.sin(-pitch * 0.017453292F);
        Vec3d target = eye.add(zFacing * pitchFacing * 5.0D, yFacing * 5.0D, xFacing * pitchFacing * 5.0D);
        RayTraceResult hit = world.rayTraceBlocks(eye, target, true);

        if (hit == null) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        Vec3d look = player.getLook(1.0F);
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(
            player,
            player.getEntityBoundingBox().expand(look.x * 5.0D, look.y * 5.0D, look.z * 5.0D).grow(1.0D)
        );
        for (Entity entity : entities) {
            if (entity.canBeCollidedWith()
                    && entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize()).contains(eye)) {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }
        }

        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        Block block = world.getBlockState(hit.getBlockPos()).getBlock();
        boolean water = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        EntityMangroveBoat boat = this.createBoat(world, hit.hitVec.x, water ? hit.hitVec.y - 0.12D : hit.hitVec.y,
            hit.hitVec.z);
        boat.rotationYaw = player.rotationYaw;
        AxisAlignedBB box = boat.getEntityBoundingBox().grow(-0.1D);

        if (!world.getCollisionBoxes(boat, box).isEmpty()) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            world.spawnEntity(boat);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    protected EntityMangroveBoat createBoat(World world, double x, double y, double z) {
        return new EntityMangroveBoat(world, x, y, z);
    }
}
