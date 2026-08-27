package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityPhantom;
import com.canoestudio.retrofutureupdateaquatic.potion.ModPotions;
import com.canoestudio.retrofuturemccore.api.fluid.FluidloggedSupport;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/** Entity effects, undead conversion, and natural phantom spawning. */
@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class AquaticEntityEvents {

    private static final int ZOMBIE_WATER_TIME = 600;
    private static final int ZOMBIE_CONVERSION_TIME = 300;
    private static final int CONDUIT_EFFECT_DURATION = 210;
    private static final int CONDUIT_REFRESH_THRESHOLD = 100;
    private static final String ZOMBIE_WATER_TICKS = "RetroFutureAquaticZombieWaterTicks";
    private static final String ZOMBIE_CONVERSION_TICKS = "RetroFutureAquaticZombieConversionTicks";

    private AquaticEntityEvents() {
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (!living.world.isRemote) {
            updateConduitPower(living);
            updateZombieDrownedConversion(living);
        }

        if (living.isPotionActive(ModPotions.SLOW_FALLING)) {
            applySlowFallingMotion(living);
        }

        if (!(living instanceof EntityDrowned) && !(living instanceof EntityPhantom)
                && living.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD
                && FluidloggedSupport.isEntityInWater(living)) {
            // 1.13 undead mobs sink instead of inheriting the old water
            // floating behavior. Drowned and phantoms have their own movement.
            living.motionY = Math.max(living.motionY - 0.02D, -0.35D);
            living.fallDistance = 0.0F;
        }
    }

    /**
     * Conduit Power is a marker effect in this 1.12 port. The three vanilla
     * effects are derived here so the effect remains useful to normal 1.12
     * mechanics, while only the short ambient effects are removed on exit.
     */
    private static void updateConduitPower(EntityLivingBase living) {
        PotionEffect conduit = living.getActivePotionEffect(ModPotions.CONDUIT_POWER);
        boolean powered = conduit != null
            && (FluidloggedSupport.isEntityInWater(living) || living.isWet());
        if (!powered) {
            removeConduitEffect(living, MobEffects.WATER_BREATHING);
            removeConduitEffect(living, MobEffects.NIGHT_VISION);
            removeConduitEffect(living, MobEffects.HASTE);
            return;
        }

        int amplifier = conduit.getAmplifier();
        applyConduitEffect(living, MobEffects.WATER_BREATHING, amplifier);
        applyConduitEffect(living, MobEffects.NIGHT_VISION, amplifier);
        applyConduitEffect(living, MobEffects.HASTE, amplifier);
        if (FluidloggedSupport.isEntityInWater(living)) {
            living.setAir(Math.max(living.getAir(), 301));
        }
    }

    private static void applyConduitEffect(EntityLivingBase living, Potion potion, int amplifier) {
        PotionEffect current = living.getActivePotionEffect(potion);
        if (current == null || (current.getAmplifier() <= amplifier
                && current.getDuration() < CONDUIT_REFRESH_THRESHOLD)) {
            living.addPotionEffect(new PotionEffect(potion, CONDUIT_EFFECT_DURATION, amplifier, true, false));
        }
    }

    private static void removeConduitEffect(EntityLivingBase living, Potion potion) {
        PotionEffect current = living.getActivePotionEffect(potion);
        if (current != null && current.getIsAmbient() && current.getDuration() <= CONDUIT_EFFECT_DURATION) {
            living.removePotionEffect(potion);
        }
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

        EntityZombie zombie = (EntityZombie) living;
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
            convertHuskToZombie((EntityHusk) zombie);
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

        copyEquipment(zombie, drowned);
        copyName(zombie, drowned);
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

        copyEquipment(husk, zombie);
        copyName(husk, zombie);
        clearZombieConversion(husk);
        husk.world.spawnEntity(zombie);
        husk.world.playEvent(1041, new BlockPos(husk), 0);
        husk.setDead();
    }

    private static void copyEquipment(EntityLivingBase source, EntityLivingBase target) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = source.getItemStackFromSlot(slot);
            if (!stack.isEmpty()) {
                target.setItemStackToSlot(slot, stack.copy());
                source.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void copyName(EntityLivingBase source, EntityLivingBase target) {
        if (source.hasCustomName()) {
            target.setCustomNameTag(source.getCustomNameTag());
            target.setAlwaysRenderNameTag(source.getAlwaysRenderNameTag());
        }
    }

    private static boolean isEyeInWater(EntityLivingBase living) {
        BlockPos eyePos = new BlockPos(living.posX, living.posY + living.getEyeHeight(), living.posZ);
        return FluidloggedSupport.isWater(living.world, eyePos);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote
                || world.provider.getDimension() != 0
                || world.getDifficulty() == EnumDifficulty.PEACEFUL
                || world.getWorldTime() % 1200L != 0L
                || world.getSkylightSubtracted() < 5) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<EntityPlayer> players = world.playerEntities;
        for (EntityPlayer player : players) {
            if (!AquaticPlayerEvents.canSpawnPhantomsFor(player) || world.rand.nextInt(3) != 0) {
                continue;
            }
            BlockPos base = new BlockPos(player);
            if (!AquaticPlayerEvents.hasSkySpawnPosition(player, base)) {
                continue;
            }
            BlockPos spawn = base.up(20 + world.rand.nextInt(16))
                .add(world.rand.nextInt(21) - 10, 0, world.rand.nextInt(21) - 10);
            if (!world.isAirBlock(spawn) || !world.canBlockSeeSky(spawn)) {
                continue;
            }
            int nearbyPhantoms = world.getEntitiesWithinAABB(EntityPhantom.class,
                player.getEntityBoundingBox().grow(32.0D)).size();
            if (nearbyPhantoms >= 8) {
                continue;
            }
            int groupSize = 1 + world.rand.nextInt(world.getDifficulty().getId() + 1);
            groupSize = Math.min(groupSize, 8 - nearbyPhantoms);
            for (int i = 0; i < groupSize; i++) {
                EntityPhantom phantom = new EntityPhantom(world);
                phantom.setLocationAndAngles(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                    world.rand.nextFloat() * 360.0F, 0.0F);
                phantom.onInitialSpawn(world.getDifficultyForLocation(spawn), null);
                world.spawnEntity(phantom);
            }
        }
    }
}
