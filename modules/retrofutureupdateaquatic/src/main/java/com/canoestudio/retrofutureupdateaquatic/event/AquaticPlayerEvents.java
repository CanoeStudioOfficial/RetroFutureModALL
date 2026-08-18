package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Player-only state and compatibility behavior for the aquatic update. */
@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class AquaticPlayerEvents {

    private static final String INSOMNIA_TICKS = "RetroFutureAquaticInsomniaTicks";

    private AquaticPlayerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        if (!player.world.isRemote) {
            if (player.isPlayerSleeping()) {
                player.getEntityData().setInteger(INSOMNIA_TICKS, 0);
            } else {
                int insomnia = Math.min(72000, player.getEntityData().getInteger(INSOMNIA_TICKS) + 1);
                player.getEntityData().setInteger(INSOMNIA_TICKS, insomnia);
            }
        }
        if (!player.capabilities.isFlying && FluidloggedSupport.isEntityInWater(player)
                && player.isPotionActive(ModPotions.DOLPHINS_GRACE)) {
            // 1.13 changes water drag from 0.8 to 0.96. Forge 1.12 has no
            // water-drag hook, so compensate after travel.
            player.motionX *= 1.2D;
            player.motionZ *= 1.2D;
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntityPlayer().getEntityData().setInteger(INSOMNIA_TICKS,
            event.getOriginal().getEntityData().getInteger(INSOMNIA_TICKS));
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntityLiving().isPotionActive(ModPotions.SLOW_FALLING)) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof EntityLivingBase
                && ((EntityLivingBase) event.getEntity()).isPotionActive(ModPotions.SLOW_FALLING)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty() || left.getItem() != Items.ELYTRA
                || right.getItem() != ModItems.PHANTOM_MEMBRANE
                || !left.isItemDamaged()) {
            return;
        }

        ItemStack repaired = left.copy();
        int repair = Math.min(repaired.getItemDamage(), repaired.getMaxDamage() / 4);
        repaired.setItemDamage(repaired.getItemDamage() - repair);
        event.setOutput(repaired);
        event.setCost(1);
        event.setMaterialCost(1);
    }

    /** Exposed for the phantom spawner without coupling it to player events. */
    public static int getInsomniaTicks(EntityPlayer player) {
        return player.getEntityData().getInteger(INSOMNIA_TICKS);
    }

    /** Exposed for the phantom spawner without duplicating the NBT key. */
    public static boolean canSpawnPhantomsFor(EntityPlayer player) {
        return !player.capabilities.isFlying && !player.capabilities.isCreativeMode && !player.isSpectator()
            && getInsomniaTicks(player) >= 72000;
    }

    /** Keep the spawn check in one place for future 1.13 parity adjustments. */
    public static boolean hasSkySpawnPosition(EntityPlayer player, BlockPos position) {
        return position.getY() >= player.world.getSeaLevel() && player.world.canBlockSeeSky(position);
    }
}
