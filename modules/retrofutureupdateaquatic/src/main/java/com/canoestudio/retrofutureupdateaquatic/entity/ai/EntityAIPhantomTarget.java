package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import java.util.List;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

/** 参考 Phantoms 模组的目标选择 AI，避免每 tick 重新搜索玩家。 */
public class EntityAIPhantomTarget extends EntityAIBase {

    private final EntityPhantom phantom;
    private int targetTicks;

    public EntityAIPhantomTarget(EntityPhantom phantom) {
        this.phantom = phantom;
    }

    @Override
    public boolean shouldExecute() {
        if (this.targetTicks-- > 0) {
            return false;
        }
        this.targetTicks = 60;

        double range = this.phantom.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
            .getAttributeValue();
        List<EntityPlayer> players = this.phantom.world.getEntitiesWithinAABB(EntityPlayer.class,
            this.phantom.getEntityBoundingBox().grow(range, 64.0D, range),
            this.phantom::canTarget);
        if (players.isEmpty()) {
            return false;
        }

        EntityPlayer target = players.get(0);
        for (EntityPlayer player : players) {
            if (player.posY > target.posY) {
                target = player;
            }
        }
        this.phantom.setAttackTarget(target);
        this.phantom.setTargetPos(target.getPosition().up(20));
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.phantom.canTarget(this.phantom.getAttackTarget())) {
            return true;
        }
        this.phantom.setAttackTarget(null);
        this.phantom.setAttackState(EntityPhantom.AttackState.CIRCLING);
        this.phantom.setTargetPos(BlockPos.ORIGIN);
        return false;
    }
}
