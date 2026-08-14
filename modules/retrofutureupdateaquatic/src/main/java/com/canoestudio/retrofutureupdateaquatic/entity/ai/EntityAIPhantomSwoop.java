package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.sounds.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

/** 参考 Phantoms 模组的俯冲 AI，带有预热、命中和受击中断。 */
public class EntityAIPhantomSwoop extends EntityAIBase {

    private final EntityPhantom phantom;
    private boolean playedSound;

    public EntityAIPhantomSwoop(EntityPhantom phantom) {
        this.phantom = phantom;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.phantom.canTarget(this.phantom.getAttackTarget())) {
            return false;
        }
        if (this.phantom.getSwoopCooldown() > 0) {
            this.phantom.setSwoopCooldown(this.phantom.getSwoopCooldown() - 1);
            return false;
        }
        return this.phantom.getRNG().nextInt(20) == 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!this.phantom.canTarget(this.phantom.getAttackTarget())) {
            this.phantom.setAttackTarget(null);
            return false;
        }
        return this.phantom.getAttackState() == EntityPhantom.AttackState.SWOOPING;
    }

    @Override
    public void startExecuting() {
        this.phantom.setAttackState(EntityPhantom.AttackState.SWOOPING);
        this.playedSound = false;
    }

    @Override
    public void resetTask() {
        this.phantom.setAttackState(EntityPhantom.AttackState.CIRCLING);
        this.phantom.setSwoopCooldown(80 + this.phantom.getRNG().nextInt(80));
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = this.phantom.getAttackTarget();
        if (target == null) {
            return;
        }

        double distanceSq = this.phantom.getDistanceSq(target);
        if (distanceSq < 225.0D && !this.playedSound) {
            this.phantom.playSound(ModSounds.PHANTOM_SWOOP, 1.6F,
                0.95F + this.phantom.getRNG().nextFloat() * 0.1F);
            this.playedSound = true;
        }
        if (distanceSq < 4.0D) {
            float damage = (float)this.phantom
                .getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            if (target.attackEntityFrom(DamageSource.causeMobDamage(this.phantom), damage)) {
                this.phantom.playSound(ModSounds.PHANTOM_BITE, 0.8F,
                    0.95F + this.phantom.getRNG().nextFloat() * 0.1F);
            }
            this.resetTask();
            return;
        }
        if (!this.phantom.world.isAirBlock(this.phantom.getPosition().down())
                || this.phantom.posY - target.posY + target.height * 0.5D < 0.0D) {
            this.resetTask();
            return;
        }

        this.phantom.getMoveHelper().setMoveTo(target.posX,
            target.posY + target.height * 0.45D, target.posZ, 0.1D);
    }
}
