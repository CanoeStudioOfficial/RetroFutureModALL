package com.canoestudio.retrofuturelushcave.contents.mobs.goat;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.contents.items.ItemGoatHorn;
import com.canoestudio.retrofuturelushcave.sounds.ModSoundHandler;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGoat extends EntityAnimal {
    private static final DataParameter<Boolean> SCREAMING = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_LEFT_HORN = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_RIGHT_HORN = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOWERING_HEAD = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final int LOWER_HEAD_ANIMATION_TICKS = 20;

    private int lowerHeadTick;
    private int lowerHeadTickOld;

    public EntityGoat(World worldIn) {
        super(worldIn);
        setSize(0.9F, 1.3F);
    }

    public static void registerFixesGoat(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityGoat.class);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(SCREAMING, false);
        dataManager.register(HAS_LEFT_HORN, true);
        dataManager.register(HAS_RIGHT_HORN, true);
        dataManager.register(LOWERING_HEAD, false);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        tasks.addTask(2, new GoatRamGoal(this));
        tasks.addTask(3, new GoatLongJumpGoal(this));
        tasks.addTask(4, new EntityAIMate(this, 1.0D));
        tasks.addTask(5, new EntityAITempt(this, 1.25D, Items.WHEAT, false));
        tasks.addTask(6, new EntityAIFollowParent(this, 1.25D));
        tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
    }

    @Override
    public void onLivingUpdate() {
        lowerHeadTickOld = lowerHeadTick;

        if (isLoweringHead()) {
            lowerHeadTick = Math.min(LOWER_HEAD_ANIMATION_TICKS, lowerHeadTick + 1);
        } else {
            lowerHeadTick = Math.max(0, lowerHeadTick - 2);
        }

        super.onLivingUpdate();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (itemstack.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !isChild()) {
            player.playSound(isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_MILK : ModSoundHandler.ENTITY_GOAT_MILK, 1.0F, isScreamingGoat() ? 0.65F : 1.0F);
            itemstack.shrink(1);

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
            } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
            }

            return true;
        }

        boolean result = super.processInteract(player, hand);
        if (result && itemstack.getItem() == Items.WHEAT) {
            playSound(isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_EAT : ModSoundHandler.ENTITY_GOAT_EAT, 1.0F, getSoundPitch());
        }
        return result;
    }

    @Override
    public EntityGoat createChild(EntityAgeable ageable) {
        EntityGoat child = new EntityGoat(world);
        boolean screamingChild = rand.nextDouble() < 0.02D;

        if (ageable instanceof EntityGoat) {
            EntityGoat other = (EntityGoat)ageable;
            screamingChild = isScreamingGoat() || other.isScreamingGoat() || rand.nextDouble() < 0.02D;
        }

        child.setScreamingGoat(screamingChild);
        child.setHasLeftHorn(true);
        child.setHasRightHorn(true);
        return child;
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        net.minecraft.entity.IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        setScreamingGoat(rand.nextDouble() < 0.02D);

        if (!isChild() && rand.nextFloat() < 0.1F) {
            if (rand.nextBoolean()) {
                setHasLeftHorn(false);
            } else {
                setHasRightHorn(false);
            }
        }

        return data;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.WHEAT;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        super.fall(Math.max(0.0F, distance - 10.0F), damageMultiplier);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_AMBIENT : ModSoundHandler.ENTITY_GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_HURT : ModSoundHandler.ENTITY_GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_DEATH : ModSoundHandler.ENTITY_GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
        playSound(ModSoundHandler.ENTITY_GOAT_STEP, 0.15F, 1.15F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.45F;
    }

    @Override
    protected float getSoundPitch() {
        float pitch = super.getSoundPitch() * (isScreamingGoat() ? 0.75F : 1.0F);
        return isChild() ? pitch * 1.25F : pitch;
    }

    @Override
    public float getEyeHeight() {
        return isChild() ? height * 0.85F : 1.1F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsScreamingGoat", isScreamingGoat());
        compound.setBoolean("HasLeftHorn", hasLeftHorn());
        compound.setBoolean("HasRightHorn", hasRightHorn());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setScreamingGoat(compound.getBoolean("IsScreamingGoat"));
        setHasLeftHorn(!compound.hasKey("HasLeftHorn") || compound.getBoolean("HasLeftHorn"));
        setHasRightHorn(!compound.hasKey("HasRightHorn") || compound.getBoolean("HasRightHorn"));
    }

    public boolean isScreamingGoat() {
        return dataManager.get(SCREAMING);
    }

    public void setScreamingGoat(boolean screaming) {
        dataManager.set(SCREAMING, screaming);
    }

    public boolean hasLeftHorn() {
        return dataManager.get(HAS_LEFT_HORN);
    }

    public void setHasLeftHorn(boolean hasHorn) {
        dataManager.set(HAS_LEFT_HORN, hasHorn);
    }

    public boolean hasRightHorn() {
        return dataManager.get(HAS_RIGHT_HORN);
    }

    public void setHasRightHorn(boolean hasHorn) {
        dataManager.set(HAS_RIGHT_HORN, hasHorn);
    }

    public boolean isLoweringHead() {
        return dataManager.get(LOWERING_HEAD);
    }

    public void setLoweringHead(boolean loweringHead) {
        dataManager.set(LOWERING_HEAD, loweringHead);
    }

    public float getRammingXHeadRot(float partialTicks) {
        float tick = lowerHeadTickOld + (lowerHeadTick - lowerHeadTickOld) * MathHelper.clamp(partialTicks, 0.0F, 1.0F);
        float factor = MathHelper.clamp(tick / (float)LOWER_HEAD_ANIMATION_TICKS, 0.0F, 1.0F);
        return factor * 0.5235988F;
    }

    public boolean removeRandomHorn() {
        if (isChild() || (!hasLeftHorn() && !hasRightHorn())) {
            return false;
        }

        if (!hasLeftHorn()) {
            setHasRightHorn(false);
        } else if (!hasRightHorn()) {
            setHasLeftHorn(false);
        } else if (rand.nextBoolean()) {
            setHasLeftHorn(false);
        } else {
            setHasRightHorn(false);
        }

        entityDropItem(ItemGoatHorn.createRandomHorn(world, isScreamingGoat()), 0.25F);
        return true;
    }
}
