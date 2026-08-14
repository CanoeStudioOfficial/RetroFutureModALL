package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.block.BlockBubbleColumn;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import java.util.List;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic.ID)
public final class AquaticEventHandler {

    private static final int ZOMBIE_WATER_TIME = 600;
    private static final int ZOMBIE_CONVERSION_TIME = 300;
    private static final String ZOMBIE_WATER_TICKS = "RetroFutureAquaticZombieWaterTicks";
    private static final String ZOMBIE_CONVERSION_TICKS = "RetroFutureAquaticZombieConversionTicks";
    private static final String INSOMNIA_TICKS = "RetroFutureAquaticInsomniaTicks";

    private AquaticEventHandler() {
    }

    @SubscribeEvent
    public static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.DRIED_KELP_BLOCK)) {
            event.setBurnTime(4000);
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        // 1.13's fishing table gives treasure a 5% roll and the treasure
        // pool gives the nautilus shell one of six equal entries.  Forge's
        // 1.12 ItemFishedEvent exposes the final stacks but not the selected
        // loot-table branch, so preserve the same aggregate 1/120 chance.
        if (event.getEntityPlayer().world.rand.nextInt(120) == 0) {
            event.getDrops().add(new ItemStack(ModItems.NAUTILUS_SHELL));
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (BlockBubbleColumn.isColumnBase(event.getPlacedBlock())) {
            BlockBubbleColumn.updateColumn(event.getWorld(), event.getPos().up());
        } else if (event.getPlacedBlock().getBlock() == ModBlocks.BUBBLE_COLUMN) {
            BlockBubbleColumn.updateColumn(event.getWorld(), event.getPos());
        }
    }

    /**
     * Refreshes a column when a base or its water is changed.  This keeps
     * player placement, fluid placement, and piston-like block updates
     * covered without scanning every block in a loaded chunk.
     */
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        refreshBubbleColumn(event.getWorld(), event.getPos());
        refreshBubbleColumn(event.getWorld(), event.getPos().up());
        refreshBubbleColumn(event.getWorld(), event.getPos().down());
    }

    private static void refreshBubbleColumn(net.minecraft.world.World world, BlockPos columnPos) {
        if (BlockBubbleColumn.isColumnBase(world.getBlockState(columnPos.down()))
                && (FluidloggedSupport.isWater(world, columnPos)
                    || world.getBlockState(columnPos).getBlock() == ModBlocks.BUBBLE_COLUMN)) {
            BlockBubbleColumn.updateColumn(world, columnPos);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (tryMarkBannerOnMap(event, stack)) {
            return;
        }

        if (tryCarvePumpkin(event, stack)) {
            return;
        }

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

    private static boolean tryCarvePumpkin(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.SHEARS
                || event.getWorld().getBlockState(event.getPos()).getBlock() != Blocks.PUMPKIN) {
            return false;
        }

        if (!event.getWorld().isRemote) {
            EnumFacing facing = event.getEntityPlayer().getHorizontalFacing().getOpposite();
            event.getWorld().setBlockState(event.getPos(), ModBlocks.CARVED_PUMPKIN.getDefaultState()
                .withProperty(BlockPumpkin.FACING, facing), 11);
            for (int i = 0; i < 4; i++) {
                event.getWorld().spawnEntity(new EntityItem(event.getWorld(), event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D,
                    new ItemStack(Items.PUMPKIN_SEEDS)));
            }
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.damageItem(1, event.getEntityPlayer());
            }
            event.getWorld().playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
        return true;
    }

    private static boolean tryMarkBannerOnMap(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return false;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() != Blocks.STANDING_BANNER && state.getBlock() != Blocks.WALL_BANNER) {
            return false;
        }

        MapData map = Items.FILLED_MAP.getMapData(stack, event.getWorld());
        if (map == null) {
            return false;
        }
        String id = "banner-" + event.getPos().getX() + "-" + event.getPos().getY() + "-"
            + event.getPos().getZ();
        if (!event.getWorld().isRemote && !hasMapDecoration(stack, id)) {
            MapData.addTargetDecoration(stack, event.getPos(), id, MapDecoration.Type.RED_MARKER);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
        return true;
    }

    private static boolean hasMapDecoration(ItemStack map, String id) {
        if (!map.hasTagCompound() || !map.getTagCompound().hasKey("Decorations", 9)) {
            return false;
        }
        NBTTagList decorations = map.getTagCompound().getTagList("Decorations", 10);
        for (int i = 0; i < decorations.tagCount(); i++) {
            if (id.equals(decorations.getCompoundTagAt(i).getString("id"))) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living.isPotionActive(ModPotions.CONDUIT_POWER)
                && FluidloggedSupport.isEntityInWater(living)) {
            // 1.13 treats Conduit Power as water breathing internally rather
            // than attaching a second visible Water Breathing effect.
            living.setAir(Math.max(living.getAir(), 301));
        }
        if (living.isPotionActive(ModPotions.SLOW_FALLING)) {
            applySlowFallingMotion(living);
        }

        if (!(living instanceof EntityDrowned) && !(living instanceof EntityPhantom)
                && living.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD
                && FluidloggedSupport.isEntityInWater(living)) {
            // 1.13 undead mobs sink instead of inheriting the old water
            // floating behaviour.  Drowned and phantoms have their own
            // movement controllers and are intentionally excluded.
            living.motionY = Math.max(living.motionY - 0.02D, -0.35D);
            living.fallDistance = 0.0F;
        }

        updateZombieDrownedConversion(living);

        if (!(living instanceof EntityPlayer) || living.world.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer)living;
        if (player.ticksExisted % 40 != 0 || !FluidloggedSupport.isEntityInWater(player)) {
            return;
        }
        ItemStack head = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.getItem() == ModItems.TURTLE_HELMET) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 200, 0, true, true));
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        PotionEffect conduit = player.getActivePotionEffect(ModPotions.CONDUIT_POWER);
        if (conduit == null) {
            return;
        }

        float conduitFactor = 1.0F + (conduit.getAmplifier() + 1) * 0.2F;
        PotionEffect haste = player.getActivePotionEffect(MobEffects.HASTE);
        float hasteFactor = haste == null ? 1.0F : 1.0F + (haste.getAmplifier() + 1) * 0.2F;
        event.setNewSpeed(event.getOriginalSpeed() / hasteFactor * Math.max(hasteFactor, conduitFactor));
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
            // 1.13's Dolphin's Grace changes water drag from 0.8 to 0.96.
            // Forge 1.12 has no water-drag hook, so compensate after travel.
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
                || living instanceof EntityZombieVillager
                || living.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return;
        }

        EntityZombie zombie = (EntityZombie)living;
        if (!zombie.isEntityAlive()) {
            clearZombieConversion(zombie);
            return;
        }

        int conversionTicks = zombie.getEntityData().getInteger(ZOMBIE_CONVERSION_TICKS);
        if (conversionTicks > 0) {
            int remaining = conversionTicks - 1;
            if (remaining <= 0) {
                convertUnderwaterZombie(zombie);
            } else {
                zombie.getEntityData().setInteger(ZOMBIE_CONVERSION_TICKS, remaining);
            }
            return;
        }

        if (isEyeInWater(zombie)) {
            int waterTicks = zombie.getEntityData().getInteger(ZOMBIE_WATER_TICKS) + 1;
            if (waterTicks >= ZOMBIE_WATER_TIME) {
                zombie.getEntityData().removeTag(ZOMBIE_WATER_TICKS);
                zombie.getEntityData().setInteger(ZOMBIE_CONVERSION_TICKS, ZOMBIE_CONVERSION_TIME);
            } else {
                zombie.getEntityData().setInteger(ZOMBIE_WATER_TICKS, waterTicks);
            }
        } else {
            clearZombieConversion(zombie);
        }
    }

    private static void clearZombieConversion(EntityZombie zombie) {
        zombie.getEntityData().removeTag(ZOMBIE_WATER_TICKS);
        zombie.getEntityData().removeTag(ZOMBIE_CONVERSION_TICKS);
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
        return FluidloggedSupport.isWater(living.world, eyePos);
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
                    || player.getEntityData().getInteger(INSOMNIA_TICKS) < 72000
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
            int nearbyPhantoms = event.world.getEntitiesWithinAABB(EntityPhantom.class,
                player.getEntityBoundingBox().grow(32.0D)).size();
            if (nearbyPhantoms >= 8) {
                continue;
            }
            int groupSize = 1 + event.world.rand.nextInt(event.world.getDifficulty().getId() + 1);
            groupSize = Math.min(groupSize, 8 - nearbyPhantoms);
            for (int i = 0; i < groupSize; i++) {
                EntityPhantom phantom = new EntityPhantom(event.world);
                phantom.setLocationAndAngles(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                    event.world.rand.nextFloat() * 360.0F, 0.0F);
                event.world.spawnEntity(phantom);
            }
        }
    }
}
