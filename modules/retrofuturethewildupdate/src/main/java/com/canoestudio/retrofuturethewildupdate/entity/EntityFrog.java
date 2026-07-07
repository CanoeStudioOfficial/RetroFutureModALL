package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturelushcavecore.api.entity.RetroEntityAttributes;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class EntityFrog extends EntityAnimal {

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TONGUE_TICKS = EntityDataManager.createKey(EntityFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CROAKING = EntityDataManager.createKey(EntityFrog.class, DataSerializers.BOOLEAN);
    private static final String[] VARIANT_NAMES = {"temperate", "warm", "cold"};

    private int hopCooldown;
    private int croakTicks;

    public EntityFrog(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.hopCooldown = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(TONGUE_TICKS, 0);
        this.dataManager.register(CROAKING, false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.35D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.SLIME_BALL, false));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.85D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntitySlime>(this, EntitySlime.class, 12, true, false,
            slime -> slime != null && slime.isEntityAlive() && slime.getSlimeSize() <= 1));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MAX_HEALTH, 10.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MOVEMENT_SPEED, 0.95D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.ATTACK_DAMAGE, 2.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setVariant(variantForBiome(this.world.getBiome(new BlockPos(this))));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        int tongueTicks = this.getTongueTicks();
        if (tongueTicks > 0) {
            this.dataManager.set(TONGUE_TICKS, tongueTicks - 1);
        }

        if (this.croakTicks > 0) {
            --this.croakTicks;
            if (this.croakTicks == 0) {
                this.dataManager.set(CROAKING, false);
            }
        } else if (!this.world.isRemote && this.rand.nextInt(this.isInWater() ? 140 : 260) == 0) {
            this.croakTicks = 28 + this.rand.nextInt(18);
            this.dataManager.set(CROAKING, true);
            this.playSound(SoundEvents.ENTITY_SLIME_SQUISH, 0.35F, 0.7F + this.rand.nextFloat() * 0.25F);
        }

        if (!this.world.isRemote) {
            this.handleHopMovement();
        }
    }

    private void handleHopMovement() {
        if (this.hopCooldown > 0) {
            --this.hopCooldown;
        }

        boolean moving = Math.abs(this.motionX) + Math.abs(this.motionZ) > 0.02D || this.getAttackTarget() != null;
        if (this.onGround && this.hopCooldown <= 0 && moving) {
            this.motionY = this.isInWater() ? 0.16D : 0.34D;
            this.motionX *= 1.45D;
            this.motionZ *= 1.45D;
            this.hopCooldown = 10 + this.rand.nextInt(12);
            this.playSound(SoundEvents.ENTITY_SLIME_JUMP, 0.25F, 1.35F);
        } else if (this.isInWater() && this.hopCooldown <= 0 && moving) {
            this.motionY += (this.rand.nextDouble() - 0.35D) * 0.035D;
            this.hopCooldown = 8 + this.rand.nextInt(12);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn instanceof EntitySlime && ((EntitySlime) entityIn).getSlimeSize() <= 1) {
            this.dataManager.set(TONGUE_TICKS, 12);
            this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 0.7F, 1.2F);

            if (!this.world.isRemote) {
                if (entityIn instanceof EntityMagmaCube) {
                    this.entityDropItem(new ItemStack(this.getFroglightBlock()), 0.15F);
                } else {
                    this.entityDropItem(new ItemStack(Items.SLIME_BALL), 0.15F);
                }
                entityIn.setDead();
            }
            return true;
        }
        return super.attackEntityAsMob(entityIn);
    }

    private net.minecraft.block.Block getFroglightBlock() {
        if (this.getVariant() == 1) {
            return ModBlocks.PEARLESCENT_FROGLIGHT;
        }
        if (this.getVariant() == 2) {
            return ModBlocks.VERDANT_FROGLIGHT;
        }
        return ModBlocks.OCHRE_FROGLIGHT;
    }

    @Override
    public EntityFrog createChild(EntityAgeable ageable) {
        EntityFrog child = new EntityFrog(this.world);
        if (ageable instanceof EntityFrog && this.rand.nextBoolean()) {
            child.setVariant(((EntityFrog) ageable).getVariant());
        } else {
            child.setVariant(this.getVariant());
        }
        return child;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.SLIME_BALL;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return pos.getY() > 55
            && (this.world.getBlockState(pos.down()).getMaterial() == Material.GRASS
                || this.world.getBlockState(pos.down()).getMaterial() == Material.GROUND
                || this.world.getBlockState(pos.down()).getMaterial() == Material.WATER)
            && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SLIME_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        return new ItemStack(ModItems.FROG_SPAWN_EGG);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Variant")) {
            this.setVariant(compound.getInteger("Variant"));
        }
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, MathHelper.clamp(variant, 0, VARIANT_NAMES.length - 1));
    }

    public String getVariantName() {
        return VARIANT_NAMES[this.getVariant()];
    }

    public int getTongueTicks() {
        return this.dataManager.get(TONGUE_TICKS);
    }

    public boolean isCroaking() {
        return this.dataManager.get(CROAKING);
    }

    public static int variantForBiome(Biome biome) {
        float temperature = biome.getDefaultTemperature();
        if (temperature < 0.55F) {
            return 2;
        }
        if (temperature >= 0.95F) {
            return 1;
        }
        return 0;
    }
}
