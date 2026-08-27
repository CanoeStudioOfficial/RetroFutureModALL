package com.canoestudio.retrofutureupdateaquatic.entity.ai;

import com.canoestudio.retrofutureupdateaquatic.entity.EntityTurtle;
import java.util.Collections;
import java.util.Set;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Mature 1.12.2-style turtle temptation goal adapted from Oceanic Expanse.
 * Navigation owns movement; this goal only selects and follows the player.
 */
public final class EntityAITurtleTempt extends EntityAIBase {

    private final EntityCreature turtle;
    private final double speed;
    private final Set<Item> temptingItems;
    private EntityPlayer player;
    private int delay;

    public EntityAITurtleTempt(EntityCreature turtle, double speed, Item item) {
        this(turtle, speed, Collections.singleton(item));
    }

    public EntityAITurtleTempt(EntityCreature turtle, double speed, Set<Item> temptingItems) {
        this.turtle = turtle;
        this.speed = speed;
        this.temptingItems = temptingItems;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.delay > 0) {
            this.delay--;
            return false;
        }
        if (((EntityTurtle)this.turtle).hasEgg()) {
            return false;
        }
        this.player = this.turtle.world.getClosestPlayerToEntity(this.turtle, 10.0D);
        return this.player != null
            && (isTempting(this.player.getHeldItemMainhand())
                || isTempting(this.player.getHeldItemOffhand()));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.player != null
            && !((EntityTurtle)this.turtle).hasEgg()
            && this.player.isEntityAlive()
            && (isTempting(this.player.getHeldItemMainhand())
                || isTempting(this.player.getHeldItemOffhand()));
    }

    @Override
    public void startExecuting() {
        this.turtle.getNavigator().tryMoveToEntityLiving(this.player, this.speed);
    }

    @Override
    public void resetTask() {
        this.player = null;
        this.turtle.getNavigator().clearPath();
        this.delay = 100;
    }

    @Override
    public void updateTask() {
        this.turtle.getLookHelper().setLookPositionWithEntity(this.player,
            this.turtle.getHorizontalFaceSpeed() + 20.0F, this.turtle.getVerticalFaceSpeed());
        if (this.turtle.getDistanceSq(this.player) < 6.25D) {
            this.turtle.getNavigator().clearPath();
        } else {
            this.turtle.getNavigator().tryMoveToEntityLiving(this.player, this.speed);
        }
    }

    private boolean isTempting(ItemStack stack) {
        return stack != null && this.temptingItems.contains(stack.getItem());
    }
}
