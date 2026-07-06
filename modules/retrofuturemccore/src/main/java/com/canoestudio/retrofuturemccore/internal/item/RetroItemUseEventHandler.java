package com.canoestudio.retrofuturemccore.internal.item;

import com.canoestudio.retrofuturemccore.api.item.RetroUseItem;
import com.canoestudio.retrofuturemccore.api.item.RetroUseItemRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RetroItemUseEventHandler {

    @SubscribeEvent
    public void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        RetroUseItem handler = RetroUseItemRegistry.find(stack);
        if (handler == null) {
            return;
        }

        ActionResult<ItemStack> result = handler.onRightClick(player.world, player, event.getHand(), stack);
        event.setCancellationResult(result.getType());
        if (result.getType() != net.minecraft.util.EnumActionResult.PASS) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void useTick(LivingEntityUseItemEvent.Tick event) {
        RetroUseItem handler = RetroUseItemRegistry.find(event.getItem());
        if (handler != null) {
            handler.onUseTick(event.getEntityLiving().world, event.getEntityLiving(), event.getItem(),
                    event.getDuration());
        }
    }

    @SubscribeEvent
    public void useStop(LivingEntityUseItemEvent.Stop event) {
        RetroUseItem handler = RetroUseItemRegistry.find(event.getItem());
        if (handler != null) {
            handler.onUseStop(event.getEntityLiving().world, event.getEntityLiving(), event.getItem(),
                    event.getDuration());
        }
    }

    @SubscribeEvent
    public void useFinish(LivingEntityUseItemEvent.Finish event) {
        EntityLivingBase entity = event.getEntityLiving();
        RetroUseItem handler = RetroUseItemRegistry.find(event.getItem());
        if (handler != null) {
            event.setResultStack(handler.onUseFinish(entity.world, entity, event.getItem(), event.getResultStack()));
        }
    }
}
