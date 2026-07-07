package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.block.BlockTurtleEgg;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

public class EntityTurtle extends EntityAnimal {

    private static final DataParameter<BlockPos> HOME_POS =
        EntityDataManager.createKey(EntityTurtle.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> HAS_EGG =
        EntityDataManager.createKey(EntityTurtle.class, DataSerializers.BOOLEAN);

    @Nullable
    private BlockPos swimTarget;
    private int targetCooldown;
    private int layEggCooldown;

    public EntityTurtle(World worldIn) {
        super(worldIn);
        this.setSize(1.2F, 0.4F);
        this.stepHeight = 1.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HOME_POS, BlockPos.ORIGIN);
        this.dataManager.register(HAS_EGG, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.16D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setHomePos(new BlockPos(this));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public BlockPos getHomePos() {
        return this.dataManager.get(HOME_POS);
    }

    public void setHomePos(BlockPos pos) {
        this.dataManager.set(HOME_POS, pos);
    }

    public boolean hasEgg() {
        return this.dataManager.get(HAS_EGG);
    }

    public void setHasEgg(boolean hasEgg) {
        this.dataManager.set(HAS_EGG, hasEgg);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.SEAGRASS);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        boolean interacted = super.processInteract(player, hand);
        if (!this.world.isRemote && interacted && !this.isChild() && !this.hasEgg() && this.rand.nextInt(2) == 0) {
            this.setHasEgg(true);
            this.layEggCooldown = 100;
        }
        return interacted;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isInWater()) {
            updateWaterMovement();
        } else {
            updateLandMovement();
        }
        if (!this.world.isRemote && this.hasEgg()) {
            updateEggLaying();
        }
        updateRotation();
    }

    private void updateWaterMovement() {
        if (this.targetCooldown > 0) {
            this.targetCooldown--;
        }
        BlockPos target = this.hasEgg() ? this.getHomePos() : this.swimTarget;
        if (!this.hasEgg() && (target == null || this.targetCooldown <= 0 || !isWater(target)
                || distanceSqToCenter(target) < 1.4D)) {
            this.swimTarget = findWaterTarget();
            this.targetCooldown = 40 + this.rand.nextInt(80);
            target = this.swimTarget;
        }
        if (target != null) {
            moveToward(target.getX() + 0.5D, target.getY() + 0.25D, target.getZ() + 0.5D,
                this.hasEgg() ? 0.065D : 0.045D, 0.16D);
        }
        this.motionX *= 0.9D;
        this.motionY *= 0.9D;
        this.motionZ *= 0.9D;
    }

    private void updateLandMovement() {
        if (this.hasEgg()) {
            BlockPos home = this.getHomePos();
            moveToward(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 0.025D, 0.08D);
        } else if (this.onGround && this.rand.nextInt(80) == 0) {
            this.motionX += (this.rand.nextDouble() - 0.5D) * 0.06D;
            this.motionZ += (this.rand.nextDouble() - 0.5D) * 0.06D;
        }
        this.motionX *= 0.65D;
        this.motionZ *= 0.65D;
    }

    private void updateEggLaying() {
        if (this.layEggCooldown > 0) {
            this.layEggCooldown--;
            return;
        }
        BlockPos below = new BlockPos(this).down();
        if (this.world.getBlockState(below).getBlock() == Blocks.SAND
                && this.world.isAirBlock(below.up())) {
            this.world.setBlockState(below.up(), ModBlocks.TURTLE_EGG.getDefaultState()
                .withProperty(BlockTurtleEgg.EGGS, 1 + this.rand.nextInt(4)), 3);
            this.setHasEgg(false);
            this.layEggCooldown = 600;
        }
    }

    @Nullable
    private BlockPos findWaterTarget() {
        BlockPos origin = new BlockPos(this);
        for (int i = 0; i < 18; i++) {
            BlockPos candidate = origin.add(this.rand.nextInt(15) - 7, this.rand.nextInt(5) - 2,
                this.rand.nextInt(15) - 7);
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

    private void updateRotation() {
        double horizontal = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (horizontal > 1.0E-5D) {
            float yaw = -((float)MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float)Math.PI);
            this.rotationYaw += MathHelper.wrapDegrees(yaw - this.rotationYaw) * 0.14F;
            this.renderYawOffset = this.rotationYaw;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.isInWater()) {
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        }
    }

    @Override
    public EntityTurtle createChild(EntityAgeable ageable) {
        EntityTurtle turtle = new EntityTurtle(this.world);
        turtle.setHomePos(this.getHomePos());
        return turtle;
    }

    @Override
    protected void onGrowingAdult() {
        this.entityDropItem(new ItemStack(ModItems.SCUTE), 0.0F);
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return this.posY > 58.0D && this.posY < 72.0D
            && this.world.getBlockState(pos.down()).getBlock() == Blocks.SAND
            && this.world.getLight(pos) > 7
            && super.getCanSpawnHere();
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? net.minecraft.init.SoundEvents.ENTITY_SQUID_AMBIENT
            : net.minecraft.init.SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return net.minecraft.init.SoundEvents.ENTITY_CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.init.SoundEvents.ENTITY_CHICKEN_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        BlockPos home = this.getHomePos();
        compound.setInteger("HomeX", home.getX());
        compound.setInteger("HomeY", home.getY());
        compound.setInteger("HomeZ", home.getZ());
        compound.setBoolean("HasEgg", this.hasEgg());
        compound.setInteger("LayEggCooldown", this.layEggCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHomePos(new BlockPos(compound.getInteger("HomeX"), compound.getInteger("HomeY"),
            compound.getInteger("HomeZ")));
        this.setHasEgg(compound.getBoolean("HasEgg"));
        this.layEggCooldown = compound.getInteger("LayEggCooldown");
    }
}
