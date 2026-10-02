package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAIPhantomCircle;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAIPhantomSwoop;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAIPhantomTarget;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.sounds.ModSounds;
import com.canoestudio.retrofuturemccore.api.entity.RetroEntityAttributes;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/**
 * 1.13 幻翼的 1.12.2 适配实体。
 *
 * <p>飞行行为采用参考 Phantoms 模组的三段式 AI：目标选择、环绕和俯冲。
 * 关键点是使用 EntityFlying 的三维移动管线，并让 MoveHelper 平滑改变
 * motion/rotation，而不是每 tick 直接瞬移到一个新位置。</p>
 */
public class EntityPhantom extends EntityFlying implements IMob {

    private static final DataParameter<Integer> SIZE =
        EntityDataManager.createKey(EntityPhantom.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> ATTACK_STATE =
        EntityDataManager.createKey(EntityPhantom.class, DataSerializers.BYTE);

    private AttackState attackState = AttackState.CIRCLING;
    private BlockPos targetPos = BlockPos.ORIGIN;
    private int swoopCooldown;

    public EntityPhantom(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 0.5F);
        this.experienceValue = 5;
        this.moveHelper = new PhantomMoveHelper(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SIZE, 0);
        this.dataManager.register(ATTACK_STATE, (byte)AttackState.CIRCLING.ordinal());
    }

    @Override
    protected void initEntityAI() {
        // 参考成熟实现的优先级：攻击打断环绕，目标选择独立于移动目标。
        this.tasks.addTask(1, new EntityAIPhantomSwoop(this));
        this.tasks.addTask(2, new EntityAIPhantomCircle(this));
        this.targetTasks.addTask(1, new EntityAIPhantomTarget(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MAX_HEALTH, 20.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.ATTACK_DAMAGE, 6.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.FOLLOW_RANGE, 64.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MOVEMENT_SPEED, 0.35D);
        this.setPhantomSize(0);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
            @Nullable IEntityLivingData livingdata) {
        // 当前版本没有额外配置尺寸，保留原版默认尺寸，同时走 EntityLiving 的标准初始化。
        this.setPhantomSize(0);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            this.spawnWingParticles();
            return;
        }

        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.setDead();
            return;
        }

