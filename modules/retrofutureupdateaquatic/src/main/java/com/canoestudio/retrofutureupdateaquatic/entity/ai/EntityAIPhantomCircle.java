package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * 幻翼环绕目标点飞行。半径、高度和方向都带有低频随机变化，避免模型只在固定
 * 圆轨道上重复动作；目标点切换由 EntityAIPhantomTarget 完成。
 */
public class EntityAIPhantomCircle extends EntityAIBase {

    private final EntityPhantom phantom;
    private Vec3d nextPos;
    private float angle;
    private float radius;
    private float height;
    private boolean clockwise;

    public EntityAIPhantomCircle(EntityPhantom phantom) {
        this.phantom = phantom;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.phantom.getAttackTarget() == null
            || this.phantom.getAttackState() == EntityPhantom.AttackState.CIRCLING;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        Random random = this.phantom.getRNG();
        BlockPos target = this.phantom.getTargetPos();
        if (target.equals(BlockPos.ORIGIN)) {
            this.phantom.setTargetPos(this.phantom.getPosition().up(20));
            target = this.phantom.getTargetPos();
        }

        double dx = this.phantom.posX - target.getX() - 0.5D;
        double dz = this.phantom.posZ - target.getZ() - 0.5D;
        this.angle = (float)Math.toDegrees(Math.atan2(dz, dx));
        this.radius = 6.0F + random.nextFloat() * 8.0F;
        this.height = -2.0F + random.nextFloat() * 7.0F;
        this.clockwise = random.nextBoolean();
        this.findNext();
    }

    @Override
    public void updateTask() {
        Random random = this.phantom.getRNG();
        if (random.nextInt(90) == 0) {
            this.height = -4.0F + random.nextFloat() * 10.0F;
        }
        if (random.nextInt(70) == 0) {
            this.radius += random.nextBoolean() ? 1.0F : -1.0F;
            if (this.radius < 6.0F || this.radius > 18.0F) {
                this.radius = MathHelper.clamp(this.radius, 6.0F, 18.0F);
                this.clockwise = !this.clockwise;
            }
        }
        if (random.nextInt(120) == 0) {
            this.angle += random.nextBoolean() ? 45.0F : -45.0F;
            this.findNext();
        }

        if (this.nextPos == null || this.phantom.getDistanceSq(this.nextPos.x,
                this.nextPos.y, this.nextPos.z) <= 4.0D) {
            this.findNext();
        }

        if (this.nextPos.y < this.phantom.posY
                && !this.phantom.world.isAirBlock(this.phantom.getPosition().down())) {
            this.height = Math.max(1.0F, this.height);
            this.findNext();
        }
        if (this.nextPos.y > this.phantom.posY
                && !this.phantom.world.isAirBlock(this.phantom.getPosition().up())) {
            this.height = Math.min(-1.0F, this.height);
            this.findNext();
        }

        this.phantom.getMoveHelper().setMoveTo(this.nextPos.x, this.nextPos.y,
            this.nextPos.z, 0.1D);
    }

    private void findNext() {
        EntityLivingBase targetEntity = this.phantom.getAttackTarget();
        if (this.phantom.getTargetPos().equals(BlockPos.ORIGIN)) {
            this.phantom.setTargetPos(targetEntity == null
                ? this.phantom.getPosition().up(20)
                : targetEntity.getPosition().up(20));
        }

        this.angle += this.clockwise ? -15.0F : 15.0F;
        BlockPos target = this.phantom.getTargetPos();
        double radians = Math.toRadians(this.angle);
        this.nextPos = new Vec3d(target.getX() + 0.5D
                + Math.cos(radians) * this.radius,
            target.getY() + this.height - 4.0D,
            target.getZ() + 0.5D + Math.sin(radians) * this.radius);
    }
}
