package com.canoestudio.retrofuturemc.contents.mobs.axolotl;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import com.canoestudio.retrofuturemc.sounds.ModSoundHandler;
import com.canoestudio.retrofuturemc.contents.items.ItemAxolotlBucket;

import javax.annotation.Nullable;

public class EntityAxolotl extends EntityWaterMob {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityAxolotl.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> PLAYING_DEAD = EntityDataManager.createKey(EntityAxolotl.class, DataSerializers.BOOLEAN);
    private static final int MAX_AIR = 6000;
    private static final int TOTAL_PLAY_DEAD_TIME = 200;
    private static final int ANIMATION_BLEND_TICKS = 10;
    private static final double WATER_IDLE_SPEED = 0.055D;
    private static final double WATER_MOVE_INERTIA = 0.16D;
    private static final double WATER_DRAG = 0.9D;
    private static final double LAND_SEEK_SPEED = 0.045D;
    private static final double HOSTILE_TARGET_RANGE = 8.0D;
    private static final double HOSTILE_TARGET_RANGE_SQ = HOSTILE_TARGET_RANGE * HOSTILE_TARGET_RANGE;
    private static final double ATTACK_REACH_SQ = 1.8D;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final String[] VARIANT_NAMES = new String[] {"lucy", "wild", "gold", "cyan", "blue"};

    @Nullable
    private BlockPos swimTarget;
    @Nullable
    private BlockPos waterTarget;
    private int targetCooldown;
    private int landHopCooldown;
    private int playDeadTicks;
    private int swimPauseTicks;
    private int attackCooldown;
    private int bucketReleaseTicks;
    @Nullable
    private EntityLivingBase attackTarget;
    private final AnimationFactor playingDeadAnimator = new AnimationFactor();
    private final AnimationFactor inWaterAnimator = new AnimationFactor();
    private final AnimationFactor onGroundAnimator = new AnimationFactor();
    private final AnimationFactor movingAnimator = new AnimationFactor();

    public EntityAxolotl(World world) {
        super(world);
        setSize(0.75F, 0.42F);
        setAir(MAX_AIR);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, 0);
        dataManager.register(PLAYING_DEAD, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
        getAttackDamageAttribute().setBaseValue(2.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.35D);
    }

