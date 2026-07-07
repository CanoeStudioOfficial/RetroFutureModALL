package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.compat.AquaticCompat;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityDolphin extends EntityWaterMob {

    @Nullable
    private BlockPos swimTarget;
    private int targetCooldown;
    private int jumpCooldown;

    public EntityDolphin(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 0.6F);
        this.setAir(4800);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.1D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isInWater()) {
            this.setAir(4800);
            updateWaterMovement();
            if (!this.world.isRemote && this.ticksExisted % 60 == 0) {
                grantNearbyPlayersGrace();
            }
        } else {
            this.motionY -= 0.08D;
            this.motionX *= 0.98D;
            this.motionZ *= 0.98D;
        }
        updateRotation();
    }

    private void updateWaterMovement() {
        if (this.targetCooldown > 0) {
            this.targetCooldown--;
        }
        if (this.swimTarget == null || this.targetCooldown <= 0 || !isWater(this.swimTarget)
                || distanceSqToCenter(this.swimTarget) < 2.0D) {
            this.swimTarget = findWaterTarget();
            this.targetCooldown = 40 + this.rand.nextInt(70);
        }
        if (this.swimTarget != null) {
            moveToward(this.swimTarget.getX() + 0.5D, this.swimTarget.getY() + 0.45D,
                this.swimTarget.getZ() + 0.5D, 0.15D, 0.18D);
        }
        if (this.jumpCooldown > 0) {
            this.jumpCooldown--;
        }
        if (this.jumpCooldown <= 0 && this.rand.nextInt(120) == 0 && canJumpOut()) {
            double yawRadians = Math.toRadians(this.rotationYaw);
            this.motionX += -Math.sin(yawRadians) * 0.55D;
            this.motionY += 0.62D;
            this.motionZ += Math.cos(yawRadians) * 0.55D;
            this.jumpCooldown = 100;
        }
        limitMotion(0.26D, 0.22D);
    }

    private void grantNearbyPlayersGrace() {
        for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class,
                this.getEntityBoundingBox().grow(8.0D))) {
            if (player.isInWater() && (player.isSprinting() || AquaticCompat.isActuallySwimming(player))) {
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120, 1, true, true));
            }
        }
    }

    private boolean canJumpOut() {
        BlockPos pos = new BlockPos(this);
        return isWater(pos) && this.world.isAirBlock(pos.up()) && this.world.isAirBlock(pos.up(2));
    }

    @Nullable
    private BlockPos findWaterTarget() {
        BlockPos origin = new BlockPos(this);
        for (int i = 0; i < 18; i++) {
            BlockPos candidate = origin.add(this.rand.nextInt(17) - 8, this.rand.nextInt(7) - 3,
                this.rand.nextInt(17) - 8);
            if (isWater(candidate)) {
                return candidate;
            }
        }
        return isWater(origin) ? origin : null;
    }

    private boolean isWater(BlockPos pos) {
        return this.world.isBlockLoaded(pos) && this.world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private void moveToward(double x, double y, double z, double speed, double inertia) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.0001D) {
            this.motionX += (dx / distance * speed - this.motionX) * inertia;
            this.motionY += (dy / distance * speed - this.motionY) * inertia;
            this.motionZ += (dz / distance * speed - this.motionZ) * inertia;
        }
    }

    private double distanceSqToCenter(BlockPos pos) {
        double dx = pos.getX() + 0.5D - this.posX;
        double dy = pos.getY() + 0.5D - this.posY;
        double dz = pos.getZ() + 0.5D - this.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void limitMotion(double horizontal, double vertical) {
        this.motionX = MathHelper.clamp(this.motionX, -horizontal, horizontal);
        this.motionY = MathHelper.clamp(this.motionY, -vertical, vertical);
        this.motionZ = MathHelper.clamp(this.motionZ, -horizontal, horizontal);
    }

    private void updateRotation() {
        double horizontal = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontal > 1.0E-5D) {
            float yaw = -((float)MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float)Math.PI);
            this.rotationYaw += MathHelper.wrapDegrees(yaw - this.rotationYaw) * 0.18F;
            this.renderYawOffset = this.rotationYaw;
        }
        float pitch = this.isInWater()
            ? -((float)MathHelper.atan2(this.motionY, MathHelper.sqrt(horizontal))) * (180F / (float)Math.PI)
            : 0.0F;
        this.rotationPitch += (pitch - this.rotationPitch) * 0.15F;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.isInWater()) {
            this.motionX *= 0.92D;
            this.motionY *= 0.92D;
            this.motionZ *= 0.92D;
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return pos.getY() < this.world.getSeaLevel()
            && this.world.getBlockState(pos).getMaterial() == Material.WATER
            && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this)
            && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.minecraft.init.SoundEvents.ENTITY_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return net.minecraft.init.SoundEvents.ENTITY_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.init.SoundEvents.ENTITY_SQUID_DEATH;
    }
}
