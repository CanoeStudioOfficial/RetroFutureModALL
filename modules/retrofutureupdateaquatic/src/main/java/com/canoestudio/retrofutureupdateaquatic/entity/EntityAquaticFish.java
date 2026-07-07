package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ItemFishBucket;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityAquaticFish extends EntityAnimal {

    private final AquaticFishType fishType;
    @Nullable
    private BlockPos swimTarget;
    private int targetCooldown;
    private int flopCooldown;

    protected EntityAquaticFish(World worldIn, AquaticFishType fishType) {
        super(worldIn);
        this.fishType = fishType;
        this.setSize(fishType.getWidth(), fishType.getHeight());
    }

    public AquaticFishType getFishType() {
        return this.fishType;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.fishType.getHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.enablePersistence();
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            if (!this.world.isRemote) {
                ItemStack bucket = ItemFishBucket.create(this.fishType);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                    if (held.isEmpty()) {
                        player.setHeldItem(hand, bucket);
                    } else if (!player.inventory.addItemStackToInventory(bucket)) {
                        player.dropItem(bucket, false);
                    }
                }
                this.playSound(net.minecraft.init.SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                this.setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.isEntityAlive()) {
            return;
        }

        if (this.isInWater()) {
            this.setAir(300);
            updateWaterMovement();
        } else {
            updateLandFlop();
        }
        updateRotationFromMotion();
    }

    private void updateWaterMovement() {
        if (this.targetCooldown > 0) {
            this.targetCooldown--;
        }
        if (this.swimTarget == null || this.targetCooldown <= 0 || !isWater(this.swimTarget)
                || distanceSqToCenter(this.swimTarget) < 0.7D) {
            this.swimTarget = findWaterTarget();
            this.targetCooldown = 25 + this.rand.nextInt(45);
        }
        if (this.swimTarget != null) {
            moveToward(this.swimTarget.getX() + 0.5D, this.swimTarget.getY() + 0.4D,
                this.swimTarget.getZ() + 0.5D, this.fishType.getSwimSpeed(), 0.22D);
        }
        this.motionX *= 0.88D;
        this.motionY *= 0.88D;
        this.motionZ *= 0.88D;
        limitMotion(0.16D, 0.12D);
    }

    private void updateLandFlop() {
        this.swimTarget = null;
        if (this.onGround && this.flopCooldown-- <= 0) {
            this.motionX += (this.rand.nextDouble() - 0.5D) * 0.16D;
            this.motionY = 0.22D + this.rand.nextDouble() * 0.08D;
            this.motionZ += (this.rand.nextDouble() - 0.5D) * 0.16D;
            this.flopCooldown = 8 + this.rand.nextInt(8);
        }
        this.motionX *= 0.72D;
        this.motionZ *= 0.72D;
    }

    @Nullable
    private BlockPos findWaterTarget() {
        BlockPos origin = new BlockPos(this);
        for (int i = 0; i < 18; i++) {
            BlockPos candidate = origin.add(this.rand.nextInt(9) - 4, this.rand.nextInt(5) - 2,
                this.rand.nextInt(9) - 4);
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

    private void limitMotion(double horizontal, double vertical) {
        this.motionX = MathHelper.clamp(this.motionX, -horizontal, horizontal);
        this.motionY = MathHelper.clamp(this.motionY, -vertical, vertical);
        this.motionZ = MathHelper.clamp(this.motionZ, -horizontal, horizontal);
    }

    private double distanceSqToCenter(BlockPos pos) {
        double dx = pos.getX() + 0.5D - this.posX;
        double dy = pos.getY() + 0.5D - this.posY;
        double dz = pos.getZ() + 0.5D - this.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void updateRotationFromMotion() {
        double horizontal = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontal > 1.0E-5D) {
            float yaw = -((float)MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float)Math.PI);
            this.rotationYaw += MathHelper.wrapDegrees(yaw - this.rotationYaw) * 0.2F;
            this.renderYawOffset = this.rotationYaw;
        }
        if (this.isInWater()) {
            float targetPitch = -((float)MathHelper.atan2(this.motionY, MathHelper.sqrt(horizontal))) * (180F / (float)Math.PI);
            this.rotationPitch += (targetPitch - this.rotationPitch) * 0.15F;
        } else {
            this.rotationPitch += (0.0F - this.rotationPitch) * 0.12F;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.isInWater()) {
            this.motionX *= 0.86D;
            this.motionY *= 0.86D;
            this.motionZ *= 0.86D;
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
    public EntityAgeable createChild(EntityAgeable ageable) {
        return this.fishType.create(this.world);
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    protected Item getDropItem() {
        return this.fishType.getRawItem();
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        this.dropItem(this.fishType.getRawItem(), 1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return net.minecraft.init.SoundEvents.ENTITY_GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.init.SoundEvents.ENTITY_GENERIC_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
    }

    public static class Cod extends EntityAquaticFish {
        public Cod(World worldIn) {
            super(worldIn, AquaticFishType.COD);
        }
    }

    public static class Salmon extends EntityAquaticFish {
        public Salmon(World worldIn) {
            super(worldIn, AquaticFishType.SALMON);
        }
    }

    public static class Pufferfish extends EntityAquaticFish {
        private int puffTicks;

        public Pufferfish(World worldIn) {
            super(worldIn, AquaticFishType.PUFFERFISH);
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            if (this.puffTicks > 0) {
                this.puffTicks--;
            }
            if (!this.world.isRemote && this.ticksExisted % 20 == 0) {
                for (EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        this.getEntityBoundingBox().grow(1.25D))) {
                    if (target != this && !(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.isCreativeMode)) {
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F);
                        target.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.POISON, 60, 0));
                        this.puffTicks = 60;
                    }
                }
            }
        }

        public boolean isPuffed() {
            return this.puffTicks > 0;
        }
    }

    public static class Tropical extends EntityAquaticFish {
        public Tropical(World worldIn) {
            super(worldIn, AquaticFishType.TROPICAL_FISH);
        }
    }
}
