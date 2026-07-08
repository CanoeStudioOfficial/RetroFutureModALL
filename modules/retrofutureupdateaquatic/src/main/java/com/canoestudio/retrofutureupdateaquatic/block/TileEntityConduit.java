package com.canoestudio.retrofutureupdateaquatic.block;

import java.util.List;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityConduit extends TileEntity implements ITickable {

    private int activeFrameBlocks;

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote || this.world.getTotalWorldTime() % 40L != 0L) {
            return;
        }

        if (!isSubmerged()) {
            this.activeFrameBlocks = 0;
            return;
        }

        this.activeFrameBlocks = countFrameBlocks();
        if (this.activeFrameBlocks < 16) {
            return;
        }

        int radius = Math.max(16, this.activeFrameBlocks / 7 * 16);
        AxisAlignedBB box = new AxisAlignedBB(this.pos).grow(radius);
        List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, box);
        for (EntityPlayer player : players) {
            if (player.getDistanceSq(this.pos) <= radius * radius && player.isInWater()) {
                player.addPotionEffect(ModPotions.conduitPower(260));
                player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 260, 0, true, true));
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 260, 0, true, true));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 260, 0, true, true));
            }
        }

        if (this.activeFrameBlocks >= 42) {
            attackNearbyHostile();
        }
    }

    public boolean isActive() {
        return this.activeFrameBlocks >= 16;
    }

    private boolean isSubmerged() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos check = this.pos.add(x, y, z);
                    if (check.equals(this.pos)) {
                        continue;
                    }
                    if (!AquaticWaterHelper.isWaterOrBubble(this.world, check)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int countFrameBlocks() {
        int count = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    int ax = Math.abs(x);
                    int ay = Math.abs(y);
                    int az = Math.abs(z);
                    boolean framePos = (ax == 2 && ay == 2 && z == 0)
                        || (ax == 2 && az == 2 && y == 0)
                        || (ay == 2 && az == 2 && x == 0);
                    if (framePos && isValidFrameBlock(this.world.getBlockState(this.pos.add(x, y, z)).getBlock())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private boolean isValidFrameBlock(Block block) {
        return block == Blocks.PRISMARINE || block == Blocks.SEA_LANTERN;
    }

    private void attackNearbyHostile() {
        AxisAlignedBB box = new AxisAlignedBB(this.pos).grow(8.0D);
        List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        EntityLivingBase closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityLivingBase target : targets) {
            if (!(target instanceof IMob) || !target.isInWater() || !target.isEntityAlive()) {
                continue;
            }
            double distance = target.getDistanceSq(this.pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = target;
            }
        }
        if (closest != null) {
            closest.attackEntityFrom(DamageSource.MAGIC, 4.0F);
        }
    }

}
