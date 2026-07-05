package com.canoestudio.retrofuturemc.contents.mobs.glowsquid;

import com.canoestudio.retrofuturemc.contents.items.ModItems;
import com.canoestudio.retrofuturemc.sounds.ModSoundHandler;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Random;

public class EntityGlowSquid extends EntitySquid {
    private int darkTicksRemaining;

    public EntityGlowSquid(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (darkTicksRemaining > 0) {
            darkTicksRemaining--;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (super.attackEntityFrom(source, amount)) {
            darkTicksRemaining = 100;
            return true;
        }
        return false;
    }

    public float getGlowBrightness(float partialTicks) {
        if (darkTicksRemaining <= 0) {
            return 1.0F;
        }
        return 0.35F + Math.max(0.0F, 1.0F - (darkTicksRemaining - partialTicks) / 100.0F) * 0.65F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundHandler.ENTITY_GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSoundHandler.ENTITY_GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundHandler.ENTITY_GLOW_SQUID_DEATH;
    }

    @Override
    protected Item getDropItem() {
        return ModItems.GLOW_INK_SAC;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int count = 1 + rand.nextInt(3) + rand.nextInt(lootingModifier + 1);
        for (int i = 0; i < count; i++) {
            dropItem(ModItems.GLOW_INK_SAC, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return world.getDifficulty() != EnumDifficulty.PEACEFUL
                && pos.getY() <= 40
                && world.getLight(pos) == 0
                && super.getCanSpawnHere();
    }

    public static boolean canSpawnAt(World world, BlockPos pos, Random random) {
        return pos.getY() > 8
                && pos.getY() <= 40
                && world.getBlockState(pos).getMaterial().isLiquid()
                && world.getLight(pos) <= 1
                && random.nextInt(3) == 0;
    }
}