    private IAttributeInstance getAttackDamageAttribute() {
        IAttributeInstance attribute = getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        return attribute != null ? attribute : getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        setRandomVariant();
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public void setRandomVariant() {
        if (rand.nextInt(1200) == 0) {
            setVariant(4);
        } else {
            setVariant(rand.nextInt(4));
        }
    }

    public int getVariant() {
        return dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        dataManager.set(VARIANT, MathHelper.clamp(variant, 0, VARIANT_NAMES.length - 1));
    }

    public String getVariantName() {
        return VARIANT_NAMES[getVariant()];
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.WATER_BUCKET && isEntityAlive()) {
            if (!world.isRemote) {
                ItemStack bucket = ItemAxolotlBucket.create(this);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.setHeldItem(hand, bucket);
                    } else if (!player.inventory.addItemStackToInventory(bucket)) {
                        player.dropItem(bucket, false);
                    }
                }
                playSound(ModSoundHandler.ITEM_BUCKET_FILL_AXOLOTL, 1.0F, 1.0F);
                setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    public boolean isPlayingDead() {
        return dataManager.get(PLAYING_DEAD);
    }

    public void setPlayingDead(boolean playingDead) {
        dataManager.set(PLAYING_DEAD, playingDead);
    }

    public float getPlayingDeadAnimationFactor(float partialTicks) {
        return playingDeadAnimator.getFactor(partialTicks);
    }

    public float getInWaterAnimationFactor(float partialTicks) {
        return inWaterAnimator.getFactor(partialTicks);
    }

    public float getOnGroundAnimationFactor(float partialTicks) {
        return onGroundAnimator.getFactor(partialTicks);
    }

    public float getMovingAnimationFactor(float partialTicks) {
        return movingAnimator.getFactor(partialTicks);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if (isEntityAlive() && isWet()) {
            setAir(MAX_AIR);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!isEntityAlive()) {
            return;
        }

        if (!world.isRemote) {
            if (attackCooldown > 0) {
                attackCooldown--;
            }

            updatePlayDeadState();

            if (isInWater()) {
                setAir(MAX_AIR);
                waterTarget = null;

                if (isPlayingDead()) {
                    motionX *= 0.55D;
                    motionY *= 0.55D;
                    motionZ *= 0.55D;

                    if (motionY > -0.005D) {
                        motionY -= 0.005D;
                    }
                } else if (bucketReleaseTicks > 0) {
                    bucketReleaseTicks--;
                    swimTarget = null;
                    motionX *= 0.6D;
                    motionY *= 0.45D;
                    motionZ *= 0.6D;
                    rotationPitch += (0.0F - rotationPitch) * 0.5F;
                } else {
                    updateAttackTarget();
                    updateWaterMovement();
                }
            } else {
                swimTarget = null;
                attackTarget = null;

                if (isPlayingDead()) {
                    playDeadTicks = 0;
                    setPlayingDead(false);
                }

                updateLandMovement();
            }
        }

        tickAnimationFactors();
        updateBodyRotation();
    }

    private void tickAnimationFactors() {
        boolean playingDead = isPlayingDead();
        boolean inWaterState = !playingDead && isInWater();
        boolean onGroundState = !playingDead && !isInWater() && onGround;
        boolean movingState = !playingDead && isMovingForAnimation();

        playingDeadAnimator.tick(playingDead);
        inWaterAnimator.tick(inWaterState);
        onGroundAnimator.tick(onGroundState);
        movingAnimator.tick(movingState);
    }

    private boolean isMovingForAnimation() {
        double dx = posX - prevPosX;
        double dy = posY - prevPosY;
        double dz = posZ - prevPosZ;
        double positionDelta = dx * dx + dy * dy + dz * dz;
        double motionDelta = motionX * motionX + motionY * motionY + motionZ * motionZ;
        return positionDelta > 1.0E-5D || motionDelta > 6.0E-5D || rotationYaw != prevRotationYaw || rotationPitch != prevRotationPitch;
    }

    private void updatePlayDeadState() {
        if (playDeadTicks > 0 && isInWater()) {
            playDeadTicks--;
            setPlayingDead(true);
        } else if (isPlayingDead()) {
            playDeadTicks = 0;
            setPlayingDead(false);
        }
    }

    private void updateWaterMovement() {
        if (attackTarget != null && attackTarget.isEntityAlive()) {
            swimTarget = null;
            swimPauseTicks = 0;
            moveToward(attackTarget.posX, attackTarget.posY + attackTarget.height * 0.5D, attackTarget.posZ, 0.12D, 0.28D);
            getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
            tryAttackTarget();
            limitMotion(0.22D, 0.14D);
            return;
        }

        if (swimPauseTicks > 0) {
            swimPauseTicks--;
            motionX *= 0.94D;
            motionY *= 0.94D;
            motionZ *= 0.94D;
            limitMotion(0.08D, 0.05D);
            return;
        }

        if (targetCooldown > 0) {
            targetCooldown--;
        }

        if (swimTarget == null || targetCooldown <= 0 || !isWater(swimTarget) || getDistanceSqToTargetCenter(swimTarget) < 1.2D) {
            if (rand.nextInt(4) == 0) {
                swimTarget = null;
                swimPauseTicks = 18 + rand.nextInt(35);
                targetCooldown = swimPauseTicks;
            } else {
                swimTarget = findRandomWaterTarget();
                targetCooldown = 35 + rand.nextInt(55);
            }
        }

        if (swimTarget != null) {
            moveToward(swimTarget.getX() + 0.5D, swimTarget.getY() + 0.35D, swimTarget.getZ() + 0.5D, WATER_IDLE_SPEED, WATER_MOVE_INERTIA);
        } else {
            motionX *= 0.92D;
            motionY *= 0.92D;
            motionZ *= 0.92D;
        }

        limitMotion(0.16D, 0.10D);
    }

    private void updateAttackTarget() {
        if (attackTarget != null && !isValidAttackTarget(attackTarget)) {
            attackTarget = null;
        }

        if (attackTarget == null || ticksExisted % 20 == 0) {
            EntityGuardian guardian = findNearestGuardianTarget();
            if (guardian != null) {
                attackTarget = guardian;
            }
        }
    }

    @Nullable
    private EntityGuardian findNearestGuardianTarget() {
        AxisAlignedBB area = getEntityBoundingBox().grow(HOSTILE_TARGET_RANGE, HOSTILE_TARGET_RANGE, HOSTILE_TARGET_RANGE);
        EntityGuardian best = null;
        double bestDistance = HOSTILE_TARGET_RANGE_SQ;

        for (EntityGuardian guardian : world.getEntitiesWithinAABB(EntityGuardian.class, area)) {
            if (!isValidAttackTarget(guardian)) {
                continue;
            }

            double distance = getDistanceSq(guardian);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = guardian;
            }
        }

        return best;
    }

    private boolean isValidAttackTarget(EntityLivingBase target) {
        return target instanceof EntityGuardian
                && target.isEntityAlive()
                && target.isInWater()
                && getDistanceSq(target) <= HOSTILE_TARGET_RANGE_SQ;
    }

    private void tryAttackTarget() {
        if (attackCooldown > 0 || attackTarget == null || getDistanceSq(attackTarget) > ATTACK_REACH_SQ) {
            return;
        }

        attackEntityAsMob(attackTarget);
        attackCooldown = ATTACK_COOLDOWN_TICKS;
    }

