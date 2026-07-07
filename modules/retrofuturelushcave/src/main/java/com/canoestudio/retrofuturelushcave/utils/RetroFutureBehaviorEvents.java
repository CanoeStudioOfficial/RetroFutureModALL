package com.canoestudio.retrofuturelushcave.utils;

import com.canoestudio.retrofuturelushcave.contents.blocks.CandleCakeBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.CandleBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.CopperBehavior;
import com.canoestudio.retrofuturelushcave.contents.blocks.LightningRodBlock;
import com.canoestudio.retrofuturelushcave.contents.blocks.ModBlocks;
import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.contents.mobs.axolotl.EntityAxolotl;
import com.canoestudio.retrofuturelushcave.sounds.ModSoundHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroBlockInteractionHandler;
import com.canoestudio.retrofuturemccore.api.event.RetroEventRegistry;
import com.canoestudio.retrofuturemccore.api.event.RetroEventResult;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.init.PotionTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RetroFutureBehaviorEvents {
    private static boolean candleDispenserBehaviorRegistered = false;
    private static boolean blockInteractionHandlerRegistered = false;
    private static final DamageSource FREEZE = new DamageSource("freeze").setDamageBypassesArmor();
    private static final int TICKS_TO_FREEZE = 140;
    private static final int FREEZE_DAMAGE_INTERVAL = 40;
    private static final double LIGHTNING_STRIKE_EPSILON = 1.0E-6D;
    private final Map<UUID, Integer> frozenTicks = new HashMap<>();

    public RetroFutureBehaviorEvents() {
        registerCandleDispenserBehavior();
        registerCoreBlockInteractionHandler();
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getWorld() instanceof WorldServer)) {
            return;
        }

        LightningRodData data = LightningRodData.get((WorldServer) event.getWorld());
        Chunk chunk = event.getChunk();
        ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
        for (ExtendedBlockStorage storage : storageArray) {
            if (storage == null || storage.isEmpty()) {
                continue;
            }

            int baseY = storage.getYLocation();
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        if (storage.get(x, y, z).getBlock() instanceof LightningRodBlock) {
                            data.add(new BlockPos((chunk.x << 4) + x, baseY + y, (chunk.z << 4) + z));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (event.getWorld().isRemote) {
            return;
        }

        if (entity instanceof EntityGuardian) {
            addGuardianAxolotlTarget((EntityGuardian) entity);
        }

        if (!(entity instanceof EntityLightningBolt)) {
            return;
        }

        World world = event.getWorld();
        BlockPos strikePos = new BlockPos(entity.posX, entity.posY - LIGHTNING_STRIKE_EPSILON, entity.posZ);
        if (activateLightningRodAt(world, strikePos)) {
            clearCopperAroundLightning(world, strikePos);
            return;
        }

        if (!world.isThundering()) {
            clearCopperAroundLightning(world, strikePos);
            return;
        }

        BlockPos rodPos = LightningRodData.get((WorldServer) world).getClosest((WorldServer) world, strikePos, LightningRodBlock.RANGE);
        if (rodPos == null) {
            clearCopperAroundLightning(world, strikePos);
            return;
        }

        entity.setPosition(rodPos.getX() + 0.5D, rodPos.getY() + 1.0D - LIGHTNING_STRIKE_EPSILON, rodPos.getZ() + 0.5D);
        activateLightningRodAt(world, rodPos);
        clearCopperAroundLightning(world, rodPos);
    }

    private void addGuardianAxolotlTarget(EntityGuardian guardian) {
        guardian.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(guardian, EntityAxolotl.class, true));
    }

    private RetroEventResult handleRightClickBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (handleCopperInteraction(world, pos, state, player, stack)) {
            return RetroEventResult.SUCCESS;
        }

        if (handlePowderSnowPickup(world, pos, state, player, hand, stack)) {
            return RetroEventResult.SUCCESS;
        }

        if (handleCandleLighting(world, pos, state, player, stack)) {
            return RetroEventResult.SUCCESS;
        }

        return handleCandleCakePlacement(world, pos, state, player, stack) ? RetroEventResult.SUCCESS : RetroEventResult.PASS;
    }

    private boolean handleCopperInteraction(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack) {
        if (!CopperBehavior.isCopper(state.getBlock()) || stack.isEmpty()) {
            return false;
        }

        if (isAxe(stack) && CopperBehavior.canScrape(state)) {
            if (!world.isRemote) {
                CopperBehavior.scrape(world, pos, state);
                world.playSound(null, pos, ModSoundHandler.STRIP_WOOD, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    stack.damageItem(1, player);
                }
            }
            return true;
        }

        if (isWax(stack) && CopperBehavior.canWax(state)) {
            if (!world.isRemote) {
                CopperBehavior.wax(world, pos, state);
                world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            return true;
        }

        return false;
    }

    private boolean handlePowderSnowPickup(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (state.getBlock() != ModBlocks.POWDER_SNOW || stack.getItem() != Items.BUCKET) {
            return false;
        }

        if (!world.isRemote) {
            world.setBlockToAir(pos);
            world.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
                ItemStack filled = new ItemStack(ModItems.POWDER_SNOW_BUCKET);
                if (stack.isEmpty()) {
                    player.setHeldItem(hand, filled);
                } else if (!player.inventory.addItemStackToInventory(filled)) {
                    player.dropItem(filled, false);
                }
            }
        }

        return true;
    }

    private boolean handleCandleCakePlacement(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack) {
        if (state.getBlock() != Blocks.CAKE || state.getValue(BlockCake.BITES) != 0 || !(stack.getItem() instanceof ItemBlock)) {
            return false;
        }

        Block candle = ((ItemBlock) stack.getItem()).getBlock();
        CandleCakeBlock cake = CandleCakeBlock.byCandle(candle);
        if (cake == null) {
            return false;
        }

        if (!world.isRemote) {
            world.setBlockState(pos, cake.getDefaultState(), 3);
            world.playSound(null, pos, cake.getSoundType(cake.getDefaultState(), world, pos, player).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        return true;
    }

    private boolean handleCandleLighting(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack) {
        if (!CandleBlock.isLightingItem(stack)) {
            return false;
        }

        if (CandleBlock.canLight(world, pos, state)) {
            CandleBlock.light(world, pos, state, player, stack, 3);
            return true;
        }

        if (CandleCakeBlock.canLight(state)) {
            CandleCakeBlock.light(world, pos, state, player, stack, 3);
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (entity.isDead) {
            frozenTicks.remove(entity.getUniqueID());
            return;
        }
        if (world.isRemote) {
            return;
        }

        BlockPos feet = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY + 0.05D, entity.posZ);
        BlockPos belowFeet = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.05D, entity.posZ);
        boolean inPowderSnow = world.getBlockState(feet).getBlock() == ModBlocks.POWDER_SNOW || world.getBlockState(belowFeet).getBlock() == ModBlocks.POWDER_SNOW;

        if (inPowderSnow && wearsLeatherBoots(entity) && !entity.isSneaking() && entity.motionY <= 0.0D) {
            BlockPos surface = world.getBlockState(feet).getBlock() == ModBlocks.POWDER_SNOW ? feet : belowFeet;
            double targetY = surface.getY() + 1.0D;
            double minY = entity.getEntityBoundingBox().minY;
            if (minY >= targetY - 0.18D && minY <= targetY + 0.08D && !entity.collidedHorizontally) {
                if (minY < targetY) {
                    entity.setPosition(entity.posX, entity.posY + targetY - minY, entity.posZ);
                }
                entity.motionY = 0.0D;
                entity.fallDistance = 0.0F;
                entity.onGround = true;
            }
        }

        updateFreeze(entity, inPowderSnow && !wearsLeatherArmor(entity));
    }

    @SubscribeEvent
    public void onFireballImpact(ProjectileImpactEvent.Fireball event) {
        if (!(event.getFireball() instanceof EntitySmallFireball)) {
            return;
        }

        handleBurningProjectileImpact(event.getFireball().world, event.getRayTraceResult(), event);
    }

    @SubscribeEvent
    public void onThrowableImpact(ProjectileImpactEvent.Throwable event) {
        EntityThrowable projectile = event.getThrowable();
        if (projectile instanceof EntityPotion) {
            handleWaterPotionImpact((EntityPotion) projectile, event.getRayTraceResult());
            return;
        }

        if (projectile.isBurning()) {
            handleBurningProjectileImpact(projectile.world, event.getRayTraceResult(), event);
        }
    }

    private void handleBurningProjectileImpact(World world, RayTraceResult hit, ProjectileImpactEvent event) {
        if (world.isRemote || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        if (lightCandleAt(world, pos, state)) {
            event.setCanceled(true);
            event.getEntity().setDead();
        }
    }

    private void handleWaterPotionImpact(EntityPotion potion, RayTraceResult hit) {
        World world = potion.world;
        if (world.isRemote || PotionUtils.getPotionFromItem(potion.getPotion()) != PotionTypes.WATER || !PotionUtils.getEffectsFromStack(potion.getPotion()).isEmpty()) {
            return;
        }

        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockEffectPos = hit.getBlockPos().offset(hit.sideHit);
            dowseCandleAt(world, hit.getBlockPos());
            dowseCandleAt(world, blockEffectPos);
            dowseCandleAt(world, blockEffectPos.offset(hit.sideHit.getOpposite()));
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                dowseCandleAt(world, blockEffectPos.offset(facing));
            }
        }
    }

    private boolean lightCandleAt(World world, BlockPos pos, IBlockState state) {
        if (CandleBlock.canLight(world, pos, state)) {
            CandleBlock.light(world, pos, state, null, ItemStack.EMPTY, 3);
            return true;
        }

        if (CandleCakeBlock.canLight(state)) {
            CandleCakeBlock.light(world, pos, state, null, ItemStack.EMPTY, 3);
            return true;
        }

        return false;
    }

    private boolean dowseCandleAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (CandleBlock.isLit(state)) {
            CandleBlock.extinguish(null, world, pos, state, 3);
            return true;
        }

        if (CandleCakeBlock.isLit(state)) {
            CandleCakeBlock.extinguish(null, world, pos, state, 3);
            return true;
        }

        return false;
    }

    private boolean activateLightningRodAt(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos, false)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof LightningRodBlock)) {
            return false;
        }

        ((LightningRodBlock) state.getBlock()).onLightningStrike(world, pos, state);
        return true;
    }

    private void clearCopperAroundLightning(World world, BlockPos center) {
        cleanCopperAt(world, center);

        Random rand = world.rand;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(center);
        for (int i = 0; i < 10; ++i) {
            pos.setPos(center.getX() + rand.nextInt(5) - 2, center.getY() + rand.nextInt(5) - 2, center.getZ() + rand.nextInt(5) - 2);
            cleanCopperAt(world, pos);
        }
    }

    private boolean cleanCopperAt(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos, false)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        return CopperBehavior.canLightningClean(state) && CopperBehavior.lightningClean(world, pos, state);
    }

    private void updateFreeze(EntityLivingBase entity, boolean freezing) {
        UUID id = entity.getUniqueID();
        int ticks = frozenTicks.containsKey(id) ? frozenTicks.get(id) : 0;
        if (freezing) {
            ticks = Math.min(TICKS_TO_FREEZE, ticks + 1);
        } else {
            ticks = Math.max(0, ticks - 2);
        }

        if (ticks == 0) {
            frozenTicks.remove(id);
            return;
        }

        frozenTicks.put(id, ticks);
        if (ticks >= TICKS_TO_FREEZE && entity.ticksExisted % FREEZE_DAMAGE_INTERVAL == 0) {
            entity.attackEntityFrom(FREEZE, 1.0F);
        }
    }

    private boolean isAxe(ItemStack stack) {
        return stack.getItem().getToolClasses(stack).contains("axe");
    }

    private boolean isWax(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        int[] ids = OreDictionary.getOreIDs(stack);
        for (int id : ids) {
            String name = OreDictionary.getOreName(id);
            if ("wax".equals(name) || "honeycomb".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean wearsLeatherBoots(EntityLivingBase entity) {
        return entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.LEATHER_BOOTS;
    }

    private boolean wearsLeatherArmor(EntityLivingBase entity) {
        ItemStack head = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        ItemStack legs = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        ItemStack feet = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        return isLeather(head.getItem()) || isLeather(chest.getItem()) || isLeather(legs.getItem()) || isLeather(feet.getItem());
    }

    private boolean isLeather(Item item) {
        return item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS;
    }

    private void registerCandleDispenserBehavior() {
        if (candleDispenserBehaviorRegistered) {
            return;
        }

        candleDispenserBehaviorRegistered = true;
        wrapLightingDispenserBehavior(Items.FLINT_AND_STEEL);
        wrapLightingDispenserBehavior(Items.FIRE_CHARGE);
    }

    private void registerCoreBlockInteractionHandler() {
        if (blockInteractionHandlerRegistered) {
            return;
        }

        blockInteractionHandlerRegistered = true;
        RetroEventRegistry.registerBlockInteraction(new RetroBlockInteractionHandler() {
            @Override
            public RetroEventResult onRightClickBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                    EnumHand hand, ItemStack stack, EnumFacing face, Vec3d hitVec) {
                return RetroFutureBehaviorEvents.this.handleRightClickBlock(world, pos, state, player, hand, stack);
            }
        });
    }

    private void wrapLightingDispenserBehavior(final Item item) {
        final IBehaviorDispenseItem original = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, new IBehaviorDispenseItem() {
            @Override
            public ItemStack dispense(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos targetPos = source.getBlockPos().offset(facing);
                IBlockState targetState = world.getBlockState(targetPos);

                if (lightCandleAt(world, targetPos, targetState)) {
                    if (item == Items.FLINT_AND_STEEL) {
                        if (stack.attemptDamageItem(1, world.rand, null)) {
                            stack.setCount(0);
                        }
                    } else if (item == Items.FIRE_CHARGE) {
                        stack.shrink(1);
                    }
                    world.playEvent(item == Items.FIRE_CHARGE ? 1018 : 1000, source.getBlockPos(), 0);
                    return stack;
                }

                return original.dispense(source, stack);
            }
        });
    }
}
