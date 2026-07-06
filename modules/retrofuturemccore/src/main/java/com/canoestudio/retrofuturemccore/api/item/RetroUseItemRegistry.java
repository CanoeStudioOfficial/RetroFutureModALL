package com.canoestudio.retrofuturemccore.api.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class RetroUseItemRegistry {

    private static final List<RetroUseItem> HANDLERS = new ArrayList<RetroUseItem>();

    private RetroUseItemRegistry() {
    }

    public static synchronized void register(RetroUseItem handler) {
        if (!HANDLERS.contains(handler)) {
            HANDLERS.add(handler);
        }
    }

    public static void register(final Item item, final RetroUseItem handler) {
        register(new RetroUseItem() {
            @Override
            public boolean matches(ItemStack stack) {
                return stack.getItem() == item && handler.matches(stack);
            }

            @Override
            public net.minecraft.util.ActionResult<ItemStack> onRightClick(net.minecraft.world.World world,
                    net.minecraft.entity.player.EntityPlayer player, net.minecraft.util.EnumHand hand, ItemStack stack) {
                return handler.onRightClick(world, player, hand, stack);
            }

            @Override
            public void onUseTick(net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity,
                    ItemStack stack, int remainingUseTicks) {
                handler.onUseTick(world, entity, stack, remainingUseTicks);
            }

            @Override
            public void onUseStop(net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity,
                    ItemStack stack, int remainingUseTicks) {
                handler.onUseStop(world, entity, stack, remainingUseTicks);
            }

            @Override
            public ItemStack onUseFinish(net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity,
                    ItemStack stack, ItemStack result) {
                return handler.onUseFinish(world, entity, stack, result);
            }

            @Override
            public int getUseDuration(ItemStack stack) {
                return handler.getUseDuration(stack);
            }
        });
    }

    public static synchronized List<RetroUseItem> getHandlers() {
        return Collections.unmodifiableList(new ArrayList<RetroUseItem>(HANDLERS));
    }

    public static RetroUseItem find(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (RetroUseItem handler : getHandlers()) {
            if (handler.matches(stack)) {
                return handler;
            }
        }
        return null;
    }
}
