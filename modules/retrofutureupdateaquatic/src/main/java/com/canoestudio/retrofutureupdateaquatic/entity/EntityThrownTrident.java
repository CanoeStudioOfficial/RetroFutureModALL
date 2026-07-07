package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityThrownTrident extends EntityThrowable {

    private ItemStack tridentStack = new ItemStack(ModItems.TRIDENT);
    private boolean dealtDamage;

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
        if (!this.world.isRemote && (this.dealtDamage || this.inGround) && this.ticksExisted > 10) {
            EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, this.tridentStack.copy());
            item.setPickupDelay(10);
            this.world.spawnEntity(item);
            this.setDead();
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
            if (result.entityHit.attackEntityFrom(source, 8.0F)) {
                result.entityHit.hurtResistantTime = 0;
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
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Trident")) {
            this.tridentStack = new ItemStack(compound.getCompoundTag("Trident"));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
        if (this.tridentStack.isEmpty()) {
            this.tridentStack = new ItemStack(ModItems.TRIDENT);
        }
    }
}
