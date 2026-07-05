package com.canoestudio.retrofuturemc.contents.mobs.goat;

import com.canoestudio.retrofuturemc.sounds.ModSoundHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class GoatRamGoal extends EntityAIBase {
    private static final int PREPARE_TIME = 20;
    private static final int RAM_TIME_LIMIT = 40;
    private static final double MIN_DISTANCE = 4.0D;
    private static final double MAX_DISTANCE = 7.0D;
    private static final double PREPARE_SPEED = 1.25D;
    private static final double RAM_SPEED = 0.58D;

    private final EntityGoat goat;
    private EntityLivingBase target;
    private int prepareTicks;
    private int ramTicks;
    private int cooldownTicks;
    private double ramX;
    private double ramZ;
    private Phase phase = Phase.COOLDOWN;

    public GoatRamGoal(EntityGoat goat) {
        this.goat = goat;
        setMutexBits(3);
        cooldownTicks = nextCooldown();
    }

    @Override
    public boolean shouldExecute() {
        if (goat.world.isRemote) {
            return false;
        }
        if (goat.isChild() && goat.getRNG().nextInt(3) != 0) {
            return false;
        }
        if (goat.isInLove() || goat.isInWater() || goat.isBeingRidden()) {
            return false;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        target = findTarget();
        return target != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (target == null || !target.isEntityAlive() || goat.isInLove()) {
            return false;
        }
        return phase == Phase.PREPARE || phase == Phase.RAM;
    }

    @Override
    public void startExecuting() {
        phase = Phase.PREPARE;
        prepareTicks = PREPARE_TIME;
        ramTicks = 0;
        goat.setLoweringHead(true);
        goat.getNavigator().clearPath();
        goat.playSound(goat.isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_PREPARE_RAM : ModSoundHandler.ENTITY_GOAT_PREPARE_RAM, 1.0F, goat.isScreamingGoat() ? 0.55F : 0.8F);
    }

    @Override
    public void resetTask() {
        goat.setLoweringHead(false);
        goat.getNavigator().clearPath();
        goat.motionX *= 0.5D;
        goat.motionZ *= 0.5D;
        target = null;
        phase = Phase.COOLDOWN;
        cooldownTicks = nextCooldown();
    }

    @Override
    public void updateTask() {
        if (target == null) {
            return;
        }

        if (phase == Phase.PREPARE) {
            updatePrepare();
        } else if (phase == Phase.RAM) {
            updateRam();
        }
    }

    private void updatePrepare() {
        goat.getNavigator().clearPath();
        goat.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

        double dx = target.posX - goat.posX;
        double dz = target.posZ - goat.posZ;
        float yaw = -((float)MathHelper.atan2(dx, dz)) * (180F / (float)Math.PI);
        goat.rotationYaw = goat.renderYawOffset = yaw;

        double distanceSq = dx * dx + dz * dz;
        if (distanceSq > MAX_DISTANCE * MAX_DISTANCE + 4.0D) {
            goat.getNavigator().tryMoveToEntityLiving(target, PREPARE_SPEED);
        }

        if (--prepareTicks <= 0) {
            double distance = MathHelper.sqrt(dx * dx + dz * dz);
            if (distance < 0.0001D) {
                resetTask();
                return;
            }

            ramX = dx / distance;
            ramZ = dz / distance;
            goat.motionX = ramX * RAM_SPEED;
            goat.motionY = goat.onGround ? 0.05D : goat.motionY;
            goat.motionZ = ramZ * RAM_SPEED;
            goat.velocityChanged = true;
            phase = Phase.RAM;
        }
    }

    private void updateRam() {
        ramTicks++;
        goat.getNavigator().clearPath();
        goat.rotationYaw = goat.renderYawOffset = -((float)MathHelper.atan2(ramX, ramZ)) * (180F / (float)Math.PI);
        goat.motionX = ramX * RAM_SPEED;
        goat.motionZ = ramZ * RAM_SPEED;
        goat.velocityChanged = true;

        EntityLivingBase hit = findHitEntity();
        if (hit != null) {
            hit.attackEntityFrom(DamageSource.causeMobDamage(goat), goat.isChild() ? 1.0F : 2.0F);
            hit.knockBack(goat, goat.isChild() ? 1.0F : 2.5F, goat.posX - hit.posX, goat.posZ - hit.posZ);
            goat.playSound(goat.isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_RAM_IMPACT : ModSoundHandler.ENTITY_GOAT_RAM_IMPACT, 0.75F, goat.isScreamingGoat() ? 0.65F : 1.15F);
            resetTask();
            return;
        }

        if (ramTicks > 4 && hasHitWall()) {
            if (goat.removeRandomHorn()) {
                goat.playSound(ModSoundHandler.ENTITY_GOAT_HORN_BREAK, 1.0F, 0.9F);
            } else {
                goat.playSound(goat.isScreamingGoat() ? ModSoundHandler.ENTITY_GOAT_SCREAMING_RAM_IMPACT : ModSoundHandler.ENTITY_GOAT_RAM_IMPACT, 0.8F, 1.0F);
            }
            resetTask();
            return;
        }

        if (ramTicks >= RAM_TIME_LIMIT || target.getDistanceSq(goat) > 100.0D) {
            resetTask();
        }
    }

    @Nullable
    private EntityLivingBase findTarget() {
        AxisAlignedBB area = goat.getEntityBoundingBox().grow(MAX_DISTANCE, 3.0D, MAX_DISTANCE);
        List<EntityLivingBase> candidates = goat.world.getEntitiesWithinAABB(EntityLivingBase.class, area, entity -> isValidTarget(entity));

        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator.comparingDouble(entity -> entity.getDistanceSq(goat)));
        for (EntityLivingBase candidate : candidates) {
            double distanceSq = candidate.getDistanceSq(goat);
            if (distanceSq >= MIN_DISTANCE * MIN_DISTANCE && distanceSq <= MAX_DISTANCE * MAX_DISTANCE && hasClearLine(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == goat || entity instanceof EntityGoat || !entity.isEntityAlive() || entity.isInvisible()) {
            return false;
        }
        if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.disableDamage) {
            return false;
        }
        return goat.canEntityBeSeen(entity);
    }

    private boolean hasClearLine(EntityLivingBase entity) {
        double dx = entity.posX - goat.posX;
        double dz = entity.posZ - goat.posZ;
        double distance = MathHelper.sqrt(dx * dx + dz * dz);
        if (distance < 0.0001D) {
            return false;
        }

        double stepX = dx / distance;
        double stepZ = dz / distance;
        for (double walked = 1.0D; walked < distance; walked += 1.0D) {
            BlockPos pos = new BlockPos(goat.posX + stepX * walked, goat.posY + 0.2D, goat.posZ + stepZ * walked);
            if (isBlocking(pos)) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    private EntityLivingBase findHitEntity() {
        AxisAlignedBB impactBox = goat.getEntityBoundingBox().grow(0.35D, 0.15D, 0.35D);
        List<EntityLivingBase> hits = goat.world.getEntitiesWithinAABB(EntityLivingBase.class, impactBox, entity -> isValidTarget(entity));
        return hits.isEmpty() ? null : hits.get(0);
    }

    private boolean hasHitWall() {
        Vec3d front = new Vec3d(goat.posX + ramX * 0.65D, goat.posY + 0.45D, goat.posZ + ramZ * 0.65D);
        return isBlocking(new BlockPos(front));
    }

    private boolean isBlocking(BlockPos pos) {
        if (!goat.world.isBlockLoaded(pos)) {
            return false;
        }

        Material material = goat.world.getBlockState(pos).getMaterial();
        return material.blocksMovement() && material != Material.LEAVES && material != Material.PLANTS && material != Material.VINE;
    }

    private int nextCooldown() {
        if (goat.isScreamingGoat()) {
            return 100 + goat.getRNG().nextInt(201);
        }
        return 600 + goat.getRNG().nextInt(5401);
    }

    private enum Phase {
        COOLDOWN,
        PREPARE,
        RAM
    }
}