        if (this.getMoveHelper().isUpdating()) {
            this.getLookHelper().setLookPosition(this.getMoveHelper().getX(),
                this.getMoveHelper().getY(), this.getMoveHelper().getZ(), 180.0F, 90.0F);
        }
        this.burnInDaylight();
    }

    /**
     * EntityFlying 的 travel 会负责实际碰撞移动和 limbSwing 累积；MoveHelper
     * 只负责生成平滑的三维速度，避免移动和动画脱节。
     */
    @Override
    public void travel(float strafe, float vertical, float forward) {
        super.travel(strafe, vertical, forward);
        this.rotationYawHead = this.rotationYaw;
    }

    private void spawnWingParticles() {
        float offset = this.getEntityId() * 3.0F + this.ticksExisted;
        float flap = MathHelper.cos(offset * 7.448451F * (float)Math.PI / 180.0F + (float)Math.PI);
        float nextFlap = MathHelper.cos((offset + 1.0F) * 7.448451F
            * (float)Math.PI / 180.0F + (float)Math.PI);
        if (flap > 0.0F && nextFlap <= 0.0F) {
            this.playSound(ModSounds.PHANTOM_FLAP, 0.95F + this.rand.nextFloat() * 0.05F,
                0.95F + this.rand.nextFloat() * 0.05F);
        }

        float angle = this.rotationYawHead * (float)Math.PI / 180.0F;
        float wingX = MathHelper.cos(angle) * (1.3F + 0.21F * this.getPhantomSize());
        float wingY = (0.3F - flap * 0.45F) * (this.getPhantomSize() * 0.2F + 1.0F);
        float wingZ = MathHelper.sin(angle) * (1.3F + 0.21F * this.getPhantomSize());
        this.world.spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX + wingX,
            this.posY + wingY, this.posZ + wingZ, 0.0D, 0.0D, 0.0D);
        this.world.spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX - wingX,
            this.posY + wingY, this.posZ - wingZ, 0.0D, 0.0D, 0.0D);
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
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.getAttackState() == AttackState.SWOOPING) {
            this.setAttackState(AttackState.CIRCLING);
            this.swoopCooldown = 60 + this.rand.nextInt(60);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = superAttackEntityAsMob(entityIn);
        if (attacked) {
            this.playSound(ModSounds.PHANTOM_BITE, 0.8F,
                0.95F + this.rand.nextFloat() * 0.1F);
        }
        return attacked;
    }

    /** IMob 不携带 EntityMob 的近战实现，这里保留其标准伤害/附魔/击退行为。 */
    private boolean superAttackEntityAsMob(Entity entityIn) {
        float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
            .getAttributeValue();
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source != DamageSource.DROWN && super.isEntityInvulnerable(source);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isCreatureType(net.minecraft.entity.EnumCreatureType type, boolean forSpawnCount) {
        return type == net.minecraft.entity.EnumCreatureType.MONSTER;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
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

    public boolean canTarget(@Nullable EntityLivingBase target) {
        if (!(target instanceof EntityPlayer) || !target.isEntityAlive()) {
            return false;
        }
        EntityPlayer player = (EntityPlayer)target;
        return !player.capabilities.isCreativeMode && !player.isSpectator()
            && this.getDistanceSq(player) < 4096.0D && this.canEntityBeSeen(player);
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

    public AttackState getAttackState() {
        int ordinal = this.dataManager.get(ATTACK_STATE);
        if (ordinal < 0 || ordinal >= AttackState.values().length) {
            return AttackState.CIRCLING;
        }
        return AttackState.values()[ordinal];
    }

    public void setAttackState(AttackState state) {
        this.attackState = state == null ? AttackState.CIRCLING : state;
        this.dataManager.set(ATTACK_STATE, (byte)this.attackState.ordinal());
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos == null ? BlockPos.ORIGIN : targetPos;
    }

    public boolean isSwooping() {
        return this.getAttackState() == AttackState.SWOOPING;
    }

    public int getSwoopCooldown() {
        return this.swoopCooldown;
    }

    public void setSwoopCooldown(int ticks) {
        this.swoopCooldown = Math.max(0, ticks);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Size", this.getPhantomSize());
        compound.setInteger("AX", this.targetPos.getX());
        compound.setInteger("AY", this.targetPos.getY());
        compound.setInteger("AZ", this.targetPos.getZ());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPhantomSize(compound.getInteger("Size"));
        this.targetPos = new BlockPos(compound.getInteger("AX"), compound.getInteger("AY"),
            compound.getInteger("AZ"));
    }

    public enum AttackState {
        CIRCLING,
        SWOOPING
    }

    /**
     * 参考 Atlas FlyingMoveControl 的最小 1.12.2 等价实现。
     * MoveHelper 每 tick 调整目标速度和姿态，EntityFlying.travel 再完成碰撞移动。
     */
    private static final class PhantomMoveHelper extends EntityMoveHelper {

        private final EntityPhantom phantom;

        private PhantomMoveHelper(EntityPhantom phantom) {
            super(phantom);
            this.phantom = phantom;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action != Action.MOVE_TO) {
                this.phantom.motionX *= 0.94D;
                this.phantom.motionY *= 0.94D;
                this.phantom.motionZ *= 0.94D;
                return;
            }

            this.action = Action.WAIT;
            double dx = this.posX - this.phantom.posX;
            double dy = this.posY - this.phantom.posY;
            double dz = this.posZ - this.phantom.posZ;
            double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 0.25D) {
                this.phantom.motionX *= 0.88D;
                this.phantom.motionY *= 0.88D;
                this.phantom.motionZ *= 0.88D;
                return;
            }

            double targetSpeed = this.phantom.isSwooping() ? 0.38D : 0.20D;
            double turnRate = this.phantom.isSwooping() ? 0.18D : 0.12D;
            this.phantom.motionX += (dx / distance * targetSpeed - this.phantom.motionX) * turnRate;
            this.phantom.motionY += (dy / distance * targetSpeed - this.phantom.motionY) * turnRate;
            this.phantom.motionZ += (dz / distance * targetSpeed - this.phantom.motionZ) * turnRate;

            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal > 1.0E-4D) {
                float yaw = -((float)MathHelper.atan2(dx, dz)) * (180F / (float)Math.PI);
                this.phantom.rotationYaw = this.limitAngle(this.phantom.rotationYaw, yaw,
                    this.phantom.isSwooping() ? 18.0F : 10.0F);
            }
            float pitch = -((float)MathHelper.atan2(dy, horizontal)) * (180F / (float)Math.PI);
            float pitchDelta = MathHelper.wrapDegrees(pitch - this.phantom.rotationPitch);
            pitchDelta = MathHelper.clamp(pitchDelta,
                this.phantom.isSwooping() ? -14.0F : -8.0F,
                this.phantom.isSwooping() ? 14.0F : 8.0F);
            this.phantom.rotationPitch += pitchDelta;
            this.phantom.renderYawOffset = this.phantom.rotationYaw;
            this.phantom.rotationYawHead = this.phantom.rotationYaw;
        }
    }
}
