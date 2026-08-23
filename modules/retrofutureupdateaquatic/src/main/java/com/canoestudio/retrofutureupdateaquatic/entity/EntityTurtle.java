package com.canoestudio.retrofutureupdateaquatic.entity;

import com.canoestudio.retrofutureupdateaquatic.block.BlockTurtleEgg;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAITurtleGoHome;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAITurtleMate;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAITurtleTempt;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAITurtleWanderLand;
import com.canoestudio.retrofutureupdateaquatic.entity.ai.EntityAIWanderUnderwater;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityTurtle extends EntityAnimal {

    private static final DataParameter<BlockPos> HOME_POS =
        EntityDataManager.createKey(EntityTurtle.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> HAS_EGG =
        EntityDataManager.createKey(EntityTurtle.class, DataSerializers.BOOLEAN);

    private int layEggCooldown;
    private final PathNavigateSwimmer waterNavigator;
    private final PathNavigateGround groundNavigator;

    public EntityTurtle(World worldIn) {
        super(worldIn);
        this.setSize(1.4F, 0.55F);
        this.stepHeight = 1.0F;
        this.setPathPriority(PathNodeType.WALKABLE, 1.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.waterNavigator = new PathNavigateSwimmer(this, worldIn);
        this.groundNavigator = new PathNavigateGround(this, worldIn);
        this.navigator = this.groundNavigator;
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

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAITurtleMate(this));
        this.tasks.addTask(2, new EntityAITurtleGoHome(this, 1.0D));
        this.tasks.addTask(3, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(3, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(3, new EntityAITurtleTempt(this, 1.1D,
            Item.getItemFromBlock(ModBlocks.SEAGRASS)));
        this.tasks.addTask(5, new EntityAITurtleWanderLand(this, 1.0D, 40));
        this.tasks.addTask(5, new EntityAIWanderUnderwater(this, 1.0D, 80, true));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
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

    public void setLayEggCooldown(int ticks) {
        this.layEggCooldown = Math.max(0, ticks);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.SEAGRASS);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        return super.processInteract(player, hand);
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote) {
            this.navigator = FluidloggedSupport.isEntityInWater(this)
                ? this.waterNavigator : this.groundNavigator;
        }
        super.onLivingUpdate();
        if (!this.world.isRemote && this.hasEgg()) {
            updateEggLaying();
        }
        updateRotation();
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

    public double getDistanceSqToHome() {
        BlockPos home = this.getHomePos();
        double dx = home.getX() + 0.5D - this.posX;
        double dy = home.getY() + 0.5D - this.posY;
        double dz = home.getZ() + 0.5D - this.posZ;
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
        if (FluidloggedSupport.isEntityInWater(this)) {
            this.moveRelative(strafe, vertical, forward, 0.1F);
            this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.8D;
        } else {
            super.travel(strafe, vertical, forward);
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
    protected Item getDropItem() {
        return Item.getItemFromBlock(ModBlocks.SEAGRASS);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // OE's mature 1.12.2 turtle loot table uses a 0-2 seagrass roll.
        int count = this.rand.nextInt(3) + lootingModifier;
        if (count > 0) {
            this.dropItem(Item.getItemFromBlock(ModBlocks.SEAGRASS), count);
        }
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
        return FluidloggedSupport.isEntityInWater(this) ? net.minecraft.init.SoundEvents.ENTITY_SQUID_AMBIENT
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
