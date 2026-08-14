package com.canoestudio.retrofutureupdateaquatic.block;

import java.util.List;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
            if (player.getDistanceSqToCenter(this.pos) <= radius * radius
                    && (FluidloggedSupport.isEntityInWater(player) || player.isWet())) {
                player.addPotionEffect(ModPotions.conduitPower(260));
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
                    // 潮涌核心自身占据中心位置，不应被当作未浸没方块。
                    if (!check.equals(this.pos) && !AquaticWaterHelper.isWaterOrBubble(this.world, check)) {
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
                    // Three perpendicular 5x5 rings form the vanilla conduit frame.
                    // Their six axis endpoints overlap, so a complete frame has
                    // 16 * 3 - 6 = 42 valid positions.
                    boolean framePos = (z == 0 && (ax == 2 || ay == 2))
                        || (y == 0 && (ax == 2 || az == 2))
                        || (x == 0 && (ay == 2 || az == 2));
                    if (framePos && isValidFrameBlock(this.world.getBlockState(this.pos.add(x, y, z)))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private boolean isValidFrameBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.SEA_LANTERN) {
            return true;
        }
        // In 1.12.2 the three 1.13 frame materials are variants of the
        // vanilla PRISMARINE block rather than separate Block instances.
        return block == Blocks.PRISMARINE;
    }

    private void attackNearbyHostile() {
        AxisAlignedBB box = new AxisAlignedBB(this.pos).grow(8.0D);
        List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        EntityLivingBase closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityLivingBase target : targets) {
            if (!(target instanceof IMob)
                    || !(FluidloggedSupport.isEntityInWater(target) || target.isWet())
                    || !target.isEntityAlive()) {
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
