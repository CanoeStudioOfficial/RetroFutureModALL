package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.enchantment.ModEnchantments;
import com.canoestudio.retrofutureupdateaquatic.item.ItemTrident;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityThrownTrident extends EntityThrowable {

    private ItemStack tridentStack = new ItemStack(ModItems.TRIDENT);
    private boolean dealtDamage;
    private int returningTicks;

    public EntityThrownTrident(World worldIn) {
        super(worldIn);
    }

    public EntityThrownTrident(World worldIn, EntityLivingBase throwerIn, ItemStack stack) {
        super(worldIn, throwerIn);
        this.tridentStack = stack.copy();
        this.tridentStack.setCount(1);
    }

    public ItemStack getTridentStack() {
        return this.tridentStack;
    }

    @Override
    protected float getGravityVelocity() {
        return this.isInWater() ? 0.01F : 0.03F;
    }

    @Override
    public void onUpdate() {
        if (shouldReturnToOwner()) {
            updateReturning();
            return;
        }

        super.onUpdate();
        if (this.isInWater()) {
            this.motionX *= 0.93D;
            this.motionY *= 0.93D;
            this.motionZ *= 0.93D;
            for (int i = 0; i < 2; i++) {
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY, this.posZ,
                    -this.motionX * 0.25D, -this.motionY * 0.25D, -this.motionZ * 0.25D);
            }
        }
        if (!this.world.isRemote && (this.dealtDamage || this.inGround) && this.ticksExisted > 10
                && getLoyaltyLevel() <= 0) {
            dropTrident();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (this.world.isRemote || this.dealtDamage) {
            return;
        }
        if (result.entityHit != null) {
            EntityLivingBase thrower = getThrower();
            DamageSource source = thrower == null ? DamageSource.causeThrownDamage(this, this)
                : DamageSource.causeThrownDamage(this, thrower);
            float damage = result.entityHit instanceof EntityLivingBase
                ? ItemTrident.getThrownDamage(this.tridentStack, (EntityLivingBase) result.entityHit) : 8.0F;
            if (result.entityHit.attackEntityFrom(source, damage)) {
                result.entityHit.hurtResistantTime = 0;
                if (thrower instanceof EntityPlayer) {
                    ((EntityPlayer) thrower).onEnchantmentCritical(result.entityHit);
                }
                tryChanneling(result);
                this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 0.8F);
            }
            this.dealtDamage = true;
        } else {
            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 0.6F);
            this.dealtDamage = true;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("Trident", this.tridentStack.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("DealtDamage", this.dealtDamage);
        compound.setInteger("ReturningTicks", this.returningTicks);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Trident")) {
            this.tridentStack = new ItemStack(compound.getCompoundTag("Trident"));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
        this.returningTicks = compound.getInteger("ReturningTicks");
        if (this.tridentStack.isEmpty()) {
            this.tridentStack = new ItemStack(ModItems.TRIDENT);
        }
    }

    private boolean shouldReturnToOwner() {
        return getLoyaltyLevel() > 0 && (this.dealtDamage || this.inGround);
    }

    private void updateReturning() {
        EntityLivingBase owner = getThrower();
        if (!this.world.isRemote && (owner == null || !owner.isEntityAlive())) {
            dropTrident();
            return;
        }

        this.noClip = true;
        this.returningTicks++;
        Vec3d target = new Vec3d(owner.posX - this.posX, owner.posY + owner.getEyeHeight() * 0.5D - this.posY,
            owner.posZ - this.posZ);
        double distance = MathHelper.sqrt(target.x * target.x + target.y * target.y + target.z * target.z);
        if (!this.world.isRemote && distance < 1.25D) {
            returnToOwner(owner);
            return;
        }

        double speed = 0.08D * getLoyaltyLevel() + 0.12D;
        Vec3d pull = target.normalize().scale(speed);
        this.motionX = this.motionX * 0.78D + pull.x;
        this.motionY = this.motionY * 0.78D + pull.y;
        this.motionZ = this.motionZ * 0.78D + pull.z;
        moveReturning();
        if (!this.world.isRemote && this.returningTicks > 200) {
            dropTrident();
        }
    }

    private void moveReturning() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float horizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, horizontal) * (180D / Math.PI));
        this.setPosition(this.posX, this.posY, this.posZ);
        if (this.world.isRemote) {
            this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY, this.posZ,
                -this.motionX * 0.1D, -this.motionY * 0.1D, -this.motionZ * 0.1D);
        }
    }

    private void returnToOwner(EntityLivingBase owner) {
        if (owner instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) owner;
            if (player.capabilities.isCreativeMode || player.inventory.addItemStackToInventory(this.tridentStack.copy())) {
                this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.8F, 1.0F);
                this.setDead();
                return;
            }
        }
        dropTrident();
    }

    private void dropTrident() {
        EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, this.tridentStack.copy());
        item.setPickupDelay(10);
        this.world.spawnEntity(item);
        this.setDead();
    }

    private void tryChanneling(RayTraceResult result) {
        if (EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CHANNELING, this.tridentStack) <= 0
                || !this.world.isThundering()) {
            return;
        }
        BlockPos strikePos = result.entityHit == null ? result.getBlockPos() : new BlockPos(result.entityHit);
        if (strikePos != null && this.world.canSeeSky(strikePos)) {
            this.world.addWeatherEffect(new EntityLightningBolt(this.world, strikePos.getX() + 0.5D,
                strikePos.getY(), strikePos.getZ() + 0.5D, false));
        }
    }

    private int getLoyaltyLevel() {
        return EnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOYALTY, this.tridentStack);
    }
}
