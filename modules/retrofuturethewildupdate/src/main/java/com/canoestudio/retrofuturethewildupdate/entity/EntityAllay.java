package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturemccore.api.entity.RetroEntityAttributes;
import com.canoestudio.retrofuturethewildupdate.sounds.ModSounds;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAllay extends EntityLiving implements EntityFlying {

    private static final DataParameter<ItemStack> HELD_ITEM =
        EntityDataManager.createKey(EntityAllay.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Boolean> DANCING =
        EntityDataManager.createKey(EntityAllay.class, DataSerializers.BOOLEAN);

    private UUID likedPlayer;
    private Vec3d wanderTarget;
    private int pickupCooldown;
    private BlockPos likedNoteBlock;
    private int likedNoteBlockCooldown;
    private BlockPos jukeboxPos;
    private int dancingTicks;
    private int duplicationCooldown;

    public EntityAllay(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.8F);
        this.isImmuneToFire = false;
        this.moveHelper = new EntityAllay.FlightMoveHelper(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HELD_ITEM, ItemStack.EMPTY);
        this.dataManager.register(DANCING, false);
    }

    @Override
    protected void initEntityAI() {
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MAX_HEALTH, 20.0D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MOVEMENT_SPEED, 0.35D);
        RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.motionY *= 0.6D;

        if (this.pickupCooldown > 0) {
            --this.pickupCooldown;
        }

        if (!this.world.isRemote) {
            if (this.ticksExisted % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
            this.tickMemoryCooldowns();
            EntityPlayer liked = this.getLikedPlayer();
            if (!this.getHeldItemStack().isEmpty() && this.tryDeliverToNoteBlock()) {
                this.updateFlightTarget(liked);
            } else if (liked != null && !this.getHeldItemStack().isEmpty()) {
                this.tryDeliverTo(liked);
            }
            if (this.pickupCooldown <= 0 && !this.getHeldItemStack().isEmpty()) {
                this.tryPickupMatchingItem();
            }
            this.updateFlightTarget(liked);
        }

        this.faceMovement();
    }

    private void updateFlightTarget(@Nullable EntityPlayer liked) {
        Vec3d target = null;
        if (this.hasActiveNoteBlock()) {
            target = new Vec3d(this.likedNoteBlock.getX() + 0.5D, this.likedNoteBlock.getY() + 1.2D,
                this.likedNoteBlock.getZ() + 0.5D);
        } else if (liked != null) {
            double distanceSq = this.getDistanceSq(liked);
            if (distanceSq > 9.0D) {
                target = new Vec3d(liked.posX, liked.posY + 1.2D, liked.posZ);
            }
        }

        if (target == null) {
            if (this.wanderTarget == null || this.ticksExisted % 60 == 0
                || this.getDistanceSq(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z) < 2.0D) {
                this.wanderTarget = new Vec3d(
                    this.posX + this.rand.nextInt(13) - 6,
                    MathHelper.clamp(this.posY + this.rand.nextInt(7) - 3, 2.0D, 250.0D),
                    this.posZ + this.rand.nextInt(13) - 6);
            }
            target = this.wanderTarget;
        }

        this.getMoveHelper().setMoveTo(target.x, target.y, target.z, liked == null && !this.hasActiveNoteBlock() ? 0.45D : 0.75D);
    }

    private void tryDeliverTo(EntityPlayer player) {
        if (this.getDistanceSq(player) > 4.0D || this.getHeldItemStack().getCount() <= 1) {
            return;
        }
        ItemStack held = this.getHeldItemStack();
        ItemStack delivered = held.splitStack(held.getCount() - 1);
        if (!player.inventory.addItemStackToInventory(delivered)) {
            this.entityDropItem(delivered, 0.2F);
        } else {
            player.inventoryContainer.detectAndSendChanges();
            this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.4F, 1.4F + this.rand.nextFloat() * 0.2F);
        }
        this.setHeldItemStack(held);
        this.pickupCooldown = 40;
    }

    private boolean tryDeliverToNoteBlock() {
        if (!this.hasActiveNoteBlock() || this.getHeldItemStack().getCount() <= 1) {
            return false;
        }
        double distanceSq = this.getDistanceSq(this.likedNoteBlock.getX() + 0.5D, this.likedNoteBlock.getY() + 0.5D,
            this.likedNoteBlock.getZ() + 0.5D);
        if (distanceSq > 4.0D) {
            return false;
        }
        ItemStack held = this.getHeldItemStack();
        ItemStack delivered = held.splitStack(held.getCount() - 1);
        EntityItem item = new EntityItem(this.world, this.likedNoteBlock.getX() + 0.5D, this.likedNoteBlock.getY() + 1.1D,
            this.likedNoteBlock.getZ() + 0.5D, delivered);
        item.motionX = (this.rand.nextDouble() - 0.5D) * 0.08D;
        item.motionY = 0.18D;
        item.motionZ = (this.rand.nextDouble() - 0.5D) * 0.08D;
        item.setDefaultPickupDelay();
        this.world.spawnEntity(item);
        this.setHeldItemStack(held);
        this.pickupCooldown = 40;
        this.playSound(ModSounds.ALLAY_THROW, 0.8F, THROW_SOUND_PITCHES[this.rand.nextInt(THROW_SOUND_PITCHES.length)]);
        return true;
    }

    private void tryPickupMatchingItem() {
        if (!this.world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }
        ItemStack held = this.getHeldItemStack();
        if (held.getCount() >= held.getMaxStackSize()) {
            return;
        }
        AxisAlignedBB area = this.getEntityBoundingBox().grow(8.0D, 5.0D, 8.0D);
        List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, area,
            item -> item != null && item.isEntityAlive() && matchesHeldItem(held, item.getItem()));
        if (items.isEmpty()) {
            return;
        }
        EntityItem item = items.get(0);
        ItemStack ground = item.getItem();
        int move = Math.min(ground.getCount(), held.getMaxStackSize() - held.getCount());
        if (move <= 0) {
            return;
        }
        held.grow(move);
        ground.shrink(move);
        if (ground.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(ground);
        }
        this.setHeldItemStack(held);
        this.pickupCooldown = 20;
        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.35F, 1.6F);
    }

    private static boolean matchesHeldItem(ItemStack held, ItemStack other) {
        return !held.isEmpty()
            && !other.isEmpty()
            && held.getItem() == other.getItem()
            && held.getMetadata() == other.getMetadata()
            && ItemStack.areItemStackTagsEqual(held, other);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack held = this.getHeldItemStack();

        if (!stack.isEmpty() && this.isDancing() && this.canDuplicate()
            && stack.getItem() == com.canoestudio.retrofuturemc.contents.items.ModItems.AMETHYST_SHARD) {
            if (!this.world.isRemote) {
                this.duplicateAllay();
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            this.playSound(SoundEvents.BLOCK_NOTE_CHIME, 1.2F, 1.0F);
            return true;
        }

        if (held.isEmpty() && !stack.isEmpty()) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            this.setHeldItemStack(copy);
            this.likedPlayer = player.getUniqueID();
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            this.playSound(ModSounds.ALLAY_ITEM_GIVEN, 0.45F, 1.5F);
            return true;
        }

        if (!held.isEmpty() && stack.isEmpty()) {
            player.setHeldItem(hand, held.copy());
            this.setHeldItemStack(ItemStack.EMPTY);
            this.likedPlayer = null;
            this.playSound(ModSounds.ALLAY_ITEM_TAKEN, 0.35F, 0.8F);
            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isLikedPlayer(source.getTrueSource())) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    private void faceMovement() {
        double dx = this.motionX;
        double dz = this.motionZ;
        if (dx * dx + dz * dz > 0.0025D) {
            this.rotationYaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.rotationYaw;
        }
    }

    @Nullable
    private EntityPlayer getLikedPlayer() {
        return this.likedPlayer == null ? null : this.world.getPlayerEntityByUUID(this.likedPlayer);
    }

    private boolean isLikedPlayer(@Nullable Entity entity) {
        return entity instanceof EntityPlayer && this.likedPlayer != null
            && this.likedPlayer.equals(((EntityPlayer) entity).getUniqueID());
    }

    public ItemStack getHeldItemStack() {
        return this.dataManager.get(HELD_ITEM);
    }

    @Override
    public ItemStack getHeldItemMainhand() {
        return this.getHeldItemStack();
    }

    public void setHeldItemStack(ItemStack stack) {
        this.dataManager.set(HELD_ITEM, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public boolean isDancing() {
        return this.dataManager.get(DANCING);
    }

    public void hearNoteBlock(BlockPos pos) {
        if (this.world.isRemote || this.isDead || this.getDistanceSq(pos) > 1024.0D) {
            return;
        }
        this.likedNoteBlock = pos.toImmutable();
        this.likedNoteBlockCooldown = 600;
    }

    public void setJukeboxPlaying(BlockPos pos, boolean playing) {
        if (this.world.isRemote || this.getDistanceSq(pos) > 1024.0D) {
            return;
        }
        if (playing) {
            this.jukeboxPos = pos.toImmutable();
            this.dancingTicks = 3600;
            this.dataManager.set(DANCING, true);
        } else if (this.jukeboxPos == null || this.jukeboxPos.equals(pos)) {
            this.jukeboxPos = null;
            this.dancingTicks = 0;
            this.dataManager.set(DANCING, false);
        }
    }

    private void tickMemoryCooldowns() {
        if (this.likedNoteBlockCooldown > 0) {
            --this.likedNoteBlockCooldown;
        } else {
            this.likedNoteBlock = null;
        }
        if (this.duplicationCooldown > 0) {
            --this.duplicationCooldown;
        }
        if (this.dancingTicks > 0) {
            --this.dancingTicks;
            if (this.dancingTicks == 0 || !this.isJukeboxStillPlaying()) {
                this.jukeboxPos = null;
                this.dataManager.set(DANCING, false);
            }
        }
    }

    private boolean hasActiveNoteBlock() {
        return this.likedNoteBlock != null && this.likedNoteBlockCooldown > 0
            && this.world.getBlockState(this.likedNoteBlock).getBlock() == net.minecraft.init.Blocks.NOTEBLOCK;
    }

    private boolean canDuplicate() {
        return this.duplicationCooldown <= 0;
    }

    private boolean isJukeboxStillPlaying() {
        return this.jukeboxPos != null
            && this.world.getBlockState(this.jukeboxPos).getBlock() == net.minecraft.init.Blocks.JUKEBOX
            && this.world.getBlockState(this.jukeboxPos).getValue(net.minecraft.block.BlockJukebox.HAS_RECORD);
    }

    private void duplicateAllay() {
        EntityAllay allay = new EntityAllay(this.world);
        allay.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
        allay.likedPlayer = this.likedPlayer;
        allay.duplicationCooldown = 6000;
        this.duplicationCooldown = 6000;
        this.world.spawnEntity(allay);
        this.world.setEntityState(this, (byte) 18);
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return true;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getHeldItemStack().isEmpty() ? ModSounds.ALLAY_AMBIENT_WITHOUT_ITEM : ModSounds.ALLAY_AMBIENT_WITH_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALLAY_DEATH;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        return new ItemStack(com.canoestudio.retrofuturethewildupdate.item.ModItems.ALLAY_SPAWN_EGG);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (!this.getHeldItemStack().isEmpty()) {
            compound.setTag("HeldItem", this.getHeldItemStack().writeToNBT(new NBTTagCompound()));
        }
        if (this.likedPlayer != null) {
            compound.setString("LikedPlayer", this.likedPlayer.toString());
        }
        if (this.likedNoteBlock != null) {
            compound.setLong("LikedNoteBlock", this.likedNoteBlock.toLong());
            compound.setInteger("LikedNoteBlockCooldown", this.likedNoteBlockCooldown);
        }
        if (this.jukeboxPos != null) {
            compound.setLong("JukeboxPos", this.jukeboxPos.toLong());
            compound.setInteger("DancingTicks", this.dancingTicks);
        }
        compound.setInteger("DuplicationCooldown", this.duplicationCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("HeldItem", 10)) {
            this.setHeldItemStack(new ItemStack(compound.getCompoundTag("HeldItem")));
        }
        if (compound.hasKey("LikedPlayer", 8)) {
            this.likedPlayer = UUID.fromString(compound.getString("LikedPlayer"));
        }
        if (compound.hasKey("LikedNoteBlock", 99)) {
            this.likedNoteBlock = BlockPos.fromLong(compound.getLong("LikedNoteBlock"));
            this.likedNoteBlockCooldown = compound.getInteger("LikedNoteBlockCooldown");
        }
        if (compound.hasKey("JukeboxPos", 99)) {
            this.jukeboxPos = BlockPos.fromLong(compound.getLong("JukeboxPos"));
            this.dancingTicks = compound.getInteger("DancingTicks");
            this.dataManager.set(DANCING, this.dancingTicks > 0);
        }
        this.duplicationCooldown = compound.getInteger("DuplicationCooldown");
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 18) {
            for (int i = 0; i < 3; i++) {
                this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.HEART,
                    this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
                    this.posY + 0.6D + this.rand.nextDouble() * 0.4D,
                    this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
                    0.0D, 0.02D, 0.0D);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private static final float[] THROW_SOUND_PITCHES = new float[]{
        0.5625F, 0.625F, 0.75F, 0.9375F, 1.0F, 1.125F, 1.25F, 1.5F, 1.875F, 2.0F
    };

    private static final class FlightMoveHelper extends net.minecraft.entity.ai.EntityMoveHelper {
        private final EntityAllay allay;

        private FlightMoveHelper(EntityAllay allay) {
            super(allay);
            this.allay = allay;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action != Action.MOVE_TO) {
                return;
            }
            double dx = this.posX - this.allay.posX;
            double dy = this.posY - this.allay.posY;
            double dz = this.posZ - this.allay.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.5D) {
                this.action = Action.WAIT;
                this.allay.motionX *= 0.5D;
                this.allay.motionY *= 0.5D;
                this.allay.motionZ *= 0.5D;
                return;
            }
            double speed = this.speed * 0.08D;
            this.allay.motionX += dx / dist * speed;
            this.allay.motionY += dy / dist * speed;
            this.allay.motionZ += dz / dist * speed;
        }
    }
}
