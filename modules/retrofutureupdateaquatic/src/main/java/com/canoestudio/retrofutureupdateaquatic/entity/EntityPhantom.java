package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.sounds.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityPhantom extends EntityMob {

    private static final DataParameter<Integer> SIZE =
        EntityDataManager.createKey(EntityPhantom.class, DataSerializers.VARINT);

    private AttackPhase phase = AttackPhase.CIRCLING;
    private BlockPos anchorPoint = BlockPos.ORIGIN;
    @Nullable
    private Vec3Target moveTarget;
    private float circleAngle;
    private float circleDistance;
    private float circleHeight;
    private int nextSweepTicks;

    public EntityPhantom(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 0.5F);
        this.experienceValue = 5;
        this.setNoGravity(true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SIZE, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        this.setPhantomSize(0);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setNoGravity(true);
        if (this.world.isRemote) {
            spawnWingParticles();
        } else {
            if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                this.setDead();
                return;
            }
            updateTarget();
            updateAttackPhase();
            burnInDaylight();
        }
        updateMoveTarget();
        flyTowardMoveTarget();
        updateRotation();
    }

    private void updateTarget() {
        EntityLivingBase target = this.getAttackTarget();
        if (!canTarget(target)) {
            this.setAttackTarget(null);
            target = null;
        }

        if (target != null && this.ticksExisted % 20 == 0) {
            this.anchorPoint = target.getPosition().up(18 + this.rand.nextInt(10));
            return;
        }

        if (target == null && this.ticksExisted % 20 == 0) {
            double range = this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            EntityPlayer player = this.world.getClosestPlayerToEntity(this, range);
            if (canTarget(player)) {
                this.setAttackTarget(player);
                this.anchorPoint = player.getPosition().up(18 + this.rand.nextInt(10));
                this.phase = AttackPhase.CIRCLING;
                pickNewCirclePoint();
            }
        }
    }

    private boolean canTarget(@Nullable EntityLivingBase target) {
        if (!(target instanceof EntityPlayer) || !target.isEntityAlive()) {
            return false;
        }
        EntityPlayer player = (EntityPlayer)target;
        return !player.capabilities.isCreativeMode
            && !player.isSpectator()
            && this.getDistanceSq(player) < 4096.0D
            && this.canEntityBeSeen(player);
    }

    private void updateAttackPhase() {
        EntityLivingBase target = this.getAttackTarget();
        if (!canTarget(target)) {
            this.phase = AttackPhase.CIRCLING;
            this.nextSweepTicks = Math.max(this.nextSweepTicks, 40);
            return;
        }

        if (this.phase == AttackPhase.SWOOPING) {
            if (this.getEntityBoundingBox().grow(0.2D).intersects(target.getEntityBoundingBox())) {
                target.attackEntityFrom(DamageSource.causeMobDamage(this),
                    (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                this.playSound(ModSounds.PHANTOM_BITE, 0.8F, 0.95F + this.rand.nextFloat() * 0.1F);
                finishSwoop(target);
            } else if (this.collidedHorizontally || this.hurtTime > 0 || this.posY < target.posY - 2.0D) {
                finishSwoop(target);
            }
            return;
        }

        if (this.nextSweepTicks > 0) {
            this.nextSweepTicks--;
        } else if (this.getDistanceSq(target) < 36.0D * 36.0D) {
            this.phase = AttackPhase.SWOOPING;
            this.playSound(ModSounds.PHANTOM_SWOOP, 1.6F, 0.95F + this.rand.nextFloat() * 0.1F);
        }
    }

    private void finishSwoop(EntityLivingBase target) {
        this.phase = AttackPhase.CIRCLING;
        this.anchorPoint = target.getPosition().up(18 + this.rand.nextInt(10));
        this.nextSweepTicks = 80 + this.rand.nextInt(80);
        pickNewCirclePoint();
    }

    private void updateMoveTarget() {
        EntityLivingBase target = this.getAttackTarget();
        if (this.phase == AttackPhase.SWOOPING && canTarget(target)) {
            this.moveTarget = new Vec3Target(target.posX, target.posY + target.height * 0.45D, target.posZ);
            return;
        }

        if (this.anchorPoint == BlockPos.ORIGIN) {
            this.anchorPoint = this.getPosition().up(16);
        }
        if (this.moveTarget == null || distanceSqTo(this.moveTarget) < 4.0D) {
            pickNewCirclePoint();
        }
    }

    private void pickNewCirclePoint() {
        if (this.circleDistance <= 0.0F) {
            this.circleDistance = 6.0F + this.rand.nextFloat() * 8.0F;
        }
        this.circleDistance += this.rand.nextFloat() * 4.0F - 2.0F;
        this.circleDistance = MathHelper.clamp(this.circleDistance, 6.0F, 18.0F);
        this.circleHeight += this.rand.nextFloat() * 4.0F - 2.0F;
        this.circleHeight = MathHelper.clamp(this.circleHeight, -4.0F, 6.0F);
        this.circleAngle += (this.rand.nextBoolean() ? 1.0F : -1.0F) * (0.35F + this.rand.nextFloat() * 0.5F);
        double x = this.anchorPoint.getX() + 0.5D + MathHelper.cos(this.circleAngle) * this.circleDistance;
        double y = this.anchorPoint.getY() + 0.5D + this.circleHeight;
        double z = this.anchorPoint.getZ() + 0.5D + MathHelper.sin(this.circleAngle) * this.circleDistance;
        this.moveTarget = new Vec3Target(x, y, z);
    }

    private void flyTowardMoveTarget() {
        if (this.moveTarget == null) {
            return;
        }
        double dx = this.moveTarget.x - this.posX;
        double dy = this.moveTarget.y - this.posY;
        double dz = this.moveTarget.z - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.0001D) {
            double speed = this.phase == AttackPhase.SWOOPING ? 0.35D : 0.18D;
            double inertia = this.phase == AttackPhase.SWOOPING ? 0.22D : 0.12D;
            this.motionX += (dx / distance * speed - this.motionX) * inertia;
            this.motionY += (dy / distance * speed - this.motionY) * inertia;
            this.motionZ += (dz / distance * speed - this.motionZ) * inertia;
        }
        this.motionX *= 0.93D;
        this.motionY *= 0.93D;
        this.motionZ *= 0.93D;
    }

    private double distanceSqTo(Vec3Target target) {
        double dx = target.x - this.posX;
        double dy = target.y - this.posY;
        double dz = target.z - this.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void updateRotation() {
        double horizontal = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontal > 1.0E-5D) {
            float yaw = -((float)MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float)Math.PI);
            this.rotationYaw += MathHelper.wrapDegrees(yaw - this.rotationYaw) * 0.2F;
            this.renderYawOffset = this.rotationYaw;
        }
        float pitch = -((float)MathHelper.atan2(this.motionY, MathHelper.sqrt(horizontal))) * (180F / (float)Math.PI);
        this.rotationPitch += (pitch - this.rotationPitch) * 0.2F;
        this.rotationYawHead = this.rotationYaw;
    }

    private void spawnWingParticles() {
        float offset = this.getEntityId() * 3.0F + this.ticksExisted;
        float flap = MathHelper.cos(offset * 7.448451F * (float)Math.PI / 180.0F + (float)Math.PI);
        float nextFlap = MathHelper.cos((offset + 1.0F) * 7.448451F * (float)Math.PI / 180.0F + (float)Math.PI);
        if (flap > 0.0F && nextFlap <= 0.0F) {
            this.playSound(ModSounds.PHANTOM_FLAP, 0.95F + this.rand.nextFloat() * 0.05F,
                0.95F + this.rand.nextFloat() * 0.05F);
        }
        float angle = this.rotationYawHead * (float)Math.PI / 180.0F;
        float wingX = MathHelper.cos(angle) * (1.3F + 0.21F * this.getPhantomSize());
        float wingY = (0.3F - flap * 0.45F) * (this.getPhantomSize() * 0.2F + 1.0F);
        float wingZ = MathHelper.sin(angle) * (1.3F + 0.21F * this.getPhantomSize());
        this.world.spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX + wingX, this.posY + wingY,
            this.posZ + wingZ, 0.0D, 0.0D, 0.0D);
        this.world.spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX - wingX, this.posY + wingY,
            this.posZ - wingZ, 0.0D, 0.0D, 0.0D);
    }

    private void burnInDaylight() {
        if (!this.isEntityAlive() || !this.world.isDaytime()) {
            return;
        }
        float brightness = this.getBrightness();
        BlockPos eyePos = new BlockPos(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        if (brightness <= 0.5F || !this.world.canBlockSeeSky(eyePos)
            || this.rand.nextFloat() * 30.0F >= (brightness - 0.4F) * 2.0F) {
            return;
        }
        ItemStack helmet = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            this.setFire(8);
        } else if (helmet.isItemStackDamageable()) {
            helmet.damageItem(this.rand.nextInt(2), this);
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.phase == AttackPhase.SWOOPING) {
            this.phase = AttackPhase.CIRCLING;
            this.nextSweepTicks = 60 + this.rand.nextInt(60);
            pickNewCirclePoint();
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = super.attackEntityAsMob(entityIn);
        if (attacked) {
            this.playSound(ModSounds.PHANTOM_BITE, 0.8F, 0.95F + this.rand.nextFloat() * 0.1F);
        }
        return attacked;
    }

    public int getPhantomSize() {
        return this.dataManager.get(SIZE);
    }

    public void setPhantomSize(int size) {
        int clamped = MathHelper.clamp(size, 0, 64);
        this.dataManager.set(SIZE, clamped);
        float scale = 1.0F + 0.15F * clamped;
        this.setSize(0.9F * scale, 0.5F * scale);
        this.experienceValue = 5 + clamped;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (SIZE.equals(key)) {
            float scale = 1.0F + 0.15F * this.getPhantomSize();
            this.setSize(0.9F * scale, 0.5F * scale);
        }
    }

    @Override
    protected Item getDropItem() {
        return ModItems.PHANTOM_MEMBRANE;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int count = this.rand.nextInt(2) + this.rand.nextInt(lootingModifier + 1);
        for (int i = 0; i < count; i++) {
            this.dropItem(ModItems.PHANTOM_MEMBRANE, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return this.world.canBlockSeeSky(pos) && pos.getY() > this.world.getSeaLevel()
            && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PHANTOM_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Size", this.getPhantomSize());
        compound.setInteger("AnchorX", this.anchorPoint.getX());
        compound.setInteger("AnchorY", this.anchorPoint.getY());
        compound.setInteger("AnchorZ", this.anchorPoint.getZ());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPhantomSize(compound.getInteger("Size"));
        this.anchorPoint = new BlockPos(compound.getInteger("AnchorX"), compound.getInteger("AnchorY"),
            compound.getInteger("AnchorZ"));
    }

    private enum AttackPhase {
        CIRCLING,
        SWOOPING
    }

    private static final class Vec3Target {
        private final double x;
        private final double y;
        private final double z;

        private Vec3Target(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