    private void updateLandMovement() {
        if (targetCooldown > 0) {
            targetCooldown--;
        }

        if (waterTarget == null || targetCooldown <= 0 || !isWater(waterTarget)) {
            waterTarget = findNearestWaterTarget(6, 3);
            targetCooldown = 20 + rand.nextInt(20);
        }

        if (waterTarget != null) {
            moveToward(waterTarget.getX() + 0.5D, waterTarget.getY() + 0.1D, waterTarget.getZ() + 0.5D, LAND_SEEK_SPEED, onGround ? 0.26D : 0.08D);

            if (landHopCooldown > 0) {
                landHopCooldown--;
            }

            if (onGround && landHopCooldown <= 0) {
                double dx = waterTarget.getX() + 0.5D - posX;
                double dz = waterTarget.getZ() + 0.5D - posZ;
                double distance = MathHelper.sqrt(dx * dx + dz * dz);

                if (distance > 0.0001D) {
                    motionX += dx / distance * 0.055D;
                    motionZ += dz / distance * 0.055D;
                }

                motionY = 0.16D;
                landHopCooldown = 7 + rand.nextInt(7);
            }
        } else if (onGround && rand.nextInt(28) == 0) {
            motionX += (rand.nextDouble() - 0.5D) * 0.08D;
            motionY = 0.14D;
            motionZ += (rand.nextDouble() - 0.5D) * 0.08D;
            landHopCooldown = 10 + rand.nextInt(10);
        }

        if (onGround) {
            motionX *= 0.58D;
            motionZ *= 0.58D;
        } else {
            motionX *= 0.91D;
            motionZ *= 0.91D;
        }

        if (!hasNoGravity()) {
            motionY -= 0.08D;
        }

        motionY *= 0.9800000190734863D;
        limitMotion(0.12D, 0.42D);
    }

    @Nullable
    private BlockPos findRandomWaterTarget() {
        BlockPos origin = new BlockPos(this);

        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos pos = origin.add(rand.nextInt(13) - 6, rand.nextInt(7) - 3, rand.nextInt(13) - 6);

            if (isWater(pos)) {
                return pos;
            }
        }

