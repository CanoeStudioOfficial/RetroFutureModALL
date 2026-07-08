package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.block.BlockBubbleColumn;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic.ID)
public final class AquaticEventHandler {

    private static final int ZOMBIE_WATER_TIME = 600;
    private static final int ZOMBIE_CONVERSION_TIME = 300;
    private static final Map<EntityZombie, Integer> ZOMBIE_WATER_TICKS = new WeakHashMap<EntityZombie, Integer>();
    private static final Map<EntityZombie, Integer> ZOMBIE_CONVERSION_TICKS =
        new WeakHashMap<EntityZombie, Integer>();

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
        EntityLivingBase living = event.getEntityLiving();
        if (living.isPotionActive(ModPotions.SLOW_FALLING)) {
            applySlowFallingMotion(living);
        }

        updateZombieDrownedConversion(living);

        if (!(living instanceof EntityPlayer) || living.world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer)living;
        if (player.ticksExisted % 40 != 0 || !player.isInWater()) {
            return;
        }
        ItemStack head = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.getItem() == ModItems.TURTLE_HELMET) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 200, 0, true, true));
        }
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
                || right.getItem() != ModItems.PHANTOM_MEMBRANE || !left.isItemDamaged()) {
            return;
        }

        ItemStack repaired = left.copy();
        int repair = Math.min(repaired.getItemDamage(), repaired.getMaxDamage() / 4);
        repaired.setItemDamage(repaired.getItemDamage() - repair);
        event.setOutput(repaired);
        event.setCost(1);
        event.setMaterialCost(1);
    }

    private static void applySlowFallingMotion(EntityLivingBase living) {
        if (living.motionY < -0.12D) {
            living.motionY = -0.12D;
            living.velocityChanged = true;
        }
        if (!living.onGround) {
            living.fallDistance = 0.0F;
        }
    }

    private static void updateZombieDrownedConversion(EntityLivingBase living) {
        if (living.world.isRemote || !(living instanceof EntityZombie) || living instanceof EntityDrowned
                || living.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return;
        }

        EntityZombie zombie = (EntityZombie)living;
        if (!zombie.isEntityAlive()) {
            clearZombieConversion(zombie);
            return;
        }

        Integer conversionTicks = ZOMBIE_CONVERSION_TICKS.get(zombie);
        if (conversionTicks != null) {
            int remaining = conversionTicks.intValue() - 1;
            if (remaining <= 0) {
                convertUnderwaterZombie(zombie);
            } else {
                ZOMBIE_CONVERSION_TICKS.put(zombie, Integer.valueOf(remaining));
            }
            return;
        }

        if (isEyeInWater(zombie)) {
            int waterTicks = ZOMBIE_WATER_TICKS.containsKey(zombie)
                ? ZOMBIE_WATER_TICKS.get(zombie).intValue() + 1 : 1;
            if (waterTicks >= ZOMBIE_WATER_TIME) {
                ZOMBIE_WATER_TICKS.remove(zombie);
                ZOMBIE_CONVERSION_TICKS.put(zombie, Integer.valueOf(ZOMBIE_CONVERSION_TIME));
            } else {
                ZOMBIE_WATER_TICKS.put(zombie, Integer.valueOf(waterTicks));
            }
        } else {
            clearZombieConversion(zombie);
        }
    }

    private static void clearZombieConversion(EntityZombie zombie) {
        ZOMBIE_WATER_TICKS.remove(zombie);
        ZOMBIE_CONVERSION_TICKS.remove(zombie);
    }

    private static void convertUnderwaterZombie(EntityZombie zombie) {
        if (zombie instanceof EntityHusk) {
            convertHuskToZombie((EntityHusk)zombie);
        } else {
            convertZombieToDrowned(zombie);
        }
    }

    private static void convertZombieToDrowned(EntityZombie zombie) {
        EntityDrowned drowned = new EntityDrowned(zombie.world);
        drowned.copyLocationAndAnglesFrom(zombie);
        drowned.rotationYawHead = zombie.rotationYawHead;
        drowned.renderYawOffset = zombie.renderYawOffset;
        drowned.setHealth(Math.min(zombie.getHealth(), drowned.getMaxHealth()));
        drowned.setChild(zombie.isChild());

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = zombie.getItemStackFromSlot(slot);
            if (!stack.isEmpty()) {
                drowned.setItemStackToSlot(slot, stack.copy());
                zombie.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }

        if (zombie.hasCustomName()) {
            drowned.setCustomNameTag(zombie.getCustomNameTag());
            drowned.setAlwaysRenderNameTag(zombie.getAlwaysRenderNameTag());
        }
        clearZombieConversion(zombie);
        zombie.world.spawnEntity(drowned);
        zombie.world.playEvent(1040, new BlockPos(zombie), 0);
        zombie.setDead();
    }

    private static void convertHuskToZombie(EntityHusk husk) {
        EntityZombie zombie = new EntityZombie(husk.world);
        zombie.copyLocationAndAnglesFrom(husk);
        zombie.rotationYawHead = husk.rotationYawHead;
        zombie.renderYawOffset = husk.renderYawOffset;
        zombie.setHealth(Math.min(husk.getHealth(), zombie.getMaxHealth()));
        zombie.setChild(husk.isChild());

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = husk.getItemStackFromSlot(slot);
            if (!stack.isEmpty()) {
                zombie.setItemStackToSlot(slot, stack.copy());
                husk.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }

        if (husk.hasCustomName()) {
            zombie.setCustomNameTag(husk.getCustomNameTag());
            zombie.setAlwaysRenderNameTag(husk.getAlwaysRenderNameTag());
        }

        clearZombieConversion(husk);
        husk.world.spawnEntity(zombie);
        husk.world.playEvent(1041, new BlockPos(husk), 0);
        husk.setDead();
    }

    private static boolean isEyeInWater(EntityLivingBase living) {
        BlockPos eyePos = new BlockPos(living.posX, living.posY + living.getEyeHeight(), living.posZ);
        return living.world.getBlockState(eyePos).getMaterial() == net.minecraft.block.material.Material.WATER;
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
