package com.canoestudio.retrofuturemccore.internal.event;

import com.canoestudio.retrofuturemccore.api.event.MutableFloat;
import com.canoestudio.retrofuturemccore.api.event.RetroBlockInteractionHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroDropHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroEntityInteractionHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroEntityLifecycleHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroEventRegistry;
import com.canoestudio.retrofuturemccore.api.event.RetroEventResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RetroEventBridge {

    @SubscribeEvent
    public void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        for (RetroBlockInteractionHandler handler : RetroEventRegistry.getBlockInteractionHandlers()) {
            RetroEventResult result = handler.onRightClickBlock(event.getWorld(), event.getPos(), state,
                    event.getEntityPlayer(), event.getHand(), event.getItemStack(), event.getFace(), event.getHitVec());
            if (result.isHandled()) {
                event.setCancellationResult(result.getActionResult());
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void entityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        Entity target = event.getTarget();
        for (RetroEntityInteractionHandler handler : RetroEventRegistry.getEntityInteractionHandlers()) {
            RetroEventResult result = handler.onRightClickEntity(event.getWorld(), target, player, event.getHand(), stack);
            if (result.isHandled()) {
                event.setCancellationResult(result.getActionResult());
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void entityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        Entity target = event.getTarget();
        for (RetroEntityInteractionHandler handler : RetroEventRegistry.getEntityInteractionHandlers()) {
            RetroEventResult result = handler.onRightClickEntityAt(event.getWorld(), target, player, event.getHand(),
                    stack, event.getLocalPos());
            if (result.isHandled()) {
                event.setCancellationResult(result.getActionResult());
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        for (RetroEntityLifecycleHandler handler : RetroEventRegistry.getEntityLifecycleHandlers()) {
            handler.onEntityJoinWorld(event.getWorld(), event.getEntity());
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        for (RetroEntityLifecycleHandler handler : RetroEventRegistry.getEntityLifecycleHandlers()) {
            handler.onLivingUpdate(entity.world, entity);
        }
    }

    @SubscribeEvent
    public void livingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        MutableFloat amount = new MutableFloat(event.getAmount());
        for (RetroEntityLifecycleHandler handler : RetroEventRegistry.getEntityLifecycleHandlers()) {
            if (handler.onLivingHurt(entity.world, entity, event.getSource(), amount)) {
                event.setCanceled(true);
                return;
            }
        }
        event.setAmount(amount.get());
    }

    @SubscribeEvent
    public void livingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        for (RetroEntityLifecycleHandler handler : RetroEventRegistry.getEntityLifecycleHandlers()) {
            if (handler.onLivingDeath(entity.world, entity, event.getSource())) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void livingDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        for (RetroDropHandler handler : RetroEventRegistry.getDropHandlers()) {
            if (handler.onLivingDrops(entity.world, entity, event.getSource(), event.getDrops(), event.getLootingLevel(),
                    event.isRecentlyHit())) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
