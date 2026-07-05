package com.canoestudio.retrofuturemc.contents.mobs.goat;

import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class GoatLongJumpGoal extends EntityAIBase {
    private static final int PREPARE_TICKS = 10;
    private static final int JUMP_TIME_LIMIT = 35;
    private static final double HORIZONTAL_SPEED = 0.46D;

    private final EntityGoat goat;
    private Vec3d target;
    private int cooldownTicks;
    private int prepareTicks;
    private int jumpTicks;
    private Phase phase = Phase.COOLDOWN;

    public GoatLongJumpGoal(EntityGoat goat) {
        this.goat = goat;
        setMutexBits(3);
        cooldownTicks = nextCooldown();
    }

    @Override
    public boolean shouldExecute() {
        if (goat.world.isRemote || goat.isInLove() || goat.isInWater() || !goat.onGround || goat.isBeingRidden()) {
            return false;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        target = findJumpTarget();
        return target != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (target == null || goat.isInWater() || goat.isInLove()) {
            return false;
        }
        return phase == Phase.PREPARE || phase == Phase.JUMP && jumpTicks < JUMP_TIME_LIMIT && (!goat.onGround || jumpTicks < 5);
    }

    @Override
    public void startExecuting() {
        phase = Phase.PREPARE;
        prepareTicks = PREPARE_TICKS;
        jumpTicks = 0;
        goat.getNavigator().clearPath();
        goat.setLoweringHead(true);
    }

    @Override
    public void resetTask() {
        goat.setLoweringHead(false);
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
            goat.getNavigator().clearPath();
            goat.getLookHelper().setLookPosition(target.x, target.y, target.z, 30.0F, 30.0F);
            if (--prepareTicks <= 0) {
                launch();
            }
        } else if (phase == Phase.JUMP) {
            jumpTicks++;
        }
    }

    @Nullable
    private Vec3d findJumpTarget() {
        BlockPos origin = new BlockPos(goat);

        for (int attempt = 0; attempt < 16; attempt++) {
            float angle = goat.getRNG().nextFloat() * ((float)Math.PI * 2.0F);
            int distance = 2 + goat.getRNG().nextInt(4);
            int dx = MathHelper.floor(MathHelper.cos(angle) * distance);
            int dz = MathHelper.floor(MathHelper.sin(angle) * distance);

            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }

            for (int dy = 2; dy >= -3; dy--) {
                BlockPos landing = origin.add(dx, dy, dz);
                if (isGoodLanding(landing) && hasClearArc(landing)) {
                    return new Vec3d(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
                }
            }
        }

        return null;
    }

    private boolean isGoodLanding(BlockPos pos) {
        if (!goat.world.isBlockLoaded(pos)) {
            return false;
        }

        Material feet = goat.world.getBlockState(pos).getMaterial();
        Material head = goat.world.getBlockState(pos.up()).getMaterial();
        Material ground = goat.world.getBlockState(pos.down()).getMaterial();
        return !feet.blocksMovement() && !head.blocksMovement() && ground.blocksMovement() && ground != Material.WATER && ground != Material.LAVA;
    }

    private boolean hasClearArc(BlockPos landing) {
        double startX = goat.posX;
        double startY = goat.posY + 0.6D;
        double startZ = goat.posZ;
        double endX = landing.getX() + 0.5D;
        double endY = landing.getY() + 0.8D;
        double endZ = landing.getZ() + 0.5D;

        for (int step = 1; step <= 5; step++) {
            double factor = step / 6.0D;
            double arc = Math.sin(factor * Math.PI) * 1.0D;
            BlockPos check = new BlockPos(startX + (endX - startX) * factor, startY + (endY - startY) * factor + arc, startZ + (endZ - startZ) * factor);

            if (goat.world.getBlockState(check).getMaterial().blocksMovement()) {
                return false;
            }
        }

        return true;
    }

    private void launch() {
        double dx = target.x - goat.posX;
        double dz = target.z - goat.posZ;
        double distance = MathHelper.sqrt(dx * dx + dz * dz);

        if (distance < 0.0001D) {
            resetTask();
            return;
        }

        goat.motionX = dx / distance * HORIZONTAL_SPEED;
        goat.motionY = 0.52D + MathHelper.clamp((target.y - goat.posY) * 0.08D, -0.08D, 0.18D);
        goat.motionZ = dz / distance * HORIZONTAL_SPEED;
        goat.velocityChanged = true;
        goat.setLoweringHead(false);
        goat.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.7F, goat.isChild() ? 1.35F : 1.0F);
        phase = Phase.JUMP;
    }

    private int nextCooldown() {
        return 600 + goat.getRNG().nextInt(601);
    }

    private enum Phase {
        COOLDOWN,
        PREPARE,
        JUMP
    }
}
