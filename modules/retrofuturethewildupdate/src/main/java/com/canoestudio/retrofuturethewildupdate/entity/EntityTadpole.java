package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntityAttributes;
import com.canoestudio.retrofuturethewildupdate.item.ItemTadpoleBucket;
import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityTadpole extends EntityWaterMob {

    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(EntityTadpole.class, DataSerializers.VARINT);
    private static final int GROW_UP_TICKS = 24000;
    private static final int MAX_AIR = 6000;

    @Nullable
    private BlockPos swimTarget;
    private int targetCooldown;

    public EntityTadpole(World worldIn) {
        super(worldIn);
        this.setSize(0.4F, 0.3F);
        this.setAir(MAX_AIR);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(AGE_TICKS, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MAX_HEALTH, 6.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MOVEMENT_SPEED, 0.8D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setGrowingAgeTicks(this.rand.nextInt(6000));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (this.isEntityAlive() && this.isWet()) {
            this.setAir(MAX_AIR);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.isEntityAlive()) {
            return;
        }

        if (!this.world.isRemote) {
            if (this.isInWater()) {
                this.setAir(MAX_AIR);
                this.updateWaterMovement();
                this.setGrowingAgeTicks(this.getGrowingAgeTicks() + 1);
                if (this.getGrowingAgeTicks() >= GROW_UP_TICKS) {
                    this.growIntoFrog();
                    return;
                }
            } else {
                this.swimTarget = null;
                this.setAir(this.getAir() - 1);
                if (this.getAir() <= -20) {
                    this.setAir(0);
                    this.attackEntityFrom(DamageSource.DROWN, 2.0F);
                }
                if (this.onGround && this.rand.nextInt(10) == 0) {
                    this.motionX += (this.rand.nextDouble() - 0.5D) * 0.05D;
                    this.motionY = 0.12D;
                    this.motionZ += (this.rand.nextDouble() - 0.5D) * 0.05D;
                }
            }
        }
        this.updateBodyRotation();
    }

    private void updateWaterMovement() {
        if (this.targetCooldown > 0) {
            --this.targetCooldown;
        }

        if (this.swimTarget == null || this.targetCooldown <= 0 || !this.isWater(this.swimTarget)
            || this.getDistanceSqToTargetCenter(this.swimTarget) < 0.75D) {
            this.swimTarget = this.findRandomWaterTarget();
            this.targetCooldown = 25 + this.rand.nextInt(45);
        }

        if (this.swimTarget != null) {
            this.moveToward(this.swimTarget.getX() + 0.5D, this.swimTarget.getY() + 0.35D, this.swimTarget.getZ() + 0.5D);
        } else {
            this.motionX *= 0.88D;
            this.motionY *= 0.88D;
            this.motionZ *= 0.88D;
        }

        this.motionX = MathHelper.clamp(this.motionX, -0.12D, 0.12D);
        this.motionY = MathHelper.clamp(this.motionY, -0.08D, 0.08D);
        this.motionZ = MathHelper.clamp(this.motionZ, -0.12D, 0.12D);
    }

    private void moveToward(double targetX, double targetY, double targetZ) {
        double dx = targetX - this.posX;
        double dy = targetY - this.posY;
        double dz = targetZ - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.0001D) {
            this.motionX += (dx / distance * 0.055D - this.motionX) * 0.18D;
            this.motionY += (dy / distance * 0.055D - this.motionY) * 0.18D;
            this.motionZ += (dz / distance * 0.055D - this.motionZ) * 0.18D;
        }
    }

    @Nullable
    private BlockPos findRandomWaterTarget() {
        BlockPos origin = new BlockPos(this);
        for (int attempt = 0; attempt < 14; attempt++) {
            BlockPos pos = origin.add(this.rand.nextInt(9) - 4, this.rand.nextInt(5) - 2, this.rand.nextInt(9) - 4);
            if (this.isWater(pos)) {
                return pos;
            }
        }
        return this.isWater(origin) ? origin : null;
    }

    private boolean isWater(BlockPos pos) {
        return this.world.isBlockLoaded(pos) && this.world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private double getDistanceSqToTargetCenter(BlockPos pos) {
        double dx = pos.getX() + 0.5D - this.posX;
        double dy = pos.getY() + 0.5D - this.posY;
        double dz = pos.getZ() + 0.5D - this.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void growIntoFrog() {
        EntityFrog frog = new EntityFrog(this.world);
        frog.setVariant(EntityFrog.variantForBiome(this.world.getBiome(new BlockPos(this))));
        frog.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
        if (this.hasCustomName()) {
            frog.setCustomNameTag(this.getCustomNameTag());
        }
        this.world.spawnEntity(frog);
        this.playSound(SoundEvents.ENTITY_SLIME_SQUISH, 0.8F, 1.0F);
        this.setDead();
    }

    private void updateBodyRotation() {
        double horizontalMotion = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontalMotion > 1.0E-5D) {
            float targetYaw = -((float) MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float) Math.PI);
            this.renderYawOffset += MathHelper.wrapDegrees(targetYaw - this.renderYawOffset) * 0.2F;
            this.rotationYaw = this.renderYawOffset;
        }
        if (this.isInWater()) {
            float horizontal = MathHelper.sqrt(horizontalMotion);
            float targetPitch = -((float) MathHelper.atan2(this.motionY, horizontal)) * (180F / (float) Math.PI);
            this.rotationPitch += (targetPitch - this.rotationPitch) * 0.18F;
        } else {
            this.rotationPitch += (0.0F - this.rotationPitch) * 0.18F;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.isInWater()) {
            this.motionX *= 0.88D;
            this.motionY *= 0.88D;
            this.motionZ *= 0.88D;
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            if (!this.world.isRemote) {
                ItemStack bucket = ItemTadpoleBucket.create(this.getGrowingAgeTicks());
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.setHeldItem(hand, bucket);
                    } else if (!player.inventory.addItemStackToInventory(bucket)) {
                        player.dropItem(bucket, false);
                    }
                }
                this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                this.setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return this.world.getBlockState(pos).getMaterial() == Material.WATER && pos.getY() < this.world.getSeaLevel();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        return new ItemStack(ModItems.TADPOLE_SPAWN_EGG);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Age", this.getGrowingAgeTicks());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Age")) {
            this.setGrowingAgeTicks(compound.getInteger("Age"));
        }
    }

    public int getGrowingAgeTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public void setGrowingAgeTicks(int age) {
        this.dataManager.set(AGE_TICKS, MathHelper.clamp(age, 0, GROW_UP_TICKS));
    }
}
