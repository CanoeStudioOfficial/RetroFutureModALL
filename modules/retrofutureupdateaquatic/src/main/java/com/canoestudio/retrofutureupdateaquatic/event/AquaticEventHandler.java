package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.block.BlockBubbleColumn;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic.ID)
public final class AquaticEventHandler {

    private AquaticEventHandler() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (BlockBubbleColumn.isColumnBase(event.getPlacedBlock())) {
            BlockBubbleColumn.updateColumn(event.getWorld(), event.getPos().up());
        } else if (event.getPlacedBlock().getBlock() == ModBlocks.BUBBLE_COLUMN) {
            BlockBubbleColumn.updateColumn(event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.getItem().getToolClasses(stack).contains("axe")) {
            return;
        }

        IBlockState strippedState = ModBlocks.getStrippedState(event.getWorld().getBlockState(event.getPos()));
        if (strippedState == null) {
            return;
        }

        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), strippedState, 11);
            event.getWorld().playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS,
                1.0F, 1.0F);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.damageItem(1, event.getEntityPlayer());
            }
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) {
            return;
        }

        int startX = event.getChunk().x << 4;
        int startZ = event.getChunk().z << 4;
        int top = Math.min(event.getWorld().getSeaLevel(), event.getWorld().getHeight() - 2);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 1; y <= top; y++) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    if (BlockBubbleColumn.isColumnBase(event.getWorld().getBlockState(pos))) {
                        BlockBubbleColumn.updateColumn(event.getWorld(), pos.up());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer)event.getEntityLiving();
        if (player.ticksExisted % 40 != 0 || !player.isInWater()) {
            return;
        }
        ItemStack head = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.getItem() == ModItems.TURTLE_HELMET) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 200, 0, true, true));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || event.world.provider.getDimension() != 0
                || event.world.getDifficulty() == EnumDifficulty.PEACEFUL
                || event.world.getWorldTime() % 1200L != 0L
                || event.world.getSkylightSubtracted() < 5) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<EntityPlayer> players = event.world.playerEntities;
        for (EntityPlayer player : players) {
            if (player.capabilities.isCreativeMode || player.isSpectator()
                    || player.ticksExisted < 72000
                    || event.world.rand.nextInt(3) != 0) {
                continue;
            }
            BlockPos base = new BlockPos(player);
            if (base.getY() < event.world.getSeaLevel() || !event.world.canBlockSeeSky(base)) {
                continue;
            }
            BlockPos spawn = base.up(20 + event.world.rand.nextInt(16))
                .add(event.world.rand.nextInt(21) - 10, 0, event.world.rand.nextInt(21) - 10);
            if (!event.world.isAirBlock(spawn) || !event.world.canBlockSeeSky(spawn)) {
                continue;
            }
            int groupSize = 1 + event.world.rand.nextInt(event.world.getDifficulty().getId() + 1);
            for (int i = 0; i < groupSize; i++) {
                EntityPhantom phantom = new EntityPhantom(event.world);
                phantom.setLocationAndAngles(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                    event.world.rand.nextFloat() * 360.0F, 0.0F);
                event.world.spawnEntity(phantom);
            }
        }
    }
}
