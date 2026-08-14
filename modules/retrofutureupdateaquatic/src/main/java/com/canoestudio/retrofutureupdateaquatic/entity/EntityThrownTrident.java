package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import com.canoestudio.retrofutureupdateaquatic.enchantment.ModEnchantments;
import com.canoestudio.retrofutureupdateaquatic.item.ItemTrident;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Trident projectile adapted from the mature Future-MC/Oceanic Expanse
 * EntityArrow implementation.  EntityArrow supplies collision, ground state,
 * pickup and vanilla projectile persistence; this subclass only adds trident
 * damage, Fluidlogged water motion and Loyalty owner recovery.
 */
public class EntityThrownTrident extends EntityArrow {

    private ItemStack tridentStack = new ItemStack(ModItems.TRIDENT);
    private boolean dealtDamage;
    private boolean canReturn;
    private boolean returning;
    @Nullable
    private UUID shooterId;

    public EntityThrownTrident(World worldIn) {
        super(worldIn);
        this.setDamage(8.0D);
    }

    public EntityThrownTrident(World worldIn, EntityLivingBase throwerIn, ItemStack stack) {
        super(worldIn, throwerIn);
        this.setDamage(8.0D);
        this.tridentStack = stack.copy();
        this.tridentStack.setCount(1);
        this.shooterId = throwerIn.getPersistentID();
    }

    public ItemStack getTridentStack() {
        return this.tridentStack;
    }

    @Override
    protected ItemStack getArrowStack() {
        return this.tridentStack.copy();
    }

    @Override
    public void onUpdate() {
        resolveShooter();

        if (this.timeInGround > 4) {
            this.canReturn = true;
        }

        if (getLoyaltyLevel() > 0 && this.canReturn) {
            updateReturning();
            if (this.returning) {
                this.inGround = false;
                this.noClip = true;
                this.setNoGravity(true);
            }
        }

        if (!this.world.isRemote && !this.returning && (this.dealtDamage || this.inGround)
                && this.ticksExisted > 10 && getLoyaltyLevel() <= 0) {
            dropTrident();
            return;
        }

        if (this.tridentStack.isEmpty() || this.tridentStack.getItemDamage() >= this.tridentStack.getMaxDamage()) {
            this.setDead();
            return;
        }

        if (!this.returning && FluidloggedSupport.isEntityInWater(this)) {
            this.motionX *= 0.98D;
            this.motionY *= 0.98D;
            this.motionZ *= 0.98D;
        }

        // EntityArrow's normal water test does not see Fluidlogged API state.
        // Returning false avoids its hard-coded 0.6 drag; the Fluidlogged drag
        // above is applied explicitly while vanilla gravity remains active.
        super.onUpdate();
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public boolean hasNoGravity() {
        // A thrown trident still sinks in water.  Only the Loyalty return
        // phase suppresses gravity, matching the mature 1.12.2 projectile
        // behaviour while FluidloggedSupport supplies the water drag.
        return this.returning;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (this.returning || this.dealtDamage) {
            return;
        }

        Entity target = result.entityHit;
        if (target == null) {
            super.onHit(result);
            this.canReturn = true;
            tryChanneling(result);
            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 0.8F);
            return;
        }

        EntityLivingBase owner = this.shootingEntity instanceof EntityLivingBase
            ? (EntityLivingBase)this.shootingEntity : null;
        DamageSource source = owner == null
            ? new EntityDamageSourceIndirect("trident", this, this)
            : new EntityDamageSourceIndirect("trident", this, owner);
        float damage = target instanceof EntityLivingBase
            ? ItemTrident.getThrownDamage(this.tridentStack, (EntityLivingBase)target) : 8.0F;
        if (target.attackEntityFrom(source, damage)) {
            target.hurtResistantTime = 0;
            if (owner instanceof EntityPlayer) {
                ((EntityPlayer)owner).onEnchantmentCritical(target);
            }
            tryChanneling(result);
            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 0.8F);
        }
        this.dealtDamage = true;
        this.canReturn = true;
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (this.world.isRemote || this.arrowShake > 0 || (!this.inGround && !this.returning)) {
            return;
        }
        EntityLivingBase owner = this.shootingEntity instanceof EntityLivingBase
            ? (EntityLivingBase)this.shootingEntity : null;
        if (owner == null || owner.getPersistentID() == null
                || !owner.getPersistentID().equals(player.getPersistentID())) {
            return;
        }
        if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED
                && !player.inventory.addItemStackToInventory(this.tridentStack.copy())) {
            return;
        }
        if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED
                || this.pickupStatus == EntityArrow.PickupStatus.CREATIVE_ONLY
                    && player.capabilities.isCreativeMode) {
            player.onItemPickup(this, 1);
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("Trident", this.tridentStack.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("DealtDamage", this.dealtDamage);
        compound.setBoolean("CanReturn", this.canReturn);
        compound.setBoolean("Returning", this.returning);
        if (this.shootingEntity != null) {
            this.shooterId = this.shootingEntity.getPersistentID();
        }
        if (this.shooterId != null) {
            compound.setUniqueId("Shooter", this.shooterId);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Trident", 10)) {
            this.tridentStack = new ItemStack(compound.getCompoundTag("Trident"));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
        this.canReturn = compound.getBoolean("CanReturn");
        this.returning = compound.getBoolean("Returning");
        if (compound.hasUniqueId("Shooter")) {
            this.shooterId = compound.getUniqueId("Shooter");
        }
        if (this.tridentStack.isEmpty()) {
            this.tridentStack = new ItemStack(ModItems.TRIDENT);
        }
    }

    private void resolveShooter() {
        if (this.shootingEntity != null || this.shooterId == null || !(this.world instanceof WorldServer)) {
            return;
        }
        Entity entity = ((WorldServer)this.world).getEntityFromUuid(this.shooterId);
        if (entity instanceof EntityLivingBase) {
            this.shootingEntity = entity;
        }
    }

    private void updateReturning() {
        if (this.shootingEntity == null || !this.shootingEntity.isEntityAlive()) {
            this.returning = false;
            if (!this.world.isRemote) {
                dropTrident();
            }
            return;
        }

        Vec3d vector = new Vec3d(this.shootingEntity.posX - this.posX,
            this.shootingEntity.posY + this.shootingEntity.getEyeHeight() - this.posY,
            this.shootingEntity.posZ - this.posZ);
        if (!this.world.isRemote && vector.squareDistanceTo(Vec3d.ZERO) < 1.5625D) {
            returnToOwner((EntityLivingBase)this.shootingEntity);
            return;
        }

        this.returning = true;
        this.noClip = true;
        this.setNoGravity(true);
        Vec3d pull = vector.normalize().scale(0.05D * getLoyaltyLevel());
        this.motionX = this.motionX * 0.95D + pull.x;
        this.motionY = this.motionY * 0.95D + pull.y;
        this.motionZ = this.motionZ * 0.95D + pull.z;
    }

    private void returnToOwner(EntityLivingBase owner) {
        if (owner instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)owner;
            if (player.capabilities.isCreativeMode || player.inventory.addItemStackToInventory(this.tridentStack.copy())) {
                this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.8F, 1.0F);
                this.setDead();
                return;
            }
        }
        dropTrident();
    }

    private void dropTrident() {
        if (!this.world.isRemote) {
            this.entityDropItem(this.tridentStack.copy(), 0.1F);
        }
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
