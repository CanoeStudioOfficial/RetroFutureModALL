package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityDrowned extends EntityZombie {

    private int swimTargetCooldown;

    public EntityDrowned(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        if (this.rand.nextInt(6) == 0) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.TRIDENT));
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isInWater() && this.getAttackTarget() != null && this.getAttackTarget().isInWater()) {
            moveToward(this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, 0.045D);
        } else if (this.isInWater() && this.swimTargetCooldown-- <= 0) {
            BlockPos target = new BlockPos(this).add(this.rand.nextInt(11) - 5, this.rand.nextInt(5) - 2,
                this.rand.nextInt(11) - 5);
            if (this.world.getBlockState(target).getMaterial() == Material.WATER) {
                moveToward(target.getX() + 0.5D, target.getY() + 0.3D, target.getZ() + 0.5D, 0.025D);
            }
            this.swimTargetCooldown = 20 + this.rand.nextInt(30);
        }
    }

    private void moveToward(double x, double y, double z, double speed) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.0001D) {
            this.motionX += dx / distance * speed;
            this.motionY += dy / distance * speed;
            this.motionZ += dz / distance * speed;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.minecraft.init.SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return net.minecraft.init.SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.init.SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        super.dropFewItems(wasRecentlyHit, lootingModifier);
        if (this.rand.nextInt(100) < 3 + lootingModifier) {
            this.dropItem(Items.ROTTEN_FLESH, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL
            && pos.getY() < this.world.getSeaLevel()
            && this.world.getBlockState(pos).getMaterial() == Material.WATER
            && this.world.getLightFromNeighbors(pos) < 8
            && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this)
            && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }
}
