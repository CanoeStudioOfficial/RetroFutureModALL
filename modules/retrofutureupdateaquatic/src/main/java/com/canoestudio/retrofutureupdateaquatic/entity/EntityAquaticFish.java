package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.item.ItemFishBucket;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAIWanderUnderwater;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;

public class EntityAquaticFish extends EntityAnimal {

    private static final DataParameter<Integer> TROPICAL_VARIANT =
        EntityDataManager.createKey(EntityAquaticFish.class, DataSerializers.VARINT);
    private final AquaticFishType fishType;
    private int flopCooldown;

    protected EntityAquaticFish(World worldIn, AquaticFishType fishType) {
        super(worldIn);
        this.fishType = fishType;
        this.setSize(fishType.getWidth(), fishType.getHeight());
        this.moveHelper = new FishMoveHelper(this);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /** Mature OE 1.12.2 AI layout, retained for all four vanilla fish types. */
    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 8.0F, 1.6D, 1.4D));
        this.tasks.addTask(2, new EntityAIWanderUnderwater(this, 1.0D, 20, true));
        this.tasks.addTask(3, new EntityAILookIdle(this));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TROPICAL_VARIANT, 0);
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
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        if (this.fishType == AquaticFishType.TROPICAL_FISH) {
            this.setTropicalFishVariant(this.randomTropicalFishVariant());
        }
        return livingdata;
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

        if (FluidloggedSupport.isEntityInWater(this)) {
            this.setAir(300);
        } else {
            if (!this.world.isRemote) {
                int air = this.getAir() - 1;
                this.setAir(air);
                if (air == -20) {
                    this.setAir(0);
                    this.attackEntityFrom(DamageSource.DROWN, 1.0F);
                }
            }
            updateLandFlop();
        }
        updateRotationFromMotion();
    }

    private void updateLandFlop() {
        if (this.onGround && this.flopCooldown-- <= 0) {
            this.motionX += (this.rand.nextDouble() - 0.5D) * 0.16D;
            this.motionY = 0.22D + this.rand.nextDouble() * 0.08D;
            this.motionZ += (this.rand.nextDouble() - 0.5D) * 0.16D;
            this.flopCooldown = 8 + this.rand.nextInt(8);
        }
        this.motionX *= 0.72D;
        this.motionZ *= 0.72D;
    }

    private void updateRotationFromMotion() {
        double horizontal = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontal > 1.0E-5D) {
            float yaw = -((float)MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float)Math.PI);
            this.rotationYaw += MathHelper.wrapDegrees(yaw - this.rotationYaw) * 0.2F;
            this.renderYawOffset = this.rotationYaw;
        }
        if (FluidloggedSupport.isEntityInWater(this)) {
            float targetPitch = -((float)MathHelper.atan2(this.motionY, MathHelper.sqrt(horizontal))) * (180F / (float)Math.PI);
            this.rotationPitch += (targetPitch - this.rotationPitch) * 0.15F;
        } else {
            this.rotationPitch += (0.0F - this.rotationPitch) * 0.12F;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isServerWorld() && FluidloggedSupport.isEntityInWater(this)) {
            this.moveRelative(strafe, vertical, forward, 0.1F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.8D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    /**
     * OE's mature fish MoveHelper, with Fluidlogged water detection.  This is
     * what keeps PathNavigateSwimmer from producing a stiff, ground-like fish.
     */
    private static final class FishMoveHelper extends EntityMoveHelper {

        private final EntityAquaticFish fish;

        private FishMoveHelper(EntityAquaticFish fish) {
            super(fish);
            this.fish = fish;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO && !this.fish.getNavigator().noPath()
                    && FluidloggedSupport.isEntityInWater(this.fish)) {
                if (FluidloggedSupport.isEntityInWater(this.fish)) {
                    this.fish.motionY += 0.005D;
                }

                double dx = this.posX - this.fish.posX;
                double dy = this.posY - this.fish.posY;
                double dz = this.posZ - this.fish.posZ;
                double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 1.0E-4D) {
                    dy /= distance;
                    float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                    this.fish.rotationYaw = this.limitAngle(this.fish.rotationYaw, yaw, 90.0F);
                    this.fish.renderYawOffset = this.fish.rotationYaw;

                    float speed = (float)(this.speed
                            * this.fish.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                                .getAttributeValue());
                    this.fish.setAIMoveSpeed(this.fish.getAIMoveSpeed()
                        + (speed - this.fish.getAIMoveSpeed()) * 0.125F);
                    this.fish.motionY += this.fish.getAIMoveSpeed() * dy * 0.1D;

                    net.minecraft.entity.ai.EntityLookHelper look = this.fish.getLookHelper();
                    double lookX = this.fish.posX + dx / distance * 3.0D;
                    double lookY = this.fish.posY + this.fish.getEyeHeight() + dy / distance * 5.0D;
                    double lookZ = this.fish.posZ + dz / distance * 3.0D;
                    double currentX = look.getLookPosX();
                    double currentY = look.getLookPosY();
                    double currentZ = look.getLookPosZ();
                    if (!look.getIsLooking()) {
                        currentX = lookX;
                        currentY = lookY;
                        currentZ = lookZ;
                    }
                    look.setLookPosition(currentX + (lookX - currentX) * 0.125D,
                        currentY + (lookY - currentY) * 0.125D,
                        currentZ + (lookZ - currentZ) * 0.125D, 5.0F, 30.0F);
                }
            } else if (!FluidloggedSupport.isEntityInWater(this.fish) && this.fish.isFlopping()) {
                this.fish.setAIMoveSpeed(0.0F);
            } else if (!FluidloggedSupport.isEntityInWater(this.fish)) {
                super.onUpdateMoveHelper();
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return pos.getY() < this.world.getSeaLevel()
            && FluidloggedSupport.isWater(this.world, pos)
            && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this)
            && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityAquaticFish child = this.fishType.create(this.world);
        if (this.fishType == AquaticFishType.TROPICAL_FISH) {
            child.setTropicalFishVariant(this.randomTropicalFishVariant());
        }
        return child;
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
        if (this.fishType == AquaticFishType.TROPICAL_FISH) {
            compound.setInteger("Variant", this.getTropicalFishVariant());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (this.fishType == AquaticFishType.TROPICAL_FISH && compound.hasKey("Variant")) {
            this.setTropicalFishVariant(compound.getInteger("Variant"));
        }
    }

    public boolean isFlopping() {
        return !FluidloggedSupport.isEntityInWater(this);
    }

    public int getTropicalFishVariant() {
        return this.dataManager.get(TROPICAL_VARIANT);
    }

    public void setTropicalFishVariant(int variant) {
        this.dataManager.set(TROPICAL_VARIANT, variant);
    }

    private int randomTropicalFishVariant() {
        return this.rand.nextInt(2)
            | (this.rand.nextInt(6) << 8)
            | (this.rand.nextInt(16) << 16)
            | (this.rand.nextInt(16) << 24);
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
        private static final DataParameter<Integer> PUFF_STATE =
            EntityDataManager.createKey(Pufferfish.class, DataSerializers.VARINT);
        private int puffCooldown;
        private int calmTicks;

        public Pufferfish(World worldIn) {
            super(worldIn, AquaticFishType.PUFFERFISH);
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            this.dataManager.register(PUFF_STATE, 0);
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            if (this.puffCooldown > 0) {
                this.puffCooldown--;
            }
            if (!this.world.isRemote) {
                if (this.calmTicks++ >= 80) {
                    this.setPuffState(this.getPuffState() - 1);
                    this.calmTicks = 0;
                }
                for (EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        this.getEntityBoundingBox().grow(1.25D))) {
                    if (target != this && target.isEntityAlive()
                            && !(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.isCreativeMode)) {
                        this.calmTicks = 0;
                        if (this.puffCooldown <= 0) {
                            this.setPuffState(this.getPuffState() + 1);
                            this.puffCooldown = 20;
                        }
                        if (this.getPuffState() == 2 && this.ticksExisted % 20 == 0) {
                            target.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F);
                            target.addPotionEffect(new net.minecraft.potion.PotionEffect(
                                net.minecraft.init.MobEffects.POISON, 60, 0));
                        }
                    }
                }
            }
        }

        public boolean isPuffed() {
            return this.getPuffState() > 0;
        }

        public int getPuffState() {
            return this.dataManager.get(PUFF_STATE);
        }

        private void setPuffState(int state) {
            this.dataManager.set(PUFF_STATE, Math.max(0, Math.min(2, state)));
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (!this.world.isRemote) {
                this.setPuffState(2);
                this.calmTicks = 0;
            }
            return super.attackEntityFrom(source, amount);
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            compound.setInteger("PuffState", this.getPuffState());
            compound.setInteger("PuffCooldown", this.puffCooldown);
            compound.setInteger("CalmTicks", this.calmTicks);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            this.setPuffState(compound.getInteger("PuffState"));
            this.puffCooldown = compound.getInteger("PuffCooldown");
            this.calmTicks = compound.getInteger("CalmTicks");
        }
    }

    public static class Tropical extends EntityAquaticFish {
        public Tropical(World worldIn) {
            super(worldIn, AquaticFishType.TROPICAL_FISH);
        }
    }
}
