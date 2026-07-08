package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.compat.AquaticCompat;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import com.canoestudio.retrofutureupdateaquatic.world.AquaticStructureData;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityDolphin extends EntityWaterMob {

    @Nullable
    private BlockPos swimTarget;
    @Nullable
    private BlockPos treasureTarget;
    private int targetCooldown;
    private int jumpCooldown;
    private int treasureTicks;
    private int playItemCooldown;
    private int heldItemTicks;

    public EntityDolphin(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 0.6F);
        this.setAir(4800);
        this.setCanPickUpLoot(true);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (isDolphinFood(held) && this.isEntityAlive()) {
            if (!this.world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EAT,
                    SoundCategory.NEUTRAL, 0.7F, 1.05F + this.rand.nextFloat() * 0.12F);
                startTreasureSearch(new BlockPos(this));
            }
            return true;
        }

        return super.processInteract(player, hand);
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
            if (!this.world.isRemote) {
                updatePlayWithItems();
            }
        } else {
            this.motionY -= 0.08D;
            this.motionX *= 0.98D;
            this.motionZ *= 0.98D;
        }
        updateRotation();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt && !this.world.isRemote && source.getTrueSource() instanceof EntityLivingBase) {
            alertNearbyDolphins((EntityLivingBase)source.getTrueSource());
        }
        return hurt;
    }

    private void alertNearbyDolphins(EntityLivingBase attacker) {
        for (EntityDolphin dolphin : this.world.getEntitiesWithinAABB(EntityDolphin.class,
                this.getEntityBoundingBox().grow(16.0D, 6.0D, 16.0D))) {
            if (dolphin != this && dolphin.isEntityAlive()) {
                dolphin.setAttackTarget(attacker);
                dolphin.clearTreasureTarget();
            }
        }
    }

    private void updatePlayWithItems() {
        ItemStack held = this.getHeldItemMainhand();
        if (!held.isEmpty()) {
            if (++this.heldItemTicks >= 30 + this.rand.nextInt(35)) {
                dropHeldPlayItem(held);
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.heldItemTicks = 0;
                this.playItemCooldown = 60 + this.rand.nextInt(120);
            }
            return;
        }

        this.heldItemTicks = 0;
        if (this.playItemCooldown > 0) {
            this.playItemCooldown--;
            return;
        }
        if (this.treasureTarget != null || this.getAttackTarget() != null || this.rand.nextInt(20) != 0) {
            return;
        }

        EntityItem item = findPlayItem();
        if (item == null) {
            this.playItemCooldown = 40 + this.rand.nextInt(80);
            return;
        }

        if (this.getDistanceSq(item) > 1.44D) {
            moveToward(item.posX, item.posY + 0.1D, item.posZ, 0.18D, 0.22D);
            this.swimTarget = new BlockPos(item);
            this.targetCooldown = 12;
            return;
        }

        ItemStack stack = item.getItem();
        ItemStack pickedUp = stack.copy();
        pickedUp.setCount(1);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, pickedUp);
        stack.shrink(1);
        if (stack.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(stack);
        }
        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.7F, 1.25F);
        this.heldItemTicks = 0;
    }

    @Nullable
    private EntityItem findPlayItem() {
        java.util.List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class,
            this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D));
        EntityItem best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EntityItem item : items) {
            if (!item.isEntityAlive() || item.getItem().isEmpty() || !item.isInWater()) {
                continue;
            }
            double distance = this.getDistanceSq(item);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = item;
            }
        }
        return best;
    }

    private void dropHeldPlayItem(ItemStack stack) {
        ItemStack dropped = stack.copy();
        EntityItem item = new EntityItem(this.world, this.posX, this.posY + this.getEyeHeight() - 0.3D, this.posZ,
            dropped);
        item.setPickupDelay(40);
        float dir = this.rand.nextFloat() * ((float)Math.PI * 2.0F);
        float scatter = 0.02F * this.rand.nextFloat();
        item.motionX = 0.3D * -Math.sin(this.rotationYaw * 0.017453292F)
            * Math.cos(this.rotationPitch * 0.017453292F) + Math.cos(dir) * scatter;
        item.motionY = 0.3D * Math.sin(this.rotationPitch * 0.017453292F) * 1.5D;
        item.motionZ = 0.3D * Math.cos(this.rotationYaw * 0.017453292F)
            * Math.cos(this.rotationPitch * 0.017453292F) + Math.sin(dir) * scatter;
        this.world.spawnEntity(item);
        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.5F, 0.85F);
    }

    private void updateWaterMovement() {
        if (this.targetCooldown > 0) {
            this.targetCooldown--;
        }

        if (this.treasureTicks > 0 && this.treasureTarget != null) {
            this.treasureTicks--;
            if (distanceSqToHorizontalCenter(this.treasureTarget) < 16.0D) {
                clearTreasureTarget();
            } else {
                BlockPos guidedTarget = findTreasureStepTarget();
                if (guidedTarget != null) {
                    this.swimTarget = guidedTarget;
                    this.targetCooldown = 12;
                    moveToward(guidedTarget.getX() + 0.5D, guidedTarget.getY() + 0.45D,
                        guidedTarget.getZ() + 0.5D, 0.22D, 0.26D);
                } else {
                    moveToward(this.treasureTarget.getX() + 0.5D, this.posY,
                        this.treasureTarget.getZ() + 0.5D, 0.12D, 0.16D);
                }
            }
        } else if (this.treasureTarget != null) {
            clearTreasureTarget();
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

    private void startTreasureSearch(BlockPos origin) {
        BlockPos found = AquaticStructureData.findNearestDolphinLocated(this.world, origin, 800);
        if (found != null) {
            this.treasureTarget = found.toImmutable();
            this.treasureTicks = 2400;
            this.targetCooldown = 0;
            this.swimTarget = null;
            this.playSound(SoundEvents.ENTITY_PLAYER_SPLASH, 0.8F, 1.4F);
        } else {
            this.treasureTicks = 120;
            this.playSound(SoundEvents.ENTITY_SQUID_AMBIENT, 0.8F, 1.25F);
        }
    }

    private void clearTreasureTarget() {
        this.treasureTarget = null;
        this.treasureTicks = 0;
    }

    @Nullable
    private BlockPos findTreasureStepTarget() {
        if (this.treasureTarget == null) {
            return null;
        }

        BlockPos origin = new BlockPos(this);
        double targetX = this.treasureTarget.getX() + 0.5D;
        double targetZ = this.treasureTarget.getZ() + 0.5D;
        double dx = targetX - this.posX;
        double dz = targetZ - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dz * dz);
        if (distance < 0.001D) {
            return origin;
        }

        int step = Math.min(16, Math.max(5, MathHelper.floor(distance * 0.35D)));
        int baseX = MathHelper.floor(this.posX + dx / distance * step);
        int baseZ = MathHelper.floor(this.posZ + dz / distance * step);
        int bestY = MathHelper.floor(this.posY);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dy = 2; dy >= -4; dy--) {
            BlockPos candidate = new BlockPos(baseX, bestY + dy, baseZ);
            if (isWater(candidate)) {
                double candidateDistance = candidate.distanceSq(this.treasureTarget);
                if (candidateDistance < bestDistance) {
                    bestDistance = candidateDistance;
                    best = candidate;
                }
            }
        }

        if (best != null) {
            return best;
        }

        for (int i = 0; i < 12; i++) {
            BlockPos candidate = origin.add(this.rand.nextInt(9) - 4, this.rand.nextInt(5) - 2,
                this.rand.nextInt(9) - 4);
            if (isWater(candidate) && candidate.distanceSq(this.treasureTarget) < origin.distanceSq(this.treasureTarget)) {
                return candidate;
            }
        }

        return null;
    }

    private void grantNearbyPlayersGrace() {
        for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class,
                this.getEntityBoundingBox().grow(8.0D))) {
            if (player.isInWater() && (player.isSprinting() || AquaticCompat.isActuallySwimming(player))) {
                player.addPotionEffect(ModPotions.dolphinsGrace(120));
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

    private double distanceSqToHorizontalCenter(BlockPos pos) {
        double dx = pos.getX() + 0.5D - this.posX;
        double dz = pos.getZ() + 0.5D - this.posZ;
        return dx * dx + dz * dz;
    }

    private static boolean isDolphinFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == ModItems.COD || item == ModItems.SALMON || item == ModItems.TROPICAL_FISH
                || item == ModItems.PUFFERFISH) {
            return true;
        }
        if (item == Items.FISH && stack.getMetadata() <= 3) {
            return true;
        }
        return false;
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

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.treasureTarget != null) {
            compound.setInteger("TreasureX", this.treasureTarget.getX());
            compound.setInteger("TreasureY", this.treasureTarget.getY());
            compound.setInteger("TreasureZ", this.treasureTarget.getZ());
            compound.setInteger("TreasureTicks", this.treasureTicks);
        }
        compound.setInteger("PlayItemCooldown", this.playItemCooldown);
        compound.setInteger("HeldItemTicks", this.heldItemTicks);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("TreasureTicks")) {
            this.treasureTarget = new BlockPos(compound.getInteger("TreasureX"), compound.getInteger("TreasureY"),
                compound.getInteger("TreasureZ"));
            this.treasureTicks = compound.getInteger("TreasureTicks");
        }
        this.playItemCooldown = compound.getInteger("PlayItemCooldown");
        this.heldItemTicks = compound.getInteger("HeldItemTicks");
    }
}