        return isWater(origin) ? origin : null;
    }

    @Nullable
    private BlockPos findNearestWaterTarget(int horizontalRadius, int verticalRadius) {
        BlockPos origin = new BlockPos(this);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int y = -verticalRadius; y <= verticalRadius; y++) {
            for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
                for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                    BlockPos pos = origin.add(x, y, z);

                    if (isWater(pos)) {
                        double distance = origin.distanceSq(pos);

                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = pos;
                        }
                    }
                }
            }
        }

        return best;
    }

    private boolean isWater(BlockPos pos) {
        return world.isBlockLoaded(pos) && world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private void moveToward(double targetX, double targetY, double targetZ, double speed, double inertia) {
        double dx = targetX - posX;
        double dy = targetY - posY;
        double dz = targetZ - posZ;
        double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance > 0.0001D) {
            motionX += (dx / distance * speed - motionX) * inertia;
            motionY += (dy / distance * speed - motionY) * inertia;
            motionZ += (dz / distance * speed - motionZ) * inertia;
        }
    }

    private void limitMotion(double horizontalLimit, double verticalLimit) {
        motionX = MathHelper.clamp(motionX, -horizontalLimit, horizontalLimit);
        motionY = MathHelper.clamp(motionY, -verticalLimit, verticalLimit);
        motionZ = MathHelper.clamp(motionZ, -horizontalLimit, horizontalLimit);
    }

    private double getDistanceSqToTargetCenter(BlockPos pos) {
        double dx = pos.getX() + 0.5D - posX;
        double dy = pos.getY() + 0.5D - posY;
        double dz = pos.getZ() + 0.5D - posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private static class AnimationFactor {
        private int ticks;
        private int ticksOld;

        private void tick(boolean active) {
            ticksOld = ticks;

            if (active) {
                if (ticks < ANIMATION_BLEND_TICKS) {
                    ticks++;
                }
            } else if (ticks > 0) {
                ticks--;
            }
        }

        private float getFactor(float partialTicks) {
            float tick = ticksOld + (ticks - ticksOld) * MathHelper.clamp(partialTicks, 0.0F, 1.0F);
            float linear = MathHelper.clamp(tick / (float)ANIMATION_BLEND_TICKS, 0.0F, 1.0F);
            return (1.0F - MathHelper.cos(linear * (float)Math.PI)) * 0.5F;
        }

        private void reset(boolean active) {
            ticks = active ? ANIMATION_BLEND_TICKS : 0;
            ticksOld = ticks;
        }
    }

    private void updateBodyRotation() {
        double horizontalMotion = motionX * motionX + motionZ * motionZ;

        if (horizontalMotion > 1.0E-5D) {
            float targetYaw = -((float)MathHelper.atan2(motionX, motionZ)) * (180F / (float)Math.PI);
            renderYawOffset += MathHelper.wrapDegrees(targetYaw - renderYawOffset) * 0.16F;
            rotationYaw = renderYawOffset;
        }

        if (isInWater() && !isPlayingDead()) {
            float horizontal = MathHelper.sqrt(horizontalMotion);
            float targetPitch = -((float)MathHelper.atan2(motionY, horizontal)) * (180F / (float)Math.PI);
            rotationPitch += (targetPitch - rotationPitch) * 0.14F;
        } else {
            rotationPitch += (0.0F - rotationPitch) * 0.18F;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        move(MoverType.SELF, motionX, motionY, motionZ);

        if (isInWater()) {
            motionX *= WATER_DRAG;
            motionY *= WATER_DRAG;
            motionZ *= WATER_DRAG;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        float healthBeforeDamage = getHealth();

        if (!world.isRemote && shouldStartPlayingDead(source, amount, healthBeforeDamage)) {
            startPlayingDead();
        }

        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)getAttackDamageAttribute().getAttributeValue());
        if (attacked) {
            playSound(ModSoundHandler.ENTITY_AXOLOTL_ATTACK, 1.0F, 1.0F);
        }
        return attacked;
    }

    private boolean shouldStartPlayingDead(DamageSource source, float amount, float healthBeforeDamage) {
        return !isPlayingDead()
                && isInWater()
                && amount < healthBeforeDamage
                && (source.getTrueSource() != null || source.getImmediateSource() != null)
                && rand.nextInt(3) == 0
                && ((float)rand.nextInt(3) < amount || healthBeforeDamage / getMaxHealth() < 0.5F);
    }

    private void startPlayingDead() {
        playDeadTicks = TOTAL_PLAY_DEAD_TIME;
        setPlayingDead(true);
        swimTarget = null;
        waterTarget = null;
        motionX *= 0.25D;
        motionY *= 0.25D;
        motionZ *= 0.25D;
        addPotionEffect(new PotionEffect(MobEffects.REGENERATION, TOTAL_PLAY_DEAD_TIME, 0));
    }

    public void resetBucketReleasePose(float yaw) {
        rotationYaw = yaw;
        prevRotationYaw = yaw;
        renderYawOffset = yaw;
        prevRenderYawOffset = yaw;
        rotationYawHead = yaw;
        prevRotationYawHead = yaw;
        rotationPitch = 0.0F;
        prevRotationPitch = 0.0F;
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        swimTarget = null;
        waterTarget = null;
        swimPauseTicks = 0;
        targetCooldown = 12;
        bucketReleaseTicks = 12;
        movingAnimator.reset(false);
        inWaterAnimator.reset(isInWater());
        onGroundAnimator.reset(false);
        playingDeadAnimator.reset(false);
    }

    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
        double distance = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);

        if (distance <= 0.0001D) {
            return;
        }

        double normalizedX = xRatio / distance;
        double normalizedZ = zRatio / distance;

        if (isInWater()) {
            double impulse = isPlayingDead() ? 0.01D : Math.min(0.045D, strength * 0.09D);
            motionX -= normalizedX * impulse;
            motionZ -= normalizedZ * impulse;

            if (!isPlayingDead()) {
                motionY += 0.012D;
            }

            motionX *= 0.62D;
            motionY *= 0.55D;
            motionZ *= 0.62D;
            velocityChanged = true;
        } else {
            motionX -= normalizedX * Math.min(0.09D, strength * 0.18D);
            motionZ -= normalizedZ * Math.min(0.09D, strength * 0.18D);

            if (onGround) {
                motionY = Math.min(0.2D, motionY + strength * 0.25D);
            }

            velocityChanged = true;
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return world.getBlockState(pos).getMaterial().isLiquid() && pos.getY() < world.getSeaLevel() && super.getCanSpawnHere();
    }

    @Override
    public float getEyeHeight() {
        return height * 0.55F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isPlayingDead() ? null : isInWater() ? ModSoundHandler.ENTITY_AXOLOTL_IDLE_WATER : ModSoundHandler.ENTITY_AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSoundHandler.ENTITY_AXOLOTL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundHandler.ENTITY_AXOLOTL_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return ModSoundHandler.ENTITY_AXOLOTL_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return ModSoundHandler.ENTITY_AXOLOTL_SPLASH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", getVariant());
        compound.setInteger("PlayingDeadTicks", playDeadTicks);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("Variant")) {
            setVariant(compound.getInteger("Variant"));
        }

        if (compound.hasKey("PlayingDeadTicks")) {
            playDeadTicks = compound.getInteger("PlayingDeadTicks");
            setPlayingDead(playDeadTicks > 0);
        }
    }
}
